package jp.aquafactory.lucisnox.block.lightcollectorjar;

import jp.aquafactory.lucisnox.LucisNox;
import jp.aquafactory.lucisnox.registry.BlockRegistry;
import jp.aquafactory.lucisnox.registry.ItemRegistry;
import jp.aquafactory.lucisnox.item.LightCollectorJarItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(LucisNox.MODID)
@PrefixGameTestTemplate(false)
public final class LightCollectorJarGameTests {
    private static final BlockPos JAR_POS = new BlockPos(1, 1, 1);

    @GameTest(template = "empty", timeoutTicks = 1300)
    public static void activeGenerationIncludesFinalSecond(GameTestHelper helper) {
        encloseJar(helper);
        helper.runAtTickTime(40L, () -> {
            var jar = resetJar(helper, 0);
            helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Failed to start active generation");
            helper.runAfterDelay(1181L, () -> helper.assertTrue(jar.getStoredLicht() == 1770, "Stored Licht just before expiration"));
            helper.runAfterDelay(1201L, () -> {
                helper.assertTrue(jar.getStoredLicht() == 1800, "Stored Licht after 60 seconds");
                helper.assertFalse(jar.isActiveGeneration(), "Active generation continued after expiration");
                helper.assertTrue(jar.getActiveGenerationUntilGameTime() == 0L, "Cleared active generation deadline");
            });
            helper.runAfterDelay(1221L, () -> {
                helper.assertTrue(jar.getStoredLicht() == 1800, "Stored Licht after expiration in darkness");
                helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Failed to restart active generation after expiration");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1300)
    public static void activeGenerationStartsBetweenIntervals(GameTestHelper helper) {
        encloseJar(helper);
        helper.runAtTickTime(40L, () -> {
            var jar = resetJar(helper, 0);
            helper.runAfterDelay(7L, () ->
                    helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Failed to start active generation between collection intervals"));
            helper.runAfterDelay(1221L, () -> {
                helper.assertTrue(jar.getStoredLicht() == 1800, "Stored Licht when active generation starts between collection intervals");
                helper.assertFalse(jar.isActiveGeneration(), "Active generation started between collection intervals did not expire");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1300)
    public static void overdueCollectionRetainsActiveYield(GameTestHelper helper) {
        encloseJar(helper);
        helper.runAtTickTime(40L, () -> {
            // 未集計周期をまとめて処理する経路は、ワールドの自動 tick に登録しない瓶で確認する。
            var jar = new LightCollectorJarBlockEntity(helper.absolutePos(JAR_POS), helper.getBlockState(JAR_POS));
            jar.setLevel(helper.getLevel());
            jar.onLoad();
            helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Failed to start active generation");
            helper.runAfterDelay(1240L, () -> {
                LightCollectorJarBlockEntity.serverTick(helper.getLevel(), jar.getBlockPos(), jar.getBlockState(), jar);
                helper.assertTrue(jar.getStoredLicht() == 1800, "Stored Licht collected across the active generation deadline");
                helper.assertFalse(jar.isActiveGeneration(), "Active generation deadline was not cleared after collection");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1300)
    public static void fullStorageCapsActiveGeneration(GameTestHelper helper) {
        encloseJar(helper);
        helper.runAtTickTime(40L, () -> {
            var jar = resetJar(helper, 19995);
            helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Failed to start active generation with nearly full storage");
            helper.runAfterDelay(21L, () -> helper.assertTrue(jar.getStoredLicht() == 20000, "Storage capacity"));
            helper.runAfterDelay(1201L, () -> {
                helper.assertTrue(jar.getStoredLicht() == 20000, "Stored Licht after reaching capacity");
                helper.assertFalse(jar.isActiveGeneration(), "Active generation deadline was not cleared at full capacity");
                helper.assertFalse(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Active generation started despite full storage");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty")
    public static void dropRestoresStorageWithoutActiveGeneration(GameTestHelper helper) {
        encloseJar(helper);
        var jar = resetJar(helper, 12345);
        helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Failed to start generation before drop");
        var drops = Block.getDrops(helper.getBlockState(JAR_POS), helper.getLevel(), helper.absolutePos(JAR_POS), jar);
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(ItemRegistry.LIGHT_COLLECTOR_JAR.get()), "Expected one jar drop");
        var stack = drops.get(0);
        helper.assertTrue(LightCollectorJarItem.getStoredLicht(stack) == 12345, "Jar drop lost stored Licht");
        helper.assertTrue(ItemRegistry.LIGHT_COLLECTOR_JAR.get().isBarVisible(stack), "Stored jar bar is hidden");

        // 設置時の vanilla BlockItem 復元経路を通し、NBT の保存側と読込側の不一致を検出する。
        helper.setBlock(JAR_POS, Blocks.AIR);
        helper.setBlock(JAR_POS, BlockRegistry.LIGHT_COLLECTOR_JAR.get());
        helper.assertTrue(BlockItem.updateCustomBlockEntityTag(helper.getLevel(), helper.makeMockSurvivalPlayer(),
                helper.absolutePos(JAR_POS), stack), "BlockItem failed to restore jar NBT");
        var restored = (LightCollectorJarBlockEntity) helper.getBlockEntity(JAR_POS);
        helper.assertTrue(restored.getStoredLicht() == 12345, "Placed jar lost stored Licht");
        helper.assertFalse(restored.isActiveGeneration(), "Dropped jar retained active generation");
        helper.assertTrue(restored.getActiveGenerationUntilGameTime() == 0L, "Dropped jar retained active deadline");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void saveAndUpdateTagRetainPlacedState(GameTestHelper helper) {
        encloseJar(helper);
        var jar = resetJar(helper, 4321);
        helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Failed to start generation before save");
        var saved = jar.saveWithFullMetadata();
        var loaded = new LightCollectorJarBlockEntity(jar.getBlockPos(), jar.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.load(saved);
        helper.assertTrue(loaded.getStoredLicht() == 4321, "Saved block entity lost stored Licht");
        helper.assertTrue(loaded.getActiveGenerationUntilGameTime() == jar.getActiveGenerationUntilGameTime(), "Save lost active deadline");
        var synced = new LightCollectorJarBlockEntity(jar.getBlockPos(), jar.getBlockState());
        synced.setLevel(helper.getLevel());
        synced.handleUpdateTag(jar.getUpdateTag());
        helper.assertTrue(synced.getStoredLicht() == 4321, "Update tag lost stored Licht");
        helper.assertTrue(synced.getActiveGenerationUntilGameTime() == jar.getActiveGenerationUntilGameTime(), "Update tag lost active deadline");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void insertionPreservesCostAndRejection(GameTestHelper helper) {
        encloseJar(helper);
        var jar = resetJar(helper, 0);
        var player = helper.makeMockSurvivalPlayer();
        var stack = new ItemStack(ItemRegistry.PHOSSHARD.get(), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var pos = helper.absolutePos(JAR_POS);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var block = BlockRegistry.LIGHT_COLLECTOR_JAR.get();
        var state = helper.getBlockState(JAR_POS);
        block.use(state, helper.getLevel(), pos, player, InteractionHand.OFF_HAND, hit);
        helper.assertFalse(jar.isActiveGeneration(), "Offhand insertion started generation");
        block.use(state, helper.getLevel(), pos, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(jar.isActiveGeneration() && stack.getCount() == 2, "Survival insertion did not consume exactly one shard");
        block.use(state, helper.getLevel(), pos, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(stack.getCount() == 2, "Rejected duplicate insertion consumed a shard");
        resetJar(helper, LightCollectorJarBlockEntity.MAX_LICHT);
        block.use(state, helper.getLevel(), pos, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(stack.getCount() == 2, "Full jar insertion consumed a shard");
        resetJar(helper, 0);
        player.getAbilities().instabuild = true;
        block.use(state, helper.getLevel(), pos, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(jar.isActiveGeneration() && stack.getCount() == 2, "Creative insertion consumed a shard or failed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void hopperInsertsOnlyOneShard(GameTestHelper helper) {
        encloseJar(helper);
        var jar = resetJar(helper, 0);
        var hopperPos = JAR_POS.above();
        helper.setBlock(hopperPos, Blocks.HOPPER);
        var hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
        hopper.setItem(0, new ItemStack(ItemRegistry.PHOSSHARD.get(), 3));
        helper.runAfterDelay(30L, () -> {
            helper.assertTrue(jar.isActiveGeneration(), "Hopper failed to start generation");
            helper.assertTrue(hopper.getItem(0).getCount() == 2, "Hopper inserted more than one shard");
            helper.succeed();
        });
    }

    private static void encloseJar(GameTestHelper helper) {
        // 時刻・天候・月齢に依存せず自然生成をゼロにするため、瓶を遮光する。
        BlockPos.betweenClosed(0, 0, 0, 2, 2, 2).forEach(pos -> helper.setBlock(pos, Blocks.STONE));
        helper.setBlock(JAR_POS, BlockRegistry.LIGHT_COLLECTOR_JAR.get());
    }

    private static LightCollectorJarBlockEntity resetJar(GameTestHelper helper, int storedLicht) {
        LightCollectorJarBlockEntity jar = (LightCollectorJarBlockEntity) helper.getBlockEntity(JAR_POS);
        var tag = new CompoundTag();
        tag.putInt(LightCollectorJarBlockEntity.STORED_LICHT_TAG, storedLicht);
        jar.load(tag);
        // 投入時刻と集計周期の境界を揃えて、最後の1秒を確実に通す。
        jar.onLoad();
        return jar;
    }
}

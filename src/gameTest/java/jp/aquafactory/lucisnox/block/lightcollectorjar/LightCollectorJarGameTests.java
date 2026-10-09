package jp.aquafactory.lucisnox.block.lightcollectorjar;

import jp.aquafactory.lucisnox.LucisNox;
import jp.aquafactory.lucisnox.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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
            helper.runAfterDelay(1181L, () -> helper.assertValueEqual(jar.getStoredLicht(), 1770, "Stored Licht just before expiration"));
            helper.runAfterDelay(1201L, () -> {
                helper.assertValueEqual(jar.getStoredLicht(), 1800, "Stored Licht after 60 seconds");
                helper.assertFalse(jar.isActiveGeneration(), "Active generation continued after expiration");
                helper.assertValueEqual(jar.getActiveGenerationUntilGameTime(), 0L, "Cleared active generation deadline");
            });
            helper.runAfterDelay(1221L, () -> {
                helper.assertValueEqual(jar.getStoredLicht(), 1800, "Stored Licht after expiration in darkness");
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
                helper.assertValueEqual(jar.getStoredLicht(), 1800, "Stored Licht when active generation starts between collection intervals");
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
                helper.assertValueEqual(jar.getStoredLicht(), 1800, "Stored Licht collected across the active generation deadline");
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
            helper.runAfterDelay(21L, () -> helper.assertValueEqual(jar.getStoredLicht(), 20000, "Storage capacity"));
            helper.runAfterDelay(1201L, () -> {
                helper.assertValueEqual(jar.getStoredLicht(), 20000, "Stored Licht after reaching capacity");
                helper.assertFalse(jar.isActiveGeneration(), "Active generation deadline was not cleared at full capacity");
                helper.assertFalse(jar.startActiveGeneration(helper.getLevel().getGameTime()), "Active generation started despite full storage");
                helper.succeed();
            });
        });
    }

    private static void encloseJar(GameTestHelper helper) {
        // 時刻・天候・月齢に依存せず自然生成をゼロにするため、瓶を遮光する。
        BlockPos.betweenClosed(0, 0, 0, 2, 2, 2).forEach(pos -> helper.setBlock(pos, Blocks.STONE));
        helper.setBlock(JAR_POS, BlockRegistry.LIGHT_COLLECTOR_JAR.get());
    }

    private static LightCollectorJarBlockEntity resetJar(GameTestHelper helper, int storedLicht) {
        LightCollectorJarBlockEntity jar = helper.getBlockEntity(JAR_POS);
        var tag = new CompoundTag();
        tag.putInt(LightCollectorJarBlockEntity.STORED_LICHT_TAG, storedLicht);
        jar.loadCustomOnly(tag, helper.getLevel().registryAccess());
        // 投入時刻と集計周期の境界を揃えて、最後の1秒を確実に通す。
        jar.onLoad();
        return jar;
    }
}

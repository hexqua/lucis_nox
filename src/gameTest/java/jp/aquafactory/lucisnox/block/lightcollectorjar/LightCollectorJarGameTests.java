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
            helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "能動生成を開始できない");
            helper.runAfterDelay(1181L, () -> helper.assertValueEqual(jar.getStoredLicht(), 1770, "終了直前のリヒト"));
            helper.runAfterDelay(1201L, () -> {
                helper.assertValueEqual(jar.getStoredLicht(), 1800, "60秒分のリヒト");
                helper.assertFalse(jar.isActiveGeneration(), "期限後も能動生成が続いている");
                helper.assertValueEqual(jar.getActiveGenerationUntilGameTime(), 0L, "期限の解除");
            });
            helper.runAfterDelay(1221L, () -> {
                helper.assertValueEqual(jar.getStoredLicht(), 1800, "暗所で終了後のリヒト");
                helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "終了後に再投入できない");
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
                    helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "周期途中で開始できない"));
            helper.runAfterDelay(1221L, () -> {
                helper.assertValueEqual(jar.getStoredLicht(), 1800, "周期途中で投入した場合のリヒト");
                helper.assertFalse(jar.isActiveGeneration(), "周期途中で投入した能動生成が終了していない");
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
            helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "能動生成を開始できない");
            helper.runAfterDelay(1240L, () -> {
                LightCollectorJarBlockEntity.serverTick(helper.getLevel(), jar.getBlockPos(), jar.getBlockState(), jar);
                helper.assertValueEqual(jar.getStoredLicht(), 1800, "期限をまたいで集計したリヒト");
                helper.assertFalse(jar.isActiveGeneration(), "集計後に期限が解除されていない");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1300)
    public static void fullStorageCapsActiveGeneration(GameTestHelper helper) {
        encloseJar(helper);
        helper.runAtTickTime(40L, () -> {
            var jar = resetJar(helper, 19995);
            helper.assertTrue(jar.startActiveGeneration(helper.getLevel().getGameTime()), "満杯直前に開始できない");
            helper.runAfterDelay(21L, () -> helper.assertValueEqual(jar.getStoredLicht(), 20000, "容量上限"));
            helper.runAfterDelay(1201L, () -> {
                helper.assertValueEqual(jar.getStoredLicht(), 20000, "満杯後のリヒト");
                helper.assertFalse(jar.isActiveGeneration(), "満杯時に期限が解除されていない");
                helper.assertFalse(jar.startActiveGeneration(helper.getLevel().getGameTime()), "満杯でも投入できてしまう");
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

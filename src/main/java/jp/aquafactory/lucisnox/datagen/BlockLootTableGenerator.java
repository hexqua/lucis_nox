package jp.aquafactory.lucisnox.datagen;

import jp.aquafactory.lucisnox.registry.BlockRegistry;
import jp.aquafactory.lucisnox.registry.ItemRegistry;
import java.util.List;
import java.util.Set;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.data.loot.BlockLootSubProvider;
import org.jetbrains.annotations.NotNull;

public final class BlockLootTableGenerator extends BlockLootSubProvider {
    public BlockLootTableGenerator() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        add(BlockRegistry.PHOSSHARD_ORE.get(), oreDrop(BlockRegistry.PHOSSHARD_ORE.get()));
        add(BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get(), oreDrop(BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get()));
        dropSelf(BlockRegistry.LIGHT_COLLECTOR_JAR.get());
    }

    private LootTable.Builder oreDrop(Block block) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(ItemRegistry.PHOSSHARD.get(),
                LootItem.lootTableItem(ItemRegistry.PHOSSHARD.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                        .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
        ));
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return List.of(
                BlockRegistry.PHOSSHARD_ORE.get(),
                BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get(),
                BlockRegistry.LIGHT_COLLECTOR_JAR.get()
        );
    }
}

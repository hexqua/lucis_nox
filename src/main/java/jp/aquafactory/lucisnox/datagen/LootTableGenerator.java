package jp.aquafactory.lucisnox.datagen;

import java.util.List;
import java.util.Set;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public final class LootTableGenerator extends LootTableProvider {
    public LootTableGenerator(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(BlockLootTableGenerator::new, LootContextParamSets.BLOCK)
        ));
    }
}

package jp.aquafactory.lucisnox.datagen;

import jp.aquafactory.lucisnox.worldgen.LucisNoxWorldgen;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class DataGenerator {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, LucisNoxWorldgen::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, LucisNoxWorldgen::bootstrapPlacedFeatures)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, LucisNoxWorldgen::bootstrapBiomeModifiers);

    private DataGenerator() {}

    public static void gatherData(GatherDataEvent event) {
        var existingFileHelper = event.getExistingFileHelper();

        event.createDatapackRegistryObjects(BUILDER);
        event.addProvider(new BlockTagGenerator(event.getGenerator().getPackOutput(), event.getLookupProvider(), existingFileHelper));
        event.addProvider(new LootTableGenerator(event.getGenerator().getPackOutput(), event.getLookupProvider()));
        event.addProvider(new BlockStateGenerator(event.getGenerator().getPackOutput(), existingFileHelper));
    }
}

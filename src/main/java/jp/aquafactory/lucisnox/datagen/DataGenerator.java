package jp.aquafactory.lucisnox.datagen;

import jp.aquafactory.lucisnox.worldgen.LucisNoxWorldgen;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import jp.aquafactory.lucisnox.LucisNox;
import java.util.Set;

public final class DataGenerator {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, LucisNoxWorldgen::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, LucisNoxWorldgen::bootstrapPlacedFeatures)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, LucisNoxWorldgen::bootstrapBiomeModifiers);

    private DataGenerator() {}

    public static void gatherData(GatherDataEvent event) {
        var existingFileHelper = event.getExistingFileHelper();

        // Forge の provider が生成 registry と通常の lookup を統合し、鉱石の参照を解決する。
        var registryProvider = new DatapackBuiltinEntriesProvider(event.getGenerator().getPackOutput(),
                event.getLookupProvider(), BUILDER, Set.of(LucisNox.MODID));
        event.getGenerator().addProvider(event.includeServer(), registryProvider);
        event.getGenerator().addProvider(event.includeServer(), new BlockTagGenerator(event.getGenerator().getPackOutput(), registryProvider.getRegistryProvider(), existingFileHelper));
        event.getGenerator().addProvider(event.includeServer(), new LootTableGenerator(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(event.includeClient(), new BlockStateGenerator(event.getGenerator().getPackOutput(), existingFileHelper));
        event.getGenerator().addProvider(event.includeServer(), new RecipeGenerator(event.getGenerator().getPackOutput()));
    }
}

package jp.aquafactory.lucisnox.worldgen;

import jp.aquafactory.lucisnox.LucisNox;
import jp.aquafactory.lucisnox.registry.BlockRegistry;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class LucisNoxWorldgen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> PHOSSHARD_ORE_CONFIGURED =
            createKey(Registries.CONFIGURED_FEATURE, "phosshard_ore");
    public static final ResourceKey<PlacedFeature> PHOSSHARD_ORE_PLACED =
            createKey(Registries.PLACED_FEATURE, "phosshard_ore");
    public static final ResourceKey<BiomeModifier> ADD_PHOSSHARD_ORE =
            createKey(NeoForgeRegistries.Keys.BIOME_MODIFIERS, "add_phosshard_ore");

    private LucisNoxWorldgen() {}

    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        var configuration = new OreConfiguration(List.of(
                OreConfiguration.target(
                        new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        BlockRegistry.PHOSSHARD_ORE.get().defaultBlockState()
                ),
                OreConfiguration.target(
                        new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                        BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get().defaultBlockState()
                )
        ), 17);

        context.register(PHOSSHARD_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, configuration));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        Holder<ConfiguredFeature<?, ?>> configuredFeature =
                context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(PHOSSHARD_ORE_CONFIGURED);

        context.register(PHOSSHARD_ORE_PLACED, new PlacedFeature(configuredFeature, List.of(
                CountPlacement.of(20),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-60), VerticalAnchor.absolute(36)),
                BiomeFilter.biome()
        )));
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderSet.Named<Biome> overworldBiomes = context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD);
        Holder<PlacedFeature> placedFeature = context.lookup(Registries.PLACED_FEATURE).getOrThrow(PHOSSHARD_ORE_PLACED);

        context.register(ADD_PHOSSHARD_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeature),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }

    private static <T> ResourceKey<T> createKey(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, ResourceLocation.fromNamespaceAndPath(LucisNox.MODID, path));
    }
}

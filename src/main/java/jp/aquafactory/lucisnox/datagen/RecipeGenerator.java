package jp.aquafactory.lucisnox.datagen;

import jp.aquafactory.lucisnox.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ItemRegistry.LIGHT_COLLECTOR_JAR.get())
                .pattern("GPG")
                .pattern("GSG")
                .pattern("GGG")
                .define('S', ItemRegistry.PHOSSHARD.get())
                .define('P', ItemTags.WOODEN_SLABS)
                .define('G', Items.GLASS_PANE)
                .unlockedBy(getHasName(ItemRegistry.PHOSSHARD.get()), has(ItemRegistry.PHOSSHARD.get()))
                .save(output);
    }
}

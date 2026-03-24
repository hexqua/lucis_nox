package jp.aquafactory.lucisnox.datagen;

import jp.aquafactory.lucisnox.LucisNox;
import jp.aquafactory.lucisnox.registry.BlockRegistry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public final class BlockTagGenerator extends BlockTagsProvider {
    public BlockTagGenerator(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, lookupProvider, LucisNox.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlockRegistry.PHOSSHARD_ORE.get())
                .add(BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(BlockRegistry.PHOSSHARD_ORE.get())
                .add(BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get());
    }
}

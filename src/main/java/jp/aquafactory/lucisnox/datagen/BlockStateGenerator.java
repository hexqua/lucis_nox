package jp.aquafactory.lucisnox.datagen;

import jp.aquafactory.lucisnox.LucisNox;
import jp.aquafactory.lucisnox.registry.BlockRegistry;
import jp.aquafactory.lucisnox.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LucisNox.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(BlockRegistry.PHOSSHARD_ORE.get(), cubeAll(BlockRegistry.PHOSSHARD_ORE.get()));
        simpleBlockWithItem(BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get(), cubeAll(BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get()));
        simpleBlock(BlockRegistry.LIGHT_COLLECTOR_JAR.get(), models().getExistingFile(modLoc("block/light_collector_jar")));
        itemModels().basicItem(ItemRegistry.PHOSSHARD.get());
    }
}

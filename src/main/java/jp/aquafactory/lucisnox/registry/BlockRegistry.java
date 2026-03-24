package jp.aquafactory.lucisnox.registry;

import jp.aquafactory.lucisnox.LucisNox;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LucisNox.MODID);

    public static final DeferredBlock<DropExperienceBlock> PHOSSHARD_ORE =
            BLOCKS.register("phosshard_ore",
                    () -> new DropExperienceBlock(UniformInt.of(2, 5), phosshardOreProperties(Blocks.IRON_ORE)));

    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_PHOSSHARD_ORE =
            BLOCKS.register("deepslate_phosshard_ore",
                    () -> new DropExperienceBlock(UniformInt.of(2, 5), phosshardOreProperties(Blocks.DEEPSLATE_IRON_ORE)));

    private BlockRegistry() {}

    private static BlockBehaviour.Properties phosshardOreProperties(net.minecraft.world.level.block.Block baseBlock) {
        return BlockBehaviour.Properties.ofFullCopy(baseBlock)
                .lightLevel(state -> 3);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

package jp.aquafactory.lucisnox.registry;

import jp.aquafactory.lucisnox.LucisNox;
import jp.aquafactory.lucisnox.block.lightcollectorjar.LightCollectorJar;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LucisNox.MODID);

    public static final RegistryObject<DropExperienceBlock> PHOSSHARD_ORE =
            BLOCKS.register("phosshard_ore",
                    () -> new DropExperienceBlock(phosshardOreProperties(Blocks.IRON_ORE), UniformInt.of(2, 5)));

    public static final RegistryObject<DropExperienceBlock> DEEPSLATE_PHOSSHARD_ORE =
            BLOCKS.register("deepslate_phosshard_ore",
                    () -> new DropExperienceBlock(phosshardOreProperties(Blocks.DEEPSLATE_IRON_ORE), UniformInt.of(2, 5)));

    public static final RegistryObject<LightCollectorJar> LIGHT_COLLECTOR_JAR =
            BLOCKS.register("light_collector_jar", () -> new LightCollectorJar());

    private BlockRegistry() {}

    private static BlockBehaviour.Properties phosshardOreProperties(Block baseBlock) {
        return BlockBehaviour.Properties.copy(baseBlock)
                .lightLevel(state -> 3);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

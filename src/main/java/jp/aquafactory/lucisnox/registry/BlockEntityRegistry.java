package jp.aquafactory.lucisnox.registry;

import jp.aquafactory.lucisnox.LucisNox;
import jp.aquafactory.lucisnox.block.lightcollectorjar.LightCollectorJarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public final class BlockEntityRegistry {
    private static final com.mojang.datafixers.types.Type<?> NO_DFU = null;

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LucisNox.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightCollectorJarBlockEntity>> LIGHT_COLLECTOR_JAR =
            register("light_collector_jar", LightCollectorJarBlockEntity::new, BlockRegistry.LIGHT_COLLECTOR_JAR);

    private BlockEntityRegistry() {}

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
            String id, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block
    ) {
        return BLOCK_ENTITY_TYPES.register(id, () -> BlockEntityType.Builder.of(factory, block.get()).build(NO_DFU));
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}

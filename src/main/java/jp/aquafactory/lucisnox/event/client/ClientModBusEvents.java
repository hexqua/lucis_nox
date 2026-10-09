package jp.aquafactory.lucisnox.event.client;

import jp.aquafactory.lucisnox.block.lightcollectorjar.LightCollectorJarBlockEntityRenderer;
import jp.aquafactory.lucisnox.registry.BlockEntityRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class ClientModBusEvents {
    private ClientModBusEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientModBusEvents::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.LIGHT_COLLECTOR_JAR.get(), LightCollectorJarBlockEntityRenderer::new);
    }
}

package jp.aquafactory.lucisnox.event.client;

import jp.aquafactory.lucisnox.block.lightcollectorjar.LightCollectorJarBlockEntityRenderer;
import jp.aquafactory.lucisnox.registry.BlockEntityRegistry;
import jp.aquafactory.lucisnox.registry.BlockRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class ClientModBusEvents {
    private ClientModBusEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientModBusEvents::onClientSetup);
        modEventBus.addListener(ClientModBusEvents::registerRenderers);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        //noinspection deprecation
        event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(BlockRegistry.LIGHT_COLLECTOR_JAR.get(), RenderType.cutout()));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.LIGHT_COLLECTOR_JAR.get(), LightCollectorJarBlockEntityRenderer::new);
    }
}

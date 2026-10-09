package jp.aquafactory.lucisnox;

import jp.aquafactory.lucisnox.datagen.DataGenerator;
import jp.aquafactory.lucisnox.event.client.ClientModBusEvents;
import jp.aquafactory.lucisnox.registry.BlockEntityRegistry;
import jp.aquafactory.lucisnox.registry.BlockRegistry;
import jp.aquafactory.lucisnox.registry.CreativeTabRegistry;
import jp.aquafactory.lucisnox.registry.ItemRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(LucisNox.MODID)
public class LucisNox {
    public static final String MODID = "lucisnox";

    public LucisNox(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        BlockRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        modEventBus.addListener(DataGenerator::gatherData);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientModBusEvents.register(modEventBus);
        }
    }
}

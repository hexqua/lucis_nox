package jp.aquafactory.lucisnox;

import jp.aquafactory.lucisnox.datagen.DataGenerator;
import jp.aquafactory.lucisnox.registry.BlockRegistry;
import jp.aquafactory.lucisnox.registry.CreativeTabRegistry;
import jp.aquafactory.lucisnox.registry.ItemRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(LucisNox.MODID)
public class LucisNox {
    public static final String MODID = "lucisnox";

    public LucisNox(IEventBus modEventBus, ModContainer modContainer) {
        BlockRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        modEventBus.addListener(DataGenerator::gatherData);
    }
}

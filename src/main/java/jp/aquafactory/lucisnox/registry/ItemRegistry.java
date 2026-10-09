package jp.aquafactory.lucisnox.registry;

import jp.aquafactory.lucisnox.item.LightCollectorJarItem;
import jp.aquafactory.lucisnox.LucisNox;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LucisNox.MODID);

    public static final RegistryObject<Item> PHOSSHARD = ITEMS.register("phosshard", () -> new Item(new Item.Properties()));
    public static final RegistryObject<BlockItem> PHOSSHARD_ORE = ITEMS.register("phosshard_ore", () -> new BlockItem(BlockRegistry.PHOSSHARD_ORE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> DEEPSLATE_PHOSSHARD_ORE = ITEMS.register("deepslate_phosshard_ore", () -> new BlockItem(BlockRegistry.DEEPSLATE_PHOSSHARD_ORE.get(), new Item.Properties()));
    public static final RegistryObject<LightCollectorJarItem> LIGHT_COLLECTOR_JAR =
            ITEMS.register("light_collector_jar",
                    () -> new LightCollectorJarItem(BlockRegistry.LIGHT_COLLECTOR_JAR.get(), new Item.Properties()));

    private ItemRegistry() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

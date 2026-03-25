package jp.aquafactory.lucisnox.registry;

import jp.aquafactory.lucisnox.item.LightCollectorJarItem;
import jp.aquafactory.lucisnox.LucisNox;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LucisNox.MODID);

    public static final DeferredItem<Item> PHOSSHARD = ITEMS.registerSimpleItem("phosshard");
    public static final DeferredItem<BlockItem> PHOSSHARD_ORE = ITEMS.registerSimpleBlockItem(BlockRegistry.PHOSSHARD_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_PHOSSHARD_ORE = ITEMS.registerSimpleBlockItem(BlockRegistry.DEEPSLATE_PHOSSHARD_ORE);
    public static final DeferredItem<LightCollectorJarItem> LIGHT_COLLECTOR_JAR =
            ITEMS.register("light_collector_jar",
                    () -> new LightCollectorJarItem(BlockRegistry.LIGHT_COLLECTOR_JAR.get(), new Item.Properties()));

    private ItemRegistry() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

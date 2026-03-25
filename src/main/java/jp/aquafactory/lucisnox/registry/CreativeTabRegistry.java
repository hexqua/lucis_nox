package jp.aquafactory.lucisnox.registry;

import jp.aquafactory.lucisnox.LucisNox;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LucisNox.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LUCIS_NOX =
            CREATIVE_MODE_TABS.register(LucisNox.MODID,
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + LucisNox.MODID))
                            .icon(() -> new ItemStack(ItemRegistry.PHOSSHARD.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(ItemRegistry.PHOSSHARD.get());
                                output.accept(ItemRegistry.PHOSSHARD_ORE.get());
                                output.accept(ItemRegistry.DEEPSLATE_PHOSSHARD_ORE.get());
                                output.accept(ItemRegistry.LIGHT_COLLECTOR_JAR.get());
                            })
                            .build());

    private CreativeTabRegistry() {}

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

package jp.aquafactory.lucisnox.item;

import jp.aquafactory.lucisnox.block.lightcollectorjar.LightCollectorJarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LightCollectorJarItem extends BlockItem {
    private static final int BAR_COLOR = 0xF3CD54;

    public LightCollectorJarItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return getStoredLicht(stack) > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        var storedLicht = getStoredLicht(stack);
        if (storedLicht <= 0) {
            return 0;
        }

        return Math.max(1, Math.round(13.0F * storedLicht / (float) LightCollectorJarBlockEntity.MAX_LICHT));
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(
                "tooltip.lucisnox.light_collector_jar.licht",
                getStoredLicht(stack),
                LightCollectorJarBlockEntity.MAX_LICHT
        ).withStyle(ChatFormatting.GOLD));
    }

    public static int getStoredLicht(ItemStack stack) {
        var customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return 0;
        }

        return Mth.clamp(customData.copyTag().getInt(LightCollectorJarBlockEntity.STORED_LICHT_TAG), 0, LightCollectorJarBlockEntity.MAX_LICHT);
    }
}

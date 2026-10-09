package jp.aquafactory.lucisnox.utility;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public final class MessageTools {
    private MessageTools() {}

    public static void sendActionBarError(@NotNull Player player, @NotNull String translationKey, Object... args) {
        sendActionBar(player, Component.translatable(translationKey, args).withStyle(ChatFormatting.RED));
    }

    public static void sendActionBar(@NotNull Player player, @NotNull Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(message));
            return;
        }

        player.displayClientMessage(message, true);
    }
}

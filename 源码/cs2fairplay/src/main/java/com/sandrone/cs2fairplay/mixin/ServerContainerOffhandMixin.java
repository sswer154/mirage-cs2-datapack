package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.MatchFairPlay;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerContainerOffhandMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleContainerClick", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/inventory/AbstractContainerMenu;suppressRemoteUpdates()V"), cancellable = true)
    private void cs2fairplay$rejectOffhandShortcut(ServerboundContainerClickPacket packet, CallbackInfo callback) {
        if (packet.getClickType() == ClickType.SWAP && packet.getButtonNum() == Inventory.SLOT_OFFHAND
                && MatchFairPlay.restrictsPlayerInput(player)) {
            // Vanilla predicts the container swap on the client before sending the packet.
            player.containerMenu.sendAllDataToRemote();
            callback.cancel();
        }
    }
}

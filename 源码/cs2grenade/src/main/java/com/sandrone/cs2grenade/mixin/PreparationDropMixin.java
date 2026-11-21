package com.sandrone.cs2grenade.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.ScoreHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class PreparationDropMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void cs2$blockHotbarDrop(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        var action = packet.getAction();
        if (action != ServerboundPlayerActionPacket.Action.DROP_ITEM
                && action != ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) {
            return;
        }
        if (player.serverLevel().getServer().isSameThread() && cs2$blocked(player.getMainHandItem())) {
            player.containerMenu.broadcastFullState();
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerClick", at = @At("HEAD"), cancellable = true)
    private void cs2$blockInventoryDrop(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (!player.serverLevel().getServer().isSameThread() || packet.getContainerId() != player.containerMenu.containerId) {
            return;
        }
        int slot = packet.getSlotNum();
        ItemStack stack;
        if (packet.getClickType() == ClickType.THROW && slot >= 0 && slot < player.containerMenu.slots.size()) {
            stack = player.containerMenu.getSlot(slot).getItem();
        } else if (packet.getClickType() == ClickType.PICKUP && slot == -999) {
            stack = player.containerMenu.getCarried();
        } else {
            return;
        }
        if (cs2$blocked(stack)) {
            player.containerMenu.broadcastFullState();
            ci.cancel();
        }
    }

    @Unique
    private boolean cs2$blocked(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains("cs2_util")) {
            return false;
        }
        var board = player.getScoreboard();
        var stateObjective = board.getObjective("cs2_state");
        var matchObjective = board.getObjective("cs2_inmatch");
        if (stateObjective == null || matchObjective == null) {
            return false;
        }
        var state = board.getPlayerScoreInfo(ScoreHolder.forNameOnly("#state"), stateObjective);
        var inMatch = board.getPlayerScoreInfo(player, matchObjective);
        return state != null && state.value() == 2 && inMatch != null && inMatch.value() == 1;
    }
}

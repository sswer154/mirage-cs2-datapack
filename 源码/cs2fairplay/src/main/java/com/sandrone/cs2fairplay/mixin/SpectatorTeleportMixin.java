package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.MatchFairPlay;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class SpectatorTeleportMixin {
    @Shadow public ServerPlayer player;
    @Shadow private Vec3 awaitingPositionFromClient;

    @Inject(method = "handleTeleportToEntityPacket", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$blockFreeSpectatorTeleport(ServerboundTeleportToEntityPacket packet, CallbackInfo callback) {
        if (player.getServer().isSameThread() && MatchFairPlay.blocksSpectatorTeleport(player)) {
            callback.cancel();
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$blockFreeSpectatorMovement(ServerboundMovePlayerPacket packet, CallbackInfo callback) {
        if (!player.getServer().isSameThread() || !MatchFairPlay.blocksSpectatorTeleport(player)
                || !packet.hasPosition()) return;
        boolean moves = packet.getX(player.getX()) != player.getX()
                || packet.getY(player.getY()) != player.getY()
                || packet.getZ(player.getZ()) != player.getZ();
        if (!moves) return;
        if (player.getCamera() == player && awaitingPositionFromClient == null) {
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        }
        callback.cancel();
    }
}

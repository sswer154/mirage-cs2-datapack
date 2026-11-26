package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.InputAuditServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
        "com.tacz.guns.network.message.ClientMessagePlayerShoot",
        "com.tacz.guns.network.message.ClientMessagePlayerReloadGun",
        "com.tacz.guns.network.message.ClientMessagePlayerDrawGun"
}, remap = false)
abstract class TaCZRequestMixin {
    @Inject(method = "handle", at = @At("HEAD"), require = 0)
    private static void cs2fairplay$recordRequest(@Coerce Object packet, IPayloadContext context, CallbackInfo callback) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        String action = switch (packet.getClass().getSimpleName()) {
            case "ClientMessagePlayerShoot" -> "fire_request";
            case "ClientMessagePlayerReloadGun" -> "reload_request";
            case "ClientMessagePlayerDrawGun" -> "draw_request";
            default -> null;
        };
        if (action != null) context.enqueueWork(() -> InputAuditServer.action(player, action, "1"));
    }
}

package com.sandrone.cs2footsteps.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 比赛队伍玩家：屏蔽原版 block.*.step，改由客户端模组播放。 */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void cs2footsteps$muteVanillaSteps(SoundInstance sound, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getTeam() == null) {
            return;
        }
        String team = mc.player.getTeam().getName();
        if (!"cs2_t".equals(team) && !"cs2_ct".equals(team)) {
            return;
        }
        var loc = sound.getLocation();
        String path = loc.getPath();
        if (loc.getNamespace().equals("minecraft") && path.startsWith("block.") && path.endsWith(".step")) {
            cir.setReturnValue(false);
        }
    }
}

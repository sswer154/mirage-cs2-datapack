package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.SpectatorHands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.sound.SoundPlayManager", remap = false)
abstract class SpectatorTaCZSoundMixin {
    @ModifyArgs(method = "lambda$playMessageSound$1", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lcom/tacz/guns/client/sound/SoundPlayManager;playClientSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/resources/ResourceLocation;FFIZIZZ)Lcom/tacz/guns/client/sound/GunSoundInstance;"), require = 0)
    private static void cs2fairplay$useObservedShotSound(Args args) {
        var replacement = SpectatorHands.shotSound(args.<Entity>get(0), args.<ResourceLocation>get(1));
        if (replacement == null) return;
        args.set(1, replacement.sound());
        args.set(2, replacement.volume());
        args.set(5, false); // first-person shots do not use third-person attenuation
        args.set(7, false); // a relative sound does not follow a world-space entity
        args.set(8, true);  // place the shot at the listening camera
    }
}

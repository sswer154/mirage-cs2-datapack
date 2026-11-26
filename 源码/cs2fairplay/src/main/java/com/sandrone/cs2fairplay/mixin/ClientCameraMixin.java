package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.FairPlayClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Options.class)
abstract class ClientCameraMixin {
    @ModifyVariable(method = "setCameraType", at = @At("HEAD"), argsOnly = true)
    private CameraType cs2fairplay$keepFirstPerson(CameraType wanted) {
        return FairPlayClient.locked() ? CameraType.FIRST_PERSON : wanted;
    }
}

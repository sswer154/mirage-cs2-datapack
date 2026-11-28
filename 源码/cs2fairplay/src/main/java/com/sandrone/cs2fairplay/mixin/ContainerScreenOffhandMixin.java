package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.FairPlayClient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
abstract class ContainerScreenOffhandMixin {
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$blockOffhandShortcut(Slot slot, int slotId, int button, ClickType type,
                                                  CallbackInfo callback) {
        if (type == ClickType.SWAP && button == Inventory.SLOT_OFFHAND && FairPlayClient.locked()) {
            callback.cancel();
        }
    }
}

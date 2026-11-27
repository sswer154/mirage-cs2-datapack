package com.sandrone.cs2fairplay.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface SpectatorItemHandMixin {
    @Invoker("renderArmWithItem")
    void cs2fairplay$renderArmWithItem(AbstractClientPlayer player, float partialTick, float pitch,
                                      InteractionHand hand, float swing, ItemStack stack, float equip,
                                      PoseStack pose, MultiBufferSource buffers, int light);

    @Invoker("renderPlayerArm")
    void cs2fairplay$renderPlayerArm(PoseStack pose, MultiBufferSource buffers, int light, float equip,
                                    float swing, HumanoidArm arm);
}

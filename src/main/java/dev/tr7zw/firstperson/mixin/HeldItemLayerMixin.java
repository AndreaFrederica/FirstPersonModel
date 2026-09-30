package dev.tr7zw.firstperson.mixin;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerHeldItem.class)
public abstract class HeldItemLayerMixin {
    @Inject(method = "renderHeldItem", at = @At("HEAD"), cancellable = true)
    private void firstperson$hideDuplicateItem(EntityLivingBase entity, ItemStack stack, TransformType transform,
            EnumHandSide side, CallbackInfo ci) {
        if (FirstPersonModelCore.instance.isRenderingPlayer() && FirstPersonModelCore.instance.hideArm(side)) ci.cancel();
    }
}

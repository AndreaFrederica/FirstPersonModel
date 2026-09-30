package dev.tr7zw.firstperson.mixin;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public abstract class ArmorLayerMixin {
    @Inject(method = "renderArmorLayer", at = @At("HEAD"), cancellable = true)
    private void firstperson$hideHelmet(EntityLivingBase entity, float limbSwing, float amount, float delta,
            float age, float yaw, float pitch, float scale, EntityEquipmentSlot slot, CallbackInfo ci) {
        if (FirstPersonModelCore.instance.isRenderingPlayer() && slot == EntityEquipmentSlot.HEAD) ci.cancel();
    }
}

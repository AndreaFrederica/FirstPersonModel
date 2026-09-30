package dev.tr7zw.firstperson.mixin;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderLivingBase.class)
public abstract class LivingRendererMixin {
    @Redirect(method = "renderLayers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/LayerRenderer;doRenderLayer(Lnet/minecraft/entity/EntityLivingBase;FFFFFFF)V"))
    private void firstperson$filterLayers(LayerRenderer layer, EntityLivingBase entity,
            float limbSwing, float amount, float delta, float age, float yaw, float pitch, float scale) {
        FirstPersonModelCore core = FirstPersonModelCore.instance;
        if (core.isRenderingPlayer()) {
            if (layer instanceof LayerCustomHead || layer instanceof LayerDeadmau5Head
                    || layer instanceof LayerArrow && !core.getConfig().renderStuckFeatures
                    || core.getConfig().hiddenLayers.contains(layer.getClass().getName())) return;
        }
        layer.doRenderLayer(entity, limbSwing, amount, delta, age, yaw, pitch, scale);
    }
}

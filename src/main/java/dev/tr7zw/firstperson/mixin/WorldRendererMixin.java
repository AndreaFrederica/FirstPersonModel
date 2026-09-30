package dev.tr7zw.firstperson.mixin;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class WorldRendererMixin {
    @Inject(method = "renderEntities", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;disableLightmap()V"))
    private void firstperson$renderBody(Entity camera, ICamera frustum, float partialTicks, CallbackInfo ci) {
        if (MinecraftForgeClient.getRenderPass() == 0) FirstPersonModelCore.instance.renderBody(partialTicks);
    }
}

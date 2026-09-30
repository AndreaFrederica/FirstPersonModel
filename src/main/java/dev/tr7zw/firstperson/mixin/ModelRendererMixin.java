package dev.tr7zw.firstperson.mixin;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelRenderer.class)
public abstract class ModelRendererMixin {
    @Shadow @Final private ModelBase baseModel;

    @Inject(method = {"render", "renderWithRotation"}, at = @At("HEAD"), cancellable = true)
    private void firstperson$hidePart(float scale, CallbackInfo ci) {
        if (baseModel instanceof ModelBiped biped
                && FirstPersonModelCore.instance.shouldHide(biped, (ModelRenderer) (Object) this)) ci.cancel();
    }
}

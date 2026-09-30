package dev.tr7zw.firstperson.mixin;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
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

    // Standard injectors also provide the selector refmap used by WrapMethod on Unimined 1.4.
    @Inject(method = {"render", "renderWithRotation"}, at = @At("HEAD"), cancellable = true)
    private void firstperson$hidePart(float scale, CallbackInfo ci) {
        if (baseModel instanceof ModelBiped biped
                && FirstPersonModelCore.instance.shouldHide(biped, (ModelRenderer) (Object) this)) ci.cancel();
    }

    @WrapMethod(method = {"render", "renderWithRotation"})
    private void firstperson$renderPart(float scale, Operation<Void> original) {
        ModelRenderer part = (ModelRenderer) (Object) this;
        FirstPersonModelCore core = FirstPersonModelCore.instance;
        if (!(baseModel instanceof ModelBiped biped) || !core.isRenderingPlayer()) {
            original.call(scale);
            return;
        }
        float angle = part.rotateAngleX;
        try {
            part.rotateAngleX += core.armOffset(biped, part);
            original.call(scale);
        } finally {
            part.rotateAngleX = angle;
        }
    }
}

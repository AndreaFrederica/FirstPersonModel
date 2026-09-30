package dev.tr7zw.firstperson.mixin;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import dev.tr7zw.firstperson.config.FirstPersonSettings.VanillaHands;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Invoker("renderArmFirstPerson")
    protected abstract void firstperson$renderArm(float equip, float swing, EnumHandSide side);

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V",
            at = @At("HEAD"), cancellable = true)
    private void firstperson$hands(AbstractClientPlayer player, float delta, float pitch, EnumHand hand,
            float swing, ItemStack stack, float equip, CallbackInfo ci) {
        FirstPersonModelCore core = FirstPersonModelCore.instance;
        if (!core.shouldRenderBody()) return;
        if (!core.showVanillaHands()) { ci.cancel(); return; }
        if (core.getConfig().vanillaHandsMode == VanillaHands.ALL_DOUBLE && hand == EnumHand.OFF_HAND
                && stack.isEmpty() && !player.isInvisible() && player.getHeldItemMainhand().getItem() != Items.FILLED_MAP) {
            GlStateManager.pushMatrix();
            try { firstperson$renderArm(equip, swing, player.getPrimaryHand().opposite()); }
            finally { GlStateManager.popMatrix(); }
        }
    }

    @ModifyVariable(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private float firstperson$lowerHands(float equip) {
        FirstPersonModelCore core = FirstPersonModelCore.instance;
        if (core.shouldRenderBody() && core.dynamicHandsEnabled()) {
            float pitch = net.minecraft.client.Minecraft.getMinecraft().player.rotationPitch;
            if (pitch > 15) return Math.max(equip, Math.min(1.2f, (pitch - 15) / 20f * 1.2f));
        }
        return equip;
    }
}

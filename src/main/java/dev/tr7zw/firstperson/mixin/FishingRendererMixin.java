package dev.tr7zw.firstperson.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderFish;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderFish.class)
public abstract class FishingRendererMixin {
    @Unique private Vec3d firstperson$lineOffset = Vec3d.ZERO;
    @Unique private boolean firstperson$modelRod;

    @WrapMethod(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V")
    private void firstperson$line(EntityFishHook hook, double x, double y, double z, float yaw, float delta,
                                  Operation<Void> original) {
        Vec3d savedOffset = firstperson$lineOffset;
        boolean savedMode = firstperson$modelRod;
        FirstPersonModelCore core = FirstPersonModelCore.instance;
        var player = Minecraft.getMinecraft().player;
        firstperson$modelRod = false;
        firstperson$lineOffset = Vec3d.ZERO;
        try {
            if (player != null && hook.getAngler() == player && core.shouldRenderBody()) {
                EnumHandSide side = player.getHeldItemMainhand().getItem() == Items.FISHING_ROD
                        ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
                firstperson$modelRod = !core.hideArm(side);
                if (firstperson$modelRod) firstperson$lineOffset = core.offset(player, delta);
            }
            original.call(hook, x, y, z, yaw, delta);
        } finally {
            firstperson$lineOffset = savedOffset;
            firstperson$modelRod = savedMode;
        }
    }

    @Redirect(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;thirdPersonView:I", ordinal = 1))
    private int firstperson$rodPerspective(GameSettings settings) {
        return firstperson$modelRod ? 1 : settings.thirdPersonView;
    }

    @Redirect(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;posX:D"))
    private double firstperson$x(EntityPlayer player) { return player.posX + firstperson$lineOffset.x; }
    @Redirect(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;prevPosX:D"))
    private double firstperson$previousX(EntityPlayer player) { return player.prevPosX + firstperson$lineOffset.x; }
    @Redirect(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;posY:D"))
    private double firstperson$y(EntityPlayer player) { return player.posY + firstperson$lineOffset.y; }
    @Redirect(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;prevPosY:D"))
    private double firstperson$previousY(EntityPlayer player) { return player.prevPosY + firstperson$lineOffset.y; }
    @Redirect(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;posZ:D"))
    private double firstperson$z(EntityPlayer player) { return player.posZ + firstperson$lineOffset.z; }
    @Redirect(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;prevPosZ:D"))
    private double firstperson$previousZ(EntityPlayer player) { return player.prevPosZ + firstperson$lineOffset.z; }
}

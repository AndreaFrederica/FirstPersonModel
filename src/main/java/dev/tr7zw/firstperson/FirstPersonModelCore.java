package dev.tr7zw.firstperson;

import dev.tr7zw.firstperson.api.FirstPersonAPI;
import dev.tr7zw.firstperson.config.FirstPersonSettings;
import dev.tr7zw.firstperson.config.FirstPersonSettings.VanillaHands;
import java.io.File;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public final class FirstPersonModelCore {
    public static final FirstPersonModelCore instance = new FirstPersonModelCore();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final FirstPersonSettings config = new FirstPersonSettings();
    private KeyBinding toggle;
    private boolean enabled;
    private boolean renderingPlayer;
    private int elytraCooldown;

    private FirstPersonModelCore() {}

    public void initialize(File file) {
        config.load(file);
        enabled = config.enabledByDefault;
        toggle = new KeyBinding("key.firstperson.toggle", Keyboard.KEY_F6, "key.categories.firstperson");
        ClientRegistry.registerKeyBinding(toggle);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public FirstPersonSettings getConfig() { return config; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public boolean isRenderingPlayer() { return renderingPlayer; }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (toggle.isPressed()) enabled = !enabled;
        if (mc.player == null) elytraCooldown = 0;
        else if (mc.player.isElytraFlying()) elytraCooldown = 2;
        else if (elytraCooldown > 0) elytraCooldown--;
    }

    @SubscribeEvent
    public void configChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if ("firstperson".equals(event.getModID())) {
            config.reload();
            enabled = config.enabledByDefault;
        }
    }

    public boolean shouldRenderBody() {
        if (!enabled || mc.player == null || mc.world == null || mc.gameSettings.thirdPersonView != 0
                || mc.getRenderViewEntity() != mc.player || mc.player.isSpectator() || mc.player.isPlayerSleeping()
                || mc.player.isElytraFlying() || elytraCooldown > 0 || !mc.player.isEntityAlive()
                || mc.player.isInvisible()) return false;
        if (holds(config.autoToggleModItems)) return false;
        return FirstPersonAPI.getActivationHandlers().stream().noneMatch(handler -> handler.preventFirstperson());
    }

    private boolean contains(Set<String> ids, ItemStack stack) {
        return !stack.isEmpty() && ids.contains(String.valueOf(stack.getItem().getRegistryName()));
    }

    private boolean holds(Set<String> ids) {
        return contains(ids, mc.player.getHeldItemMainhand()) || contains(ids, mc.player.getHeldItemOffhand());
    }

    public boolean dynamicHandsEnabled() {
        return config.dynamicMode && config.vanillaHandsMode != VanillaHands.OFF && !holds(config.autoVanillaHands);
    }

    public boolean showVanillaHands() {
        if (holds(config.autoVanillaHands)) return true;
        if (dynamicHandsEnabled() && mc.player.rotationPitch > 35) return false;
        return config.vanillaHandsMode == VanillaHands.ALL || config.vanillaHandsMode == VanillaHands.ALL_DOUBLE
                || config.vanillaHandsMode == VanillaHands.ITEMS
                && (!mc.player.getHeldItemMainhand().isEmpty() || !mc.player.getHeldItemOffhand().isEmpty());
    }

    public boolean hideArm(EnumHandSide side) {
        if (!showVanillaHands()) return false;
        // Both vanilla hands replace the model arms as a pair, including the two-handed map pose.
        return true;
    }

    public boolean shouldHide(ModelBiped model, ModelRenderer part) {
        if (!renderingPlayer) return false;
        if (part == model.bipedHead || part == model.bipedHeadwear) return true;
        if (part == model.bipedLeftArm && hideArm(EnumHandSide.LEFT)
                || part == model.bipedRightArm && hideArm(EnumHandSide.RIGHT)) return true;
        if (model instanceof ModelPlayer player) {
            return part == player.bipedLeftArmwear && hideArm(EnumHandSide.LEFT)
                    || part == player.bipedRightArmwear && hideArm(EnumHandSide.RIGHT);
        }
        return false;
    }

    public Vec3d offset(AbstractClientPlayer player, float delta) {
        float yaw = player.prevRenderYawOffset + MathHelper.wrapDegrees(player.renderYawOffset - player.prevRenderYawOffset) * delta;
        float distance = .25f + config.xOffset / 100f;
        if (player.isSneaking()) distance = .27f + config.sneakXOffset / 100f;
        else if (player.isRiding()) {
            distance = .20f + config.sitXOffset / 100f;
            if (player.getRidingEntity() instanceof EntityLivingBase mount) {
                yaw = mount.prevRenderYawOffset + MathHelper.wrapDegrees(mount.renderYawOffset - mount.prevRenderYawOffset) * delta;
                float head = player.prevRotationYawHead + MathHelper.wrapDegrees(player.rotationYawHead - player.prevRotationYawHead) * delta;
                float diff = MathHelper.wrapDegrees(head - yaw);
                float clamped = MathHelper.clamp(diff, -85f, 85f);
                yaw = head - clamped;
                if (clamped * clamped > 2500f) yaw += clamped * .2f;
            }
        }
        Vec3d offset = new Vec3d(distance * Math.sin(Math.toRadians(yaw)), 0, -distance * Math.cos(Math.toRadians(yaw)));
        for (var handler : FirstPersonAPI.getPlayerOffsetHandlers()) offset = handler.applyOffset(player, delta, Vec3d.ZERO, offset);
        return offset;
    }

    /** Run in the world's entity pass, using the vanilla lightmap and camera origin. */
    public void renderBody(float partialTicks) {
        if (renderingPlayer || !shouldRenderBody()) return;
        AbstractClientPlayer player = mc.player;
        Vec3d offset = offset(player, partialTicks);
        RenderManager manager = mc.getRenderManager();
        boolean shadow = manager.isRenderShadow();
        boolean debug = manager.isDebugBoundingBox();
        renderingPlayer = true;
        GlStateManager.pushMatrix();
        try {
            manager.setRenderShadow(false);
            manager.setDebugBoundingBox(false);
            // Translate the render only: never mutate entity positions, bounding boxes or camera state.
            GlStateManager.translate(offset.x, offset.y, offset.z);
            manager.renderEntityStatic(player, partialTicks, false);
        } finally {
            manager.setRenderShadow(shadow);
            manager.setDebugBoundingBox(debug);
            renderingPlayer = false;
            GlStateManager.popMatrix();
            GlStateManager.color(1, 1, 1, 1);
        }
    }
}

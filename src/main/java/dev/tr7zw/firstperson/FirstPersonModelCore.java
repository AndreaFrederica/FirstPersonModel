package dev.tr7zw.firstperson;

import dev.tr7zw.firstperson.api.FirstPersonAPI;
import dev.tr7zw.firstperson.compat.AquaAcrobaticsCompat;
import dev.tr7zw.firstperson.compat.PlayerPose;
import dev.tr7zw.firstperson.compat.UieCompat;
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
import net.minecraft.init.Items;
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
    private AquaAcrobaticsCompat aqua;
    private UieCompat uie;
    private PlayerPose renderPose = PlayerPose.STANDING;
    private HandPolicy renderHands;

    private FirstPersonModelCore() {}

    public void initialize(File file) {
        config.load(file);
        aqua = new AquaAcrobaticsCompat();
        uie = new UieCompat();
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
        return shouldRenderBody(pose(1));
    }

    public PlayerPose pose(float delta) {
        return aqua == null || mc.player == null ? PlayerPose.STANDING : aqua.sample(mc.player, delta);
    }

    private boolean shouldRenderBody(PlayerPose pose) {
        if (!enabled || mc.player == null || mc.world == null || mc.gameSettings.thirdPersonView != 0
                || mc.getRenderViewEntity() != mc.player || mc.player.isSpectator() || mc.player.isPlayerSleeping()
                || mc.player.isElytraFlying() || elytraCooldown > 0 || !mc.player.isEntityAlive()
                || mc.player.isInvisible()) return false;
        if (holds(config.autoToggleModItems)) return false;
        if (pose.leavingSwimPose()) return false;
        if (mc.player.isHandActive() && contains(config.spyglassItems, mc.player.getActiveItemStack())) return false;
        if (uie != null && (uie.ownsCamera() || config.disableDuringUieZoom && uie.isZooming())) return false;
        return FirstPersonAPI.getActivationHandlers().stream().noneMatch(handler -> handler.preventFirstperson());
    }

    private boolean contains(Set<String> ids, ItemStack stack) {
        return !stack.isEmpty() && ids.contains(String.valueOf(stack.getItem().getRegistryName()));
    }

    private boolean holds(Set<String> ids) {
        return contains(ids, mc.player.getHeldItemMainhand()) || contains(ids, mc.player.getHeldItemOffhand());
    }

    public boolean dynamicHandsEnabled() {
        PlayerPose pose = renderingPlayer ? renderPose : pose(1);
        return mc.player != null && !pose.prone() && !(config.vanillaHandsSkipSwimming && pose.swimming())
                && config.dynamicMode && config.vanillaHandsMode != VanillaHands.OFF && !holds(config.autoVanillaHands);
    }

    public boolean showVanillaHands() {
        return hands().vanillaHands();
    }

    private HandPolicy hands() {
        return renderingPlayer ? renderHands : hands(pose(1));
    }

    private HandPolicy hands(PlayerPose pose) {
        if (mc.player == null) return new HandPolicy(true, false, false, false, 0);
        ItemStack main = mc.player.getHeldItemMainhand(), off = mc.player.getHeldItemOffhand();
        return HandPolicy.calculate(config.vanillaHandsMode, config.dynamicMode, holds(config.autoVanillaHands),
                config.vanillaHandsSkipSwimming, pose.swimming(), pose.prone(), !main.isEmpty(), !off.isEmpty(),
                main.getItem() == Items.FILLED_MAP, mc.player.rotationPitch);
    }

    public boolean hideArm(EnumHandSide side) {
        HandPolicy hands = hands();
        return side == mc.player.getPrimaryHand() ? hands.hideMainArm() : hands.hideOffArm();
    }

    public boolean hideSwimmingBody() {
        return renderingPlayer && config.hideBodyWhileSwimming && renderPose.swimming();
    }

    public float armOffset(ModelBiped model, ModelRenderer part) {
        if (!renderingPlayer) return 0;
        boolean arm = part == model.bipedLeftArm || part == model.bipedRightArm;
        if (model instanceof ModelPlayer player) arm |= part == player.bipedLeftArmwear || part == player.bipedRightArmwear;
        return arm ? renderHands.armOffset() : 0;
    }

    public boolean shouldHide(ModelBiped model, ModelRenderer part) {
        if (!renderingPlayer) return false;
        if (part == model.bipedHead || part == model.bipedHeadwear) return true;
        if (part == model.bipedBody && hideSwimmingBody()) return true;
        if (part == model.bipedLeftArm && hideArm(EnumHandSide.LEFT)
                || part == model.bipedRightArm && hideArm(EnumHandSide.RIGHT)) return true;
        if (model instanceof ModelPlayer player) {
            return part == player.bipedBodyWear && hideSwimmingBody()
                    || part == player.bipedLeftArmwear && hideArm(EnumHandSide.LEFT)
                    || part == player.bipedRightArmwear && hideArm(EnumHandSide.RIGHT);
        }
        return false;
    }

    public Vec3d offset(AbstractClientPlayer player, float delta) {
        return offset(player, delta, pose(delta));
    }

    private Vec3d offset(AbstractClientPlayer player, float delta, PlayerPose pose) {
        float yaw = player.prevRenderYawOffset + MathHelper.wrapDegrees(player.renderYawOffset - player.prevRenderYawOffset) * delta;
        float distance = .25f + config.xOffset / 100f;
        double y = 0;
        if (pose.prone()) {
            yaw = player.prevRotationYawHead + MathHelper.wrapDegrees(player.rotationYawHead - player.prevRotationYawHead) * delta;
            float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * delta;
            distance = (pitch > 0 ? .60f : .50f) + (pose.swimming() ? config.swimXOffset : config.crawlXOffset) / 100f;
            // Match upstream's camera clearance while Aqua supplies the horizontal model rotation.
            y = pitch > 0 && pose.eyesUnderwater() ? .6 * Math.sin(Math.toRadians(pitch))
                    : -.01 * Math.sin(Math.toRadians(pitch));
        }
        else if (player.isSneaking() || pose.crouching()) distance = .27f + config.sneakXOffset / 100f;
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
        Vec3d offset = new Vec3d(distance * Math.sin(Math.toRadians(yaw)), y, -distance * Math.cos(Math.toRadians(yaw)));
        for (var handler : FirstPersonAPI.getPlayerOffsetHandlers()) offset = handler.applyOffset(player, delta, Vec3d.ZERO, offset);
        return offset;
    }

    /** Run in the world's entity pass, using the vanilla lightmap and camera origin. */
    public void renderBody(float partialTicks) {
        if (renderingPlayer) return;
        PlayerPose pose = pose(partialTicks);
        if (!shouldRenderBody(pose)) return;
        AbstractClientPlayer player = mc.player;
        Vec3d offset = offset(player, partialTicks, pose);
        RenderManager manager = mc.getRenderManager();
        boolean shadow = manager.isRenderShadow();
        boolean debug = manager.isDebugBoundingBox();
        float bodyYaw = player.renderYawOffset, previousBodyYaw = player.prevRenderYawOffset;
        renderPose = pose;
        renderHands = hands(pose);
        renderingPlayer = true;
        GlStateManager.pushMatrix();
        try {
            manager.setRenderShadow(false);
            manager.setDebugBoundingBox(false);
            if (pose.prone()) {
                player.renderYawOffset = player.rotationYawHead;
                player.prevRenderYawOffset = player.prevRotationYawHead;
            }
            // Translate the render only: never mutate entity positions, bounding boxes or camera state.
            GlStateManager.translate(offset.x, offset.y, offset.z);
            manager.renderEntityStatic(player, partialTicks, false);
        } finally {
            manager.setRenderShadow(shadow);
            manager.setDebugBoundingBox(debug);
            renderingPlayer = false;
            renderPose = PlayerPose.STANDING;
            renderHands = null;
            player.renderYawOffset = bodyYaw;
            player.prevRenderYawOffset = previousBodyYaw;
            GlStateManager.popMatrix();
            GlStateManager.color(1, 1, 1, 1);
        }
    }
}

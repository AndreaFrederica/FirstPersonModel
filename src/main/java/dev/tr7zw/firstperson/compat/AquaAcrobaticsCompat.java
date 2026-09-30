package dev.tr7zw.firstperson.compat;

import java.lang.reflect.Method;
import org.apache.logging.log4j.LogManager;

/** Optional public API bridge; no Aqua classes are linked or bundled into this mod. */
public final class AquaAcrobaticsCompat {
    private final Class<?> api;
    private Method prone, swimming, eyes, crouching, animation;
    private boolean failed;

    public AquaAcrobaticsCompat() {
        this(findApi());
    }

    AquaAcrobaticsCompat(Class<?> api) {
        this.api = api;
        if (api == null) return;
        try {
            // isVisuallySwimming() in Aqua means land crawling, unlike modern vanilla.
            prone = api.getMethod("isActuallySwimming");
            swimming = api.getMethod("isSwimming");
            eyes = api.getMethod("getEyesInWaterPlayer");
            crouching = api.getMethod("isActuallySneaking");
            animation = api.getMethod("getSwimAnimation", float.class);
        } catch (ReflectiveOperationException | LinkageError error) {
            fail(error);
        }
    }

    private static Class<?> findApi() {
        try {
            return Class.forName("com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable", false,
                    AquaAcrobaticsCompat.class.getClassLoader());
        } catch (ClassNotFoundException ignored) {
            return null;
        } catch (LinkageError error) {
            LogManager.getLogger("FirstPerson").warn("Unable to load Aqua Acrobatics API", error);
            return null;
        }
    }

    public PlayerPose sample(Object player, float delta) {
        if (api == null || failed || !api.isInstance(player)) return PlayerPose.STANDING;
        try {
            float amount = ((Number) animation.invoke(player, delta)).floatValue();
            amount = Float.isFinite(amount) ? Math.max(0, Math.min(1, amount)) : 0;
            return new PlayerPose((boolean) prone.invoke(player), (boolean) swimming.invoke(player),
                    (boolean) eyes.invoke(player), (boolean) crouching.invoke(player), amount);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            fail(error);
            return PlayerPose.STANDING;
        }
    }

    private void fail(Throwable error) {
        failed = true;
        LogManager.getLogger("FirstPerson").warn("Aqua Acrobatics API failed; disabling its adapter", error);
    }
}

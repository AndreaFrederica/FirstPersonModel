package dev.tr7zw.firstperson.api;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import java.util.ArrayList;
import java.util.List;

/** Client-only API. Handler lists intentionally remain mutable, as in upstream. */
public final class FirstPersonAPI {
    private static final List<PlayerOffsetHandler> OFFSETS = new ArrayList<>();
    private static final List<ActivationHandler> ACTIVATION = new ArrayList<>();
    private FirstPersonAPI() {}
    public static boolean isEnabled() { return FirstPersonModelCore.instance.isEnabled(); }
    public static void setEnabled(boolean enabled) { FirstPersonModelCore.instance.setEnabled(enabled); }
    public static boolean isRenderingPlayer() { return FirstPersonModelCore.instance.isRenderingPlayer(); }
    public static void registerPlayerHandler(Object handler) {
        if (handler instanceof PlayerOffsetHandler offset) OFFSETS.add(offset);
        if (handler instanceof ActivationHandler activation) ACTIVATION.add(activation);
    }
    public static List<PlayerOffsetHandler> getPlayerOffsetHandlers() { return OFFSETS; }
    public static List<ActivationHandler> getActivationHandlers() { return ACTIVATION; }
}

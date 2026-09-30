package dev.tr7zw.firstperson;

import dev.tr7zw.firstperson.config.FirstPersonSettings.VanillaHands;

/** One immutable decision shared by body arms, sleeves, items and vanilla hands. */
public record HandPolicy(boolean vanillaHands, boolean hideAllModelArms,
                         boolean hideMainArm, boolean hideOffArm, float armOffset) {
    public static HandPolicy calculate(VanillaHands mode, boolean dynamic, boolean forcedVanilla,
                                       boolean skipSwimming, boolean swimming, boolean prone,
                                       boolean mainItem, boolean offItem, boolean twoHandedMap, float pitch) {
        if (skipSwimming && swimming) return new HandPolicy(false, false, false, false, 0);
        boolean dynamicActive = dynamic && mode != VanillaHands.OFF && !forcedVanilla && !prone;
        boolean vanilla = forcedVanilla || mode == VanillaHands.ALL || mode == VanillaHands.ALL_DOUBLE
                || mode == VanillaHands.ITEMS && (mainItem || offItem);
        if (dynamicActive && pitch > 35) vanilla = false;
        // The upstream transition restores model arms at 30 degrees and hides vanilla at 35.
        boolean hideAll = (mode != VanillaHands.OFF || forcedVanilla) && !(dynamicActive && pitch > 30);
        boolean hideMain = hideAll || dynamicActive && pitch <= 30 && mainItem;
        boolean hideOff = hideAll || dynamicActive && pitch <= 30 && (offItem || twoHandedMap);
        float offset = dynamicActive && !hideAll ? Math.max(0, Math.min(.7f, -pitch / 20f + 2)) : 0;
        return new HandPolicy(vanilla, hideAll, hideMain, hideOff, offset);
    }
}

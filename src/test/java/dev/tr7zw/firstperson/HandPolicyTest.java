package dev.tr7zw.firstperson;

import dev.tr7zw.firstperson.config.FirstPersonSettings.VanillaHands;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandPolicyTest {
    @Test void swimmingUsesAnimatedArmsEvenWithMapOverride() {
        for (var mode : VanillaHands.values()) {
            var state = HandPolicy.calculate(mode, true, true, true, true, true, true, true, true, 70);
            assertFalse(state.vanillaHands());
            assertFalse(state.hideMainArm());
            assertFalse(state.hideOffArm());
            assertEquals(0, state.armOffset());
        }
    }

    @Test void swimmingCanKeepVanillaHandsWhenConfigured() {
        var state = HandPolicy.calculate(VanillaHands.ALL, true, false, false, true, true, false, false, false, 70);
        assertTrue(state.vanillaHands());
        assertTrue(state.hideAllModelArms());
    }

    @Test void dynamicTransitionRestoresBodyBeforeRemovingVanillaHands() {
        var upper = HandPolicy.calculate(VanillaHands.ALL, true, false, true, false, false, true, false, false, 20);
        var transition = HandPolicy.calculate(VanillaHands.ALL, true, false, true, false, false, true, false, false, 33);
        var down = HandPolicy.calculate(VanillaHands.ALL, true, false, true, false, false, true, false, false, 60);
        assertTrue(upper.hideMainArm());
        assertTrue(transition.vanillaHands());
        assertFalse(transition.hideMainArm());
        assertTrue(transition.armOffset() > 0);
        assertFalse(down.vanillaHands());
        assertFalse(down.hideMainArm());
    }

    @Test void mapOverrideAndCrawlingDoNotUseDynamicPitchCutoff() {
        assertTrue(HandPolicy.calculate(VanillaHands.OFF, true, true, true, false, false,
                true, false, true, 80).vanillaHands());
        assertTrue(HandPolicy.calculate(VanillaHands.ALL, true, false, true, false, true,
                true, false, false, 80).vanillaHands());
    }

    @Test void emptyItemsModeKeepsModelArmsWhenLookingDown() {
        var state = HandPolicy.calculate(VanillaHands.ITEMS, true, false, true, false, false,
                false, false, false, 70);
        assertFalse(state.vanillaHands());
        assertFalse(state.hideAllModelArms());
    }
}

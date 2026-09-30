package dev.tr7zw.firstperson.compat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompatibilityTest {
    public static class AquaPlayer {
        public boolean water;
        public boolean horizontal = true;
        public boolean eyes;
        public float animation = 1;
        public boolean isActuallySwimming() { return horizontal; }
        public boolean isSwimming() { return water; }
        public boolean getEyesInWaterPlayer() { return eyes; }
        public boolean isActuallySneaking() { return false; }
        public float getSwimAnimation(float delta) { return animation * delta; }
        // Deliberately opposite to water swimming, as in Aqua's real public API.
        public boolean isVisuallySwimming() { return horizontal && !water; }
    }

    public static class CameraApi {
        static boolean drone, freeLook, shoulder, override;
        public static boolean isDroneActive() { return drone; }
        public static boolean isFreeLookActive() { return freeLook; }
        public static boolean isShoulderActive() { return shoulder; }
        public static boolean isRenderOverrideActive() { return override; }
    }

    public static class ZoomApi {
        static boolean active;
        public static boolean isZoomActive() { return active; }
    }

    @Test void absentModsAndUnmodifiedEntitiesAreSafe() {
        assertEquals(PlayerPose.STANDING, new AquaAcrobaticsCompat(null).sample(new Object(), 1));
        assertEquals(PlayerPose.STANDING, new AquaAcrobaticsCompat(AquaPlayer.class).sample(new Object(), 1));
        var uie = new UieCompat(null, null);
        assertFalse(uie.ownsCamera());
        assertFalse(uie.isZooming());
    }

    @Test void aquaDistinguishesSwimmingCrawlingAndExitAnimation() {
        var adapter = new AquaAcrobaticsCompat(AquaPlayer.class);
        var player = new AquaPlayer();
        assertTrue(adapter.sample(player, 1).prone());
        assertFalse(adapter.sample(player, 1).swimming());
        player.water = true;
        player.eyes = true;
        var swimming = adapter.sample(player, .5f);
        assertTrue(swimming.prone());
        assertTrue(swimming.swimming());
        assertTrue(swimming.eyesUnderwater());
        assertEquals(.5f, swimming.swimAnimation());
        player.horizontal = false;
        player.water = false;
        assertTrue(adapter.sample(player, 1).leavingSwimPose());
        player.animation = 0;
        assertFalse(adapter.sample(player, 1).leavingSwimPose());
    }

    @Test void uieCameraOwnershipAndZoomAreIndependent() {
        var uie = new UieCompat(CameraApi.class, ZoomApi.class);
        try {
            assertFalse(uie.ownsCamera());
            CameraApi.freeLook = true;
            assertTrue(uie.ownsCamera());
            CameraApi.freeLook = false;
            CameraApi.drone = true;
            assertTrue(uie.ownsCamera());
            CameraApi.drone = false;
            CameraApi.shoulder = true;
            assertTrue(uie.ownsCamera());
            CameraApi.shoulder = false;
            CameraApi.override = true;
            assertTrue(uie.ownsCamera());
            CameraApi.override = false;
            ZoomApi.active = true;
            assertFalse(uie.ownsCamera());
            assertTrue(uie.isZooming());
        } finally {
            CameraApi.drone = CameraApi.freeLook = CameraApi.shoulder = CameraApi.override = ZoomApi.active = false;
        }
    }
}

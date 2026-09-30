package dev.tr7zw.firstperson.compat;

/** Aqua's horizontal pose includes both swimming and land crawling. */
public record PlayerPose(boolean prone, boolean swimming, boolean eyesUnderwater,
                         boolean crouching, float swimAnimation) {
    public static final PlayerPose STANDING = new PlayerPose(false, false, false, false, 0);

    public boolean leavingSwimPose() {
        return !prone && swimAnimation > 0;
    }
}

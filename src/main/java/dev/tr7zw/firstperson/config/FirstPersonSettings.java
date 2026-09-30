package dev.tr7zw.firstperson.config;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.common.config.Configuration;

public final class FirstPersonSettings {
    public enum VanillaHands { OFF, ITEMS, ALL, ALL_DOUBLE }

    public Configuration configuration;
    public boolean enabledByDefault;
    public int xOffset, sneakXOffset, sitXOffset;
    public boolean dynamicMode, renderStuckFeatures;
    public VanillaHands vanillaHandsMode;
    public Set<String> autoVanillaHands, autoToggleModItems, hiddenLayers;

    public void load(File file) {
        configuration = new Configuration(file);
        configuration.load();
        reload();
    }

    public void reload() {
        String category = Configuration.CATEGORY_GENERAL;
        enabledByDefault = configuration.getBoolean("enabledByDefault", category, true, "Enable on startup; F6 toggles for this session.");
        xOffset = configuration.getInt("xOffset", category, 0, -100, 100, "Standing offset in centimetres, added to 25 cm.");
        sneakXOffset = configuration.getInt("sneakXOffset", category, 0, -100, 100, "Sneaking offset, added to 27 cm.");
        sitXOffset = configuration.getInt("sitXOffset", category, 0, -100, 100, "Riding offset, added to 20 cm.");
        String mode = configuration.getString("vanillaHandsMode", category, "OFF", "Vanilla first-person hands mode.",
                new String[] {"OFF", "ITEMS", "ALL", "ALL_DOUBLE"});
        try { vanillaHandsMode = VanillaHands.valueOf(mode); }
        catch (IllegalArgumentException ignored) { vanillaHandsMode = VanillaHands.OFF; }
        dynamicMode = configuration.getBoolean("dynamicMode", category, true, "Switch from vanilla hands to model arms when looking down.");
        renderStuckFeatures = configuration.getBoolean("renderStuckFeatures", category, true, "Render arrows stuck in the body.");
        autoVanillaHands = strings("autoVanillaHands", new String[] {"minecraft:filled_map", "antiqueatlas:antique_atlas",
                "twilightforest:filled_magic_map", "twilightforest:filled_maze_map", "twilightforest:filled_ore_map"},
                "Items that always use vanilla hands (registry IDs).");
        autoToggleModItems = strings("autoToggleModItems", new String[0], "Items that temporarily disable the body renderer.");
        hiddenLayers = strings("hiddenLayers", new String[0], "Fully qualified layer class names to hide only during the first-person body pass.");
        if (configuration.hasChanged()) configuration.save();
    }

    private Set<String> strings(String key, String[] defaults, String description) {
        return new HashSet<>(Arrays.asList(configuration.getStringList(key, Configuration.CATEGORY_GENERAL, defaults, description)));
    }
}

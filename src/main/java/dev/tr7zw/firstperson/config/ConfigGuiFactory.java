package dev.tr7zw.firstperson.config;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

public final class ConfigGuiFactory implements IModGuiFactory {
    @Override public void initialize(Minecraft minecraft) {}
    @Override public boolean hasConfigGui() { return true; }
    @Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
    @Override public GuiScreen createConfigGui(GuiScreen parent) {
        return new GuiConfig(parent, new ConfigElement(FirstPersonModelCore.instance.getConfig().configuration
                .getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), "firstperson", false, false,
                "First-person Model");
    }
}

package dev.tr7zw.firstperson;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "firstperson", name = "First-person Model", version = "2.7.3-backport.1",
        acceptedMinecraftVersions = "[1.12.2]", clientSideOnly = true,
        acceptableRemoteVersions = "*", guiFactory = "dev.tr7zw.firstperson.config.ConfigGuiFactory")
public final class FirstPersonModelMod {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide().isClient()) {
            FirstPersonModelCore.instance.initialize(event.getSuggestedConfigurationFile());
        }
    }
}

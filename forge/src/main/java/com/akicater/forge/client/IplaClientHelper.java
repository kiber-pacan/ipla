package com.akicater.forge.client;


import com.akicater.client.screen.IPLA_ConfigScreenBase;
import net.minecraftforge.fml.ModLoadingContext;
#if MC_VER >= V1_19_2 && MC_VER < V1_20_4
import net.minecraftforge.client.ConfigScreenHandler;
#else
import net.minecraftforge.client.ConfigGuiHandler;
#endif

public class IplaClientHelper {
    public static void registerConfig() {
        ModLoadingContext.get().registerExtensionPoint(
            #if MC_VER >= V1_19_2 ConfigScreenHandler.ConfigScreenFactory.class #else ConfigGuiHandler.ConfigGuiFactory.class #endif,
                () -> new #if MC_VER >= V1_19_2 ConfigScreenHandler.ConfigScreenFactory #else ConfigGuiHandler.ConfigGuiFactory #endif(
                        (client, screen) -> new IPLA_ConfigScreenBase(screen)
                )
        );
    }
}
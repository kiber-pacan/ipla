package com.akicater.forge;

import com.akicater.Ipla;
import com.akicater.client.IplaConfig;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

#if MC_VER >= V1_19_2 && MC_VER < V1_20_4
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraftforge.client.ConfigScreenHandler;
#elif MC_VER <= V1_18_2
import net.minecraftforge.client.ConfigGuiHandler;
import me.shedaniel.autoconfig.AutoConfig;
#endif

@Mod(Ipla.MOD_ID)
public final class IplaForge {
    public IplaForge() {
        EventBuses.registerModEventBus(Ipla.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        #if MC_VER > V1_18_2
        // Initialization
        Ipla.initializeServer();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Ipla::initializeClient);

        #if MC_VER >= V1_19_2 && MC_VER < V1_20_4
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(
                                (client, screen) -> MidnightConfig.getScreen(screen, Ipla.MOD_ID)
                        )
                )
        );
        #endif
        #endif
    }
    #if MC_VER <= V1_18_2
    private void onCommonSetup(FMLCommonSetupEvent event) {
        // Initialization
        Ipla.initializeServer();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Ipla::initializeClient);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigGuiHandler.ConfigGuiFactory.class,
                        () -> new ConfigGuiHandler.ConfigGuiFactory(
                                (client, screen) -> AutoConfig.getConfigScreen(IplaConfig.class, screen).get()
                        )
                )
        );
    }
    #endif
}

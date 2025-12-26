package com.akicater.forge;


import com.akicater.IPLA;
import com.akicater.client.screen.IPLA_ConfigScreenBase;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
#if MC_VER >= V1_19_2 && MC_VER < V1_20_4
import net.minecraftforge.client.ConfigScreenHandler;
#else
import net.minecraftforge.client.ConfigGuiHandler;
#endif
#if MC_VER >= V1_19_4

#else

#endif
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(IPLA.MOD_ID)
public final class IplaForge {
    public IplaForge() {
        EventBuses.registerModEventBus(IPLA.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        IPLA.initializeServer();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> IPLA::initializeClient);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModLoadingContext.get().registerExtensionPoint(
                        #if MC_VER >= V1_19_2 ConfigScreenHandler.ConfigScreenFactory.class #else ConfigGuiHandler.ConfigGuiFactory.class #endif,
                        () -> new #if MC_VER >= V1_19_2 ConfigScreenHandler.ConfigScreenFactory #else ConfigGuiHandler.ConfigGuiFactory #endif(
                                (client, screen) -> new IPLA_ConfigScreenBase(screen)
                        )
                )
        );
    }
}

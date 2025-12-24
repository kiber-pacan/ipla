package com.akicater.forge;


import com.akicater.IPLA;
import com.akicater.client.screen.IPLA_ConfigScreenBase;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(IPLA.MOD_ID)
public final class IplaForge {
    public IplaForge(FMLJavaModLoadingContext context) {
        EventBuses.registerModEventBus(IPLA.MOD_ID, context.getModEventBus());

        #if MC_VER > V1_18_2
        // Initialization
        IPLA.initializeServer();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> IPLA::initializeClient);

        #if MC_VER >= V1_19_2 && MC_VER < V1_20_4
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                context.registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(
                                (client, screen) -> new IPLA_ConfigScreenBase(screen)
                        )
                )
        );
        #endif
        #endif
    }
    #if MC_VER <= V1_18_2
    private void onCommonSetup(FMLCommonSetupEvent event) {
        // Initialization
        IPLA.initializeServer();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> IPLA::initializeClient);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigGuiHandler.ConfigGuiFactory.class,
                        () -> new ConfigGuiHandler.ConfigGuiFactory(
                                (client, screen) -> new IPLA_ConfigScreen(screen)
                        )
                )
        );
    }
    #endif
}

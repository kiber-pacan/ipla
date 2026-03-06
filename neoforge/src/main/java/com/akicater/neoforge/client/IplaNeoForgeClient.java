package com.akicater.neoforge.client;

import com.akicater.IPLA_Client;
import com.akicater.client.screen.IPLA_ConfigScreenBase;
import net.neoforged.fml.ModLoadingContext;

#if MC_VER < V1_21
import net.neoforged.neoforge.client.ConfigScreenHandler;
import com.akicater.client.screen.IPLA_ConfigScreen; // Добавил твой класс для старых версий
#else
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
#endif

public class IplaNeoForgeClient {
    public static void init() {
        IPLA_Client.initializeClient();

        ModLoadingContext.get().registerExtensionPoint(
                #if MC_VER >= V1_21
                IConfigScreenFactory.class,
                () -> (client, parent) -> new IPLA_ConfigScreenBase(parent)
                #else
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                (client, parent) -> new IPLA_ConfigScreen(parent))
                #endif
        );
    }
}
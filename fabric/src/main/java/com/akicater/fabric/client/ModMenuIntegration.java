package com.akicater.fabric.client;


import com.akicater.client.screen.IPLA_ConfigScreenBase;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;


public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new IPLA_ConfigScreenBase(screen);
    }
}

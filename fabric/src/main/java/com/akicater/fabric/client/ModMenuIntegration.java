package com.akicater.fabric.client;


import com.akicater.client.IPLA_Config;
import com.akicater.client.IPLA_ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

#if MC_VER < V1_20_4 && MC_VER > V1_18_2
import eu.midnightdust.lib.config.MidnightConfig;
#elif MC_VER <= V1_18_2
import me.shedaniel.autoconfig.AutoConfig;
#endif

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new IPLA_ConfigScreen(screen);
    }
}

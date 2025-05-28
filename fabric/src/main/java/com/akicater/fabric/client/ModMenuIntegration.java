package com.akicater.fabric.client;


import com.akicater.Ipla;
import com.akicater.client.IplaConfig;
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
        #if MC_VER >= V1_20_4
        return screen -> IplaConfig.HANDLER.instance().getScreen(screen);
        #elif MC_VER < V1_20_4 && MC_VER > V1_18_2
        return screen -> MidnightConfig.getScreen(screen, Ipla.MOD_ID);
        #elif  MC_VER <= V1_18_2
        return screen -> AutoConfig.getConfigScreen(IplaConfig.class, screen).get();
        #endif
    }
}

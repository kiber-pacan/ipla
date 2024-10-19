package com.akicater.neoforge;

import com.akicater.client.IplaConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import com.akicater.Ipla;

#if MC_VER < V1_21
import net.neoforged.neoforge.client.ConfigScreenHandler;
#else
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
#endif

#if MC_VER >= V1_20_4
import net.neoforged.fml.loading.FMLEnvironment;
#endif


@Mod(Ipla.MOD_ID)
public final class IplaNeoForge {
    public IplaNeoForge() {
        Ipla.initializeServer();

            #if MC_VER >= V1_20_4
            if (FMLEnvironment.dist.isClient()) {
                Ipla.initializeClient();
            }
            #else
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Ipla::initializeClient);
            #endif


            #if MC_VER == V1_20_4
            if (FMLEnvironment.dist.isClient()) {
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(
                                (client, parent) -> IplaConfig.HANDLER.instance().getScreen(parent)
                        )
                );
            }
            #elif MC_VER >= V1_21
            if (FMLEnvironment.dist.isClient()) {
                ModLoadingContext.get().registerExtensionPoint(
                        IConfigScreenFactory.class,
                        () -> (client, parent) -> IplaConfig.HANDLER.instance().getScreen(parent));
            }
            #endif
    }
}

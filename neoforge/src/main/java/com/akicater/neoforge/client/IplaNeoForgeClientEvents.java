package com.akicater.neoforge.client;

import com.akicater.Ipla;
import com.akicater.client.IplaConfig;
import com.akicater.client.LayingItemBER_common;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
#if MC_VER >= V1_21
import net.neoforged.fml.common.EventBusSubscriber;
#else
import net.neoforged.fml.common.Mod;
#endif

import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@#if MC_VER < V1_21 Mod. #endif EventBusSubscriber(bus = #if MC_VER >= V1_21 EventBusSubscriber #else Mod.EventBusSubscriber #endif.Bus.MOD, modid = Ipla.MOD_ID)
public class IplaNeoForgeClientEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Ipla.lItemBlockEntity.get(),
                LayingItemBER_common::new
        );
    }
}

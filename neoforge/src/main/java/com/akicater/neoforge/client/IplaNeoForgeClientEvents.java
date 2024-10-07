package com.akicater.neoforge.client;

import com.akicater.Ipla;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Ipla.MOD_ID)
public class IplaNeoForgeClientEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Ipla.lItemBlockEntity.get(),
                context -> new LayingItemBER_neoforge(context)
        );
    }
}

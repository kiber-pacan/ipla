package com.akicater.forge.client;

import com.akicater.Ipla;
import com.akicater.client.LayingItemBER_common;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ipla.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IplaForgeClientEvents {
    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Ipla.lItemBlockEntity.get(), LayingItemBER_common::new);
    }
}
package com.akicater.neoforge;

import com.akicater.neoforge.client.LayingItemBER_neoforge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import com.akicater.Ipla;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(Ipla.MOD_ID)
public final class IplaNeoForge {
    public IplaNeoForge() {
        Ipla.initializeServer();

        if (Dist.CLIENT.isClient()) {
            Ipla.initializeClient();
        }
    }
}

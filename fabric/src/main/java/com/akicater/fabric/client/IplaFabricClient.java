package com.akicater.fabric.client;

import com.akicater.IPLA;
import com.akicater.client.LayingItemBER_common;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class IplaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        IPLA.initializeClient();
        #if MC_VER >= V1_19_2
        BlockEntityRenderers.register(IPLA.lItemBlockEntity #if MC_VER < V1_21_3 .get() #endif, LayingItemBER_common::new);
        #else
        BlockEntityRendererRegistry.register(Ipla.lItemBlockEntity.get(), LayingItemBER_common::new);
        #endif
    }
}

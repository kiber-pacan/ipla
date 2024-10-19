package com.akicater.fabric.client;

import com.akicater.Ipla;
import com.akicater.client.LayingItemBER_common;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class IplaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Ipla.initializeClient();
        #if MC_VER >= V1_19_2
        BlockEntityRenderers.register(Ipla.lItemBlockEntity.get(), LayingItemBER_common::new);
        #else
        BlockEntityRendererRegistry.register(Ipla.lItemBlockEntity.get(), LayingItemBER_common::new);
        #endif
    }
}

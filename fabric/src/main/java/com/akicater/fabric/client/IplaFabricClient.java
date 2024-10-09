package com.akicater.fabric.client;

import com.akicater.Ipla;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class IplaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Ipla.initializeClient();
        BlockEntityRenderers.register(Ipla.lItemBlockEntity.get(), LayingItemBER_fabric::new);
    }
}

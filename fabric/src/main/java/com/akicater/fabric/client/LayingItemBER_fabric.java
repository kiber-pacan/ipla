package com.akicater.fabric.client;

import com.akicater.blocks.LayingItemEntity;
import com.akicater.client.LayingItemBER_common;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LayingItemBER_fabric extends LayingItemBER_common {
    public LayingItemBER_fabric(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        this.render(entity, partialTick, poseStack, buffer, packedLight, packedOverlay,1,0.75f,1,false);
    }
}

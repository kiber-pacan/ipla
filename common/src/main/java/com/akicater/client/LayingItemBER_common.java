package com.akicater.client;

import com.akicater.IPLA;
import com.akicater.blocks.LayingItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LayingItemBER_common extends LayingItemBER_abstract_common {
    public LayingItemBER_common(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void #if MC_VER < V1_21_5
                render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
                #else
                render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 cameraPos)
                #endif {
        this.render(entity, partialTick, poseStack, buffer, packedLight, packedOverlay, IPLA.config.itemSize, IPLA.config.blockSize, IPLA.config.absoluteSize, IPLA.config.oldRendering, partialTick);
    }
}


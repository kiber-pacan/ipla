package com.akicater.client;

import com.akicater.IPLA;
import com.akicater.blocks.LayingItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

#if MC_VER >= V1_21_9
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
#elif MC_VER >= V1_21_5
import net.minecraft.world.phys.Vec3;
#endif

public class LayingItemBER_common extends LayingItemBER_abstract_common {
    public LayingItemBER_common(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    #if MC_VER >= V1_21_9
    public void submit(LayingItemBERS renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState)
    #elif MC_VER < V1_21_5
    public void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    #else
    public void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 cameraPos)
    #endif
    {
        #if MC_VER >= V1_21_9
        render(renderState, poseStack, nodeCollector, cameraRenderState, IPLA.config.itemSize, IPLA.config.blockSize, IPLA.config.absoluteSize, IPLA.config.oldRendering);
        #else
        render(entity, partialTick, poseStack, buffer, packedLight, packedOverlay, #if MC_VER >= V1_21_5 cameraPos, #endif IPLA.config.itemSize, IPLA.config.blockSize, IPLA.config.absoluteSize, IPLA.config.oldRendering, partialTick);
        #endif
    }
}


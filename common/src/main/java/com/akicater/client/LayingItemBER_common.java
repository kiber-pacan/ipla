package com.akicater.client;

import com.akicater.Ipla;
import com.akicater.blocks.LayingItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
#if MC_VER > V1_19_2 && MC_VER < V1_20_4
import eu.midnightdust.lib.config.MidnightConfig;
#elif MC_VER <= V1_18_2
import me.shedaniel.autoconfig.AutoConfig;
#endif
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LayingItemBER_common extends LayingItemBER_abstract_common {
    public LayingItemBER_common(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        #if MC_VER >= V1_20_4
            IplaConfig config = IplaConfig.HANDLER.instance();
            this.render(entity, partialTick, poseStack, buffer, packedLight, packedOverlay, config.iSize, config.bSize, config.absSize, config.oldRendering);
        #elif MC_VER <= V1_18_2
            IplaConfig config = AutoConfig.getConfigHolder(IplaConfig.class).getConfig();
            this.render(entity, partialTick, poseStack, buffer, packedLight, packedOverlay, config.iSize, config.bSize, config.absSize, config.oldRendering);
        #else
            this.render(entity, partialTick, poseStack, buffer, packedLight, packedOverlay, IplaConfig.iSize, IplaConfig.bSize, IplaConfig.absSize, IplaConfig.oldRendering);
        #endif
    }
}


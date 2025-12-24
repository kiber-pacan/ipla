package com.akicater.client;

#if MC_VER >= V1_19_4
import com.akicater.IPLA;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Math;
#else
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
#endif

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.akicater.blocks.LayingItemEntity;

#if MC_VER >= V1_21_9
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
#endif

public abstract class #if MC_VER >= V1_21_9 LayingItemBER_abstract_common implements BlockEntityRenderer<LayingItemEntity, LayingItemBERS> #else LayingItemBER_abstract_common implements BlockEntityRenderer<LayingItemEntity> #endif {
    public LayingItemBER_abstract_common(BlockEntityRendererProvider.Context context) {
        #if MC_VER >= V1_21_9 this.itemModelResolver = context.itemModelResolver(); #endif
    }

    #if MC_VER >= V1_19_4
    public static List<Quaternionf> rot = new ArrayList<>(
            List.of(
                    Axis.XP.rotationDegrees(90),    //DOWN
                    Axis.XN.rotationDegrees(90),    //UP
                    Axis.YP.rotationDegrees(180),   //NORTH
                    Axis.YP.rotationDegrees(0),     //SOUTH
                    Axis.YP.rotationDegrees(270),     //WEST
                    Axis.YP.rotationDegrees(90)    //EAST
            )
    );
    #else
    public static List<Quaternion> rot = new ArrayList<>(
            List.of(
                    Vector3f.XP.rotationDegrees(90),    //DOWN
                    Vector3f.XN.rotationDegrees(90),    //UP
                    Vector3f.YP.rotationDegrees(180),   //NORTH
                    Vector3f.YP.rotationDegrees(0),     //SOUTH
                    Vector3f.YP.rotationDegrees(270),     //WEST
                    Vector3f.YP.rotationDegrees(90)    //EAST
            )
    );
    #endif



    public static Vec3 pos1 = new Vec3(0.5F, 0.5F, 0);

    #if MC_VER >= V1_21_9
    private final ItemModelResolver itemModelResolver;


    @Override
    public @NotNull LayingItemBERS createRenderState() {
        return new LayingItemBERS();
    }

    @Override
    public void extractRenderState(LayingItemEntity blockEntity, LayingItemBERS renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress); // ВАЖНО
        // Item rotations
        renderState.rot = blockEntity.rot;
        renderState.lastRot = blockEntity.lastRot;

        // Quad mode for sides
        renderState.quad = blockEntity.quad;

        // Inventory items to IRS
        int j = (int)blockEntity.getBlockPos().asLong();

        renderState.inv = new ArrayList<>(blockEntity.inv.size());
        renderState.isFullBlock = new ArrayList<>(blockEntity.inv.size());

        for(int i = 0; i < blockEntity.inv.size(); ++i) {
            renderState.isFullBlock.add(blockEntity.inv.get(i).getItem() instanceof BlockItem && ((BlockItem) blockEntity.inv.get(i).getItem()).getBlock().defaultBlockState().isCollisionShapeFullBlock(blockEntity.getLevel(), blockEntity.getBlockPos()));
            ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemStackRenderState, blockEntity.inv.get(i), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, i + j);
            renderState.inv.add(itemStackRenderState);
        }
    }
    #endif

    #if MC_VER >= V1_21_9
    public void render(LayingItemBERS entity, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState, float itemSize, float blockSize, float absoluteSize, boolean oldRendering)
    #else
    public void render(#if MC_VER < V1_21_5 LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, #else LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 cameraPos, #endif float itemSize, float blockSize, float absoluteSize, boolean oldRendering, float dt)
    #endif
    {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        for (int s = 0; s < 6; s++) {
            if (entity.quad.get(s)) {
                float iSize = itemSize * absoluteSize / IPLA.config.itemScale;
                float bSize = blockSize * absoluteSize / IPLA.config.blockScale;

                for (int i = 0; i < 4; i++) {
                    if (!entity.inv.get(s * 4 + i).isEmpty()) {
                        #if MC_VER >= V1_21_9
                        ItemStackRenderState irs = entity.inv.get(s * 4 + i); // HAHA IRS
                        #else
                        ItemStack stack = entity.inv.get(s * 4 + i);
                        #endif


                        poseStack.pushPose();

                        boolean fullBlock = #if MC_VER >= V1_21_9 entity.isFullBlock.get(s * 4 + i); #else stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock().defaultBlockState().isCollisionShapeFullBlock(entity.getLevel(), entity.getBlockPos()); #endif
                        manipStack(poseStack, entity, fullBlock, oldRendering, iSize, bSize, s, i, #if MC_VER >= V1_21_9 Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks() * 4 #else dt #endif);

                        if ((fullBlock)) {
                            poseStack.scale(bSize, bSize, bSize);
                        } else {
                            poseStack.scale(iSize, iSize, iSize);
                        }

                        #if MC_VER >= V1_21_9
                        irs.submit(poseStack, nodeCollector, entity.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                        #else
                        itemRenderer.renderStatic(stack, #if MC_VER >= V1_19_4 ItemDisplayContext.FIXED #else ItemTransforms.TransformType.FIXED #endif, packedLight, packedOverlay, poseStack, buffer #if MC_VER >= V1_19_4, entity.getLevel() #endif, 1);
                        #endif

                        poseStack.popPose();
                    }
                }
            } else {
                if (!entity.inv.get(s * 4).isEmpty()) {
                    float iSize = itemSize * absoluteSize;
                    float bSize = blockSize * absoluteSize;

                    #if MC_VER >= V1_21_9
                    ItemStackRenderState irs = entity.inv.get(s * 4); // HAHA IRS
                    #else
                    ItemStack stack = entity.inv.get(s * 4);
                    #endif

                    poseStack.pushPose();

                    boolean fullBlock = #if MC_VER >= V1_21_9 entity.isFullBlock.get(s * 4); #else stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock().defaultBlockState().isCollisionShapeFullBlock(entity.getLevel(), entity.getBlockPos()); #endif
                    manipStack(poseStack, entity, fullBlock, oldRendering, iSize, bSize, s, 4, #if MC_VER >= V1_21_9 Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks() * 4 #else dt #endif);

                    if ((fullBlock)) {
                        poseStack.scale(bSize, bSize, bSize);
                    } else {
                        poseStack.scale(iSize, iSize, iSize);
                    }

                    #if MC_VER >= V1_21_9
                    irs.submit(poseStack, nodeCollector, entity.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                    #else
                    itemRenderer.renderStatic(stack, #if MC_VER >= V1_19_4 ItemDisplayContext.FIXED #else ItemTransforms.TransformType.FIXED #endif, packedLight, packedOverlay, poseStack, buffer #if MC_VER >= V1_19_4, entity.getLevel() #endif, 1);
                    #endif

                    poseStack.popPose();
                }
            }
        }
    }

    float lerp(float a, float b, float f)  {
        return a * (1.0f - f) + (b * f);
    }



    public void manipStack(PoseStack poseStack, #if MC_VER >= V1_21_9 LayingItemBERS #else LayingItemEntity #endif entity, boolean fullBlock, boolean oldRendering, float iSize, float bSize, int s, int i, float dt) {
        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(rot.get(s));

        boolean quad = i < 4;
        int x = s * 4 + ((quad) ? i : 0);

        float rotation = #if MC_VER >= V1_19_4 Math.lerp #else lerp #endif(entity.lastRot.get(x), entity.rot.get(x), 0.1f * dt);

        if (fullBlock && !oldRendering) {
            poseStack.translate(-0.5, -0.5, -0.5);

            if (quad) {
                poseStack.translate(0.25f + (((i + 1) % 2 == 0) ? 0.5f : 0), 0.25f + ((i > 1) ? 0.5f : 0), 0.25f * bSize);
            } else {
                poseStack.translate(pos1.x, pos1.y, 0.25f * bSize);
            }

            #if MC_VER >= V1_19_4
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            #else
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(rotation));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
            #endif
        } else {
            poseStack.translate(-0.5, -0.5, -0.5);

            if (quad) {
                poseStack.translate(0.25f + (((i + 1) % 2 == 0) ? 0.5f : 0), 0.25f + ((i > 1) ? 0.5f : 0), 0.03125f * iSize);
            } else {
                poseStack.translate(pos1.x, pos1.y, 0.03125f * iSize);
            }

            #if MC_VER >= V1_19_4
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            #else
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(rotation));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
            #endif
        }

        entity.lastRot.set(x, rotation);
    }


    #if MC_VER >= V1_21_9
    public abstract void submit(LayingItemBERS renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState);
    #elif MC_VER < V1_21_5
    public abstract void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
    #else
    public abstract void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 cameraPos);
    #endif
}


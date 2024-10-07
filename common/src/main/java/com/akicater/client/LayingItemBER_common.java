package com.akicater.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import com.akicater.blocks.LayingItemEntity;
import org.joml.Vector3f;

public abstract class LayingItemBER_common implements BlockEntityRenderer<LayingItemEntity> {
    public LayingItemBER_common(BlockEntityRendererProvider.Context context) {

    }

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

    public static Vec3 pos1 = new Vec3(0.5F, 0.5F, 0);

    public void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, float absoluteSize, float blockSize, float itemSize, boolean oldRendering) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        float iSize = itemSize * absoluteSize / 2;
        float bSize = blockSize * absoluteSize / 2;

        for (int s = 0; s < 6; s++) {
            if (entity.quad.get(s)) {
                for (int i = 0; i < 4; i++) {
                    if (!entity.inv.get(s * 4 + i).isEmpty()) {
                        ItemStack stack = entity.inv.get(s * 4 + i);

                        poseStack.pushPose();

                        boolean fullBlock = stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock().defaultBlockState().isCollisionShapeFullBlock(entity.getLevel(), entity.getBlockPos());
                        manipStack(poseStack, entity, fullBlock, oldRendering, bSize, iSize, s, i);

                        if ((fullBlock)) {
                            poseStack.scale(bSize, bSize, bSize);
                        } else {
                            poseStack.scale(iSize, iSize, iSize);
                        }

                        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, entity.getLevel(), 1);

                        poseStack.popPose();
                    }
                }
            } else {
                if (!entity.inv.get(s * 4).isEmpty()) {
                    ItemStack stack = entity.inv.get(s * 4);

                    poseStack.pushPose();

                    boolean fullBlock = stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock().defaultBlockState().isCollisionShapeFullBlock(entity.getLevel(), entity.getBlockPos());
                    manipStack(poseStack, entity, fullBlock, oldRendering, bSize, itemSize, s, 4);

                    if ((fullBlock)) {
                        poseStack.scale(blockSize, blockSize, blockSize);
                    } else {
                        poseStack.scale(itemSize, itemSize, itemSize);
                    }

                    itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, entity.getLevel(), 1);

                    poseStack.popPose();
                }
            }
        }
    }


    public void manipStack(PoseStack poseStack, LayingItemEntity entity, boolean fullBlock, boolean oldRendering, float bSize, float iSize, int s, int i) {
        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(rot.get(s));

        boolean quad = i < 4;
        int x = s * 4 + ((quad) ? i : 0);

        float rotation = Math.lerp(entity.lastRot.get(x), entity.rot.get(x), 0.1f);

        if (fullBlock && !oldRendering) {
            poseStack.translate(-0.5, -0.5, -0.5);

            if (quad) {
                poseStack.translate(0.25f + (((i + 1) % 2 == 0) ? 0.5f : 0), 0.25f + ((i > 1) ? 0.5f : 0), 0.25f * bSize);
            } else {
                poseStack.translate(pos1.x, pos1.y, 0.5f * bSize);
            }

            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
        } else {
            poseStack.translate(-0.5, -0.5, -0.5);

            if (quad) {
                poseStack.translate(0.25f + (((i + 1) % 2 == 0) ? 0.5f : 0), 0.25f + ((i > 1) ? 0.5f : 0), 0.025f * iSize);
            } else {
                poseStack.translate(pos1.x, pos1.y, 0.025f * iSize);
            }

            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }

        entity.lastRot.set(x, rotation);
    }

    public abstract void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
}


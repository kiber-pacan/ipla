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

    public static Quaternionf rotateZ(float angle, Quaternionf dest) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return new Quaternionf(dest.x * cos + dest.y * sin,
                dest.y * cos - dest.x * sin,
                dest.w * sin + dest.z * cos,
                dest.w * cos - dest.z * sin);
    }

    public static Quaternionf rotateX(float angle, Quaternionf dest) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return new Quaternionf(dest.w * sin + dest.x * cos,
                dest.y * cos + dest.z * sin,
                dest.z * cos - dest.y * sin,
                dest.w * cos - dest.x * sin);
    }

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
                        manipStack(poseStack, entity, fullBlock, oldRendering, iSize, s, i);

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
                    manipStack(poseStack, entity, fullBlock, oldRendering, itemSize, s, 4);

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


    public static void manipStack(PoseStack poseStack, LayingItemEntity entity, boolean fullBlock, boolean oldRendering, float bSize, int s, int i) {
        // If full block and old rendering disabled rotate 90 degrees
        poseStack.translate(0.5, 0, 0.5);

        poseStack.mulPose(rotateZ(Math.toRadians(entity.rot.get(s * 4)), rot.get(s)));

        if (fullBlock && !oldRendering) {
            poseStack.translate(-0.5, 0, -0.5);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            if (i < 4) {
                poseStack.translate(0.25f + (((i + 1) % 2 == 0) ? 0.5f : 0), 0.25 * bSize, -(0.25f + ((i > 1) ? 0.5f : 0)));
            } else {
                poseStack.translate(pos1.x, 0.25 * bSize, -pos1.y);
            }
        } else {
            poseStack.translate(-0.5, 0, -0.5);
            if (i < 4) {
                poseStack.translate(0.25f + (((i + 1) % 2 == 0) ? 0.5f : 0), 0.25f + ((i > 1) ? 0.5f : 0), 0);
            } else {
                poseStack.translate(pos1.x, pos1.y, 0);
            }
        }
    }

    public abstract void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
}


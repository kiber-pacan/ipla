package com.akicater.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import com.akicater.blocks.LayingItemEntity;
import org.joml.Vector3f;

public abstract class layingItemBER implements BlockEntityRenderer<LayingItemEntity> {
    public static List<Quaternionf> list = new ArrayList<>(
            List.of(
                    Axis.XP.rotationDegrees(90),    //DOWN
                    Axis.XN.rotationDegrees(90),    //UP
                    Axis.YP.rotationDegrees(180),   //NORTH
                    Axis.XP.rotationDegrees(0),     //SOUTH
                    Axis.YN.rotationDegrees(90),     //WEST
                    Axis.YP.rotationDegrees(90)    //EAST
            )
    );

    public static List<Vector3f> pos = new ArrayList<>(
            List.of(
                    new Vector3f(0.5F, 0.025F, 0.5F),
                    new Vector3f(0.5F, 0.975F, 0.5F),
                    new Vector3f(0.5F, 0.5F, 0.025F),
                    new Vector3f(0.5F, 0.5F, 0.975F),
                    new Vector3f(0.025F, 0.5F, 0.5F),
                    new Vector3f(0.975F, 0.5F, 0.5F)
            )
    );


    static int getLight(Level level, BlockPos pos) {
        return LightTexture.pack(LightTexture.block(level.getLightEmission(pos)), LightTexture.sky(level.getLightEmission(pos)));
    }

    public Quaternionf rotateZ(float angle, Quaternionf dest) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return new Quaternionf(dest.x * cos + dest.y * sin,
                dest.y * cos - dest.x * sin,
                dest.w * sin + dest.z * cos,
                dest.w * cos - dest.z * sin);
    }

    public Quaternionf rotateX(float angle, Quaternionf dest) {
        float sin = Math.sin(angle * 0.5f);
        float cos = Math.cosFromSin(sin, angle * 0.5f);
        return new Quaternionf(dest.w * sin + dest.x * cos,
                dest.y * cos + dest.z * sin,
                dest.z * cos - dest.y * sin,
                dest.w * cos - dest.x * sin);
    }

    public void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, float absoluteSize, float blockSize, float itemSize, boolean oldRendering) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        int x = getLight(entity.getLevel(), entity.getBlockPos());

        for (int i = 0; i < 6; i++) {
            if (!entity.items.get(i).isEmpty()) {
                ItemStack stack = entity.items.get(i);

                poseStack.pushPose();

                poseStack.translate(pos.get(i).x, pos.get(i).y, pos.get(i).z);

                // Differentiate item and block rendering
                if (Minecraft.getInstance().getItemRenderer().getModel(stack, entity.getLevel(), null, 1).usesBlockLight()) {
                    // Differentiate new and old block rendering
                    if (!oldRendering) {
                        poseStack.mulPose(rotateX(Math.toRadians(-90), rotateZ(Math.toRadians(entity.rot.get(i)), list.get(i))));
                        poseStack.translate(0, 0.25 * blockSize - 0.025, 0);
                    } else {
                        poseStack.mulPose(rotateZ(Math.toRadians(entity.rot.get(i)), list.get(i)));
                    }
                    poseStack.scale(blockSize, blockSize, blockSize);
                } else {
                    poseStack.scale(itemSize, itemSize, itemSize);
                    poseStack.mulPose(rotateZ(Math.toRadians(entity.rot.get(i)), list.get(i)));
                }

                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, x, packedOverlay, poseStack, buffer, entity.getLevel(), 1);

                poseStack.popPose();
            }
        }
    }
}


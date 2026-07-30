package com.akicater.client;

import com.akicater.IPLA_Methods;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

#if MC_VER < V26_2
import net.minecraft.client.renderer.MultiBufferSource;
#endif

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.color.block.BlockColors;


import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.akicater.IPLA_Client;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.random.RandomGenerator;

#if MC_VER >= V1_21_9
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
#if MC_VER < V26_1
import net.minecraft.client.renderer.state.CameraRenderState;
#else
import net.minecraft.client.renderer.state.level.CameraRenderState;
#endif
#endif

#if MC_VER >= V1_19_2
import net.minecraft.util.RandomSource;
#endif

#if MC_VER >= V1_19_4
import org.joml.Quaternionf;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
#else
import net.minecraft.client.renderer.block.model.ItemTransforms;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
#endif

#if MC_VER < V1_21_5
import net.minecraft.client.resources.model.BakedModel;
#else

#if MC_VER >= V26_1
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
#else
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
#endif

#endif


#if MC_VER >= V26_1
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import com.mojang.blaze3d.vertex.QuadInstance;
#else
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
#endif

#if MC_VER >= V1_21_11
import net.minecraft.client.renderer.rendertype.RenderTypes;
#else
import net.minecraft.client.renderer.RenderType;
#endif

import com.akicater.blocks.LayingItemEntity;

public abstract class #if MC_VER >= V1_21_9 LayingItemBER_abstract_common implements BlockEntityRenderer<LayingItemEntity, LayingItemBERS> #else LayingItemBER_abstract_common implements BlockEntityRenderer<LayingItemEntity> #endif {
    public LayingItemBER_abstract_common(BlockEntityRendererProvider.Context context) {
        #if MC_VER >= V1_21_9 this.itemModelResolver = context.itemModelResolver(); #endif

        #if MC_VER >= V1_19_2
        random = RandomSource.create();
        #else
        random = new Random();
        #endif
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
                    Vector3f.XP.rotationDegrees(-90),    //UP
                    Vector3f.YP.rotationDegrees(180),   //NORTH
                    Vector3f.YP.rotationDegrees(0),     //SOUTH
                    Vector3f.YP.rotationDegrees(270),     //WEST
                    Vector3f.YP.rotationDegrees(90)    //EAST
            )
    );
    #endif

    float margin = 0.0001f;
    public static Vec3 pos1 = new Vec3(0.5F, 0.5F, 0);
    #if MC_VER >= V1_19_2
    private static RandomSource random;
    #else
    private static Random random;
    #endif

    #if MC_VER >= V1_21_9
    private final ItemModelResolver itemModelResolver;


    @Override
    public @NotNull LayingItemBERS createRenderState() {
        return new LayingItemBERS();
    }

    @Override
    public void extractRenderState(LayingItemEntity entity, LayingItemBERS renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, renderState, partialTick, cameraPosition, breakProgress);
        // Item rotations
        renderState.rot = entity.rot;
        renderState.lastRot = entity.lastRot;

        // Quad mode for sides
        renderState.quad = entity.quad;

        // Inventory items to IRS
        int j = (int)entity.getBlockPos().asLong();

        renderState.inv = new ArrayList<>(entity.inv.size());
        renderState.items = new ArrayList<>(entity.inv.size());
        renderState.renderAsBlock = new ArrayList<>(entity.inv.size());

        renderState.blockPos = entity.getBlockPos();
        renderState.level = entity.getLevel();

        for(int i = 0; i < entity.inv.size(); ++i) {
            renderState.renderAsBlock.add(IPLA_Methods.renderAsBlock(entity.inv.get(i).getItem()));
            ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemStackRenderState, entity.inv.get(i), ItemDisplayContext.FIXED, entity.getLevel(), Minecraft.getInstance().player, i + j);
            renderState.inv.add(itemStackRenderState);
            renderState.items.add(entity.inv.get(i).getItem());
        }
    }
    #endif


    #if MC_VER >= V1_21_9
    public void render(LayingItemBERS entity, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState, float itemSize, float blockSize, float absoluteSize, boolean oldRendering)
    #else
    public void render(#if MC_VER < V1_21_5 LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, #else LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 cameraPos, #endif float itemSize, float blockSize, float absoluteSize, boolean oldRendering, float dt)
    #endif
    {
        #if MC_VER >= V26_1
        ItemModelResolver itemRenderer = Minecraft.getInstance().getItemModelResolver();
        #else
        #endif
        for (int slot = 0; slot < 6; slot++) {
            if (entity.quad.get(slot)) {
                for (int i = 0; i < 4; i++) {
                    if (!entity.inv.get(slot * 4 + i).isEmpty()) {
                        #if MC_VER >= V1_21_9 ItemStackRenderState irs #else ItemStack stack #endif = entity.inv.get(slot * 4 + i); // HAHA IRS


                        boolean renderAsBlock = #if MC_VER >= V1_21_9 entity.renderAsBlock.get(slot * 4 + i) #else IPLA_Methods.renderAsBlock(stack.getItem()) #endif;

                        float size = (renderAsBlock) ? (blockSize * absoluteSize / 2) : (itemSize * absoluteSize / IPLA_Client.config.itemScale);

                        poseStack.pushPose(); // START

                        List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> transforms =  manipStack(poseStack, entity, renderAsBlock, oldRendering, size, slot, i, #if MC_VER >= V1_21_9 Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks() * 4 #else dt #endif);

                        poseStack.scale(size, size, size);

                        #if MC_VER >= V1_21_9
                        drawStack(entity, entity.items.get(slot * 4 + i), transforms, renderAsBlock, poseStack, nodeCollector, irs);
                        #else
                        drawStack(stack, transforms, renderAsBlock, poseStack, buffer, packedLight, packedOverlay, entity);
                        #endif

                        poseStack.popPose(); // END
                    }
                }
            } else {
                if (!entity.inv.get(slot * 4).isEmpty()) {
                    #if MC_VER >= V1_21_9
                    ItemStackRenderState irs = entity.inv.get(slot * 4); // HAHA IRS
                    #else
                    ItemStack stack = entity.inv.get(slot * 4);
                    #endif

                    boolean renderAsBlock = #if MC_VER >= V1_21_9 entity.renderAsBlock.get(slot * 4) #else IPLA_Methods.renderAsBlock(stack.getItem()) #endif;

                    float size = (renderAsBlock) ? blockSize * absoluteSize / 2 : itemSize * absoluteSize;

                    poseStack.pushPose(); // START

                    List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> transforms = manipStack(poseStack, entity, renderAsBlock, oldRendering, size, slot, 4, #if MC_VER >= V1_21_9 Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks() * 4 #else dt #endif);

                    poseStack.scale(size, size, size);

                    #if MC_VER >= V1_21_9
                    drawStack(entity, entity.items.get(slot * 4), transforms, renderAsBlock, poseStack, nodeCollector, irs);
                    #else
                    drawStack(stack, transforms, renderAsBlock, poseStack, buffer, packedLight, packedOverlay, entity);
                    #endif

                    poseStack.popPose(); // END
                }
            }
        }
    }

    void mul(PoseStack poseStack, List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> transforms, #if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif quaternion) {
        transforms.add(quaternion);
        poseStack.mulPose(transforms.get(transforms.size() - 1));
    }

    void mul(PoseStack poseStack, List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> transforms, int index, float degrees) {
        switch (index) {
            case -3: {
                transforms.add(#if MC_VER >= V1_19_4 Axis #else Vector3f #endif .ZN.rotationDegrees(degrees));
                poseStack.mulPose(transforms.get(transforms.size() - 1));
                break;
            }
            case -2: {
                transforms.add(#if MC_VER >= V1_19_4 Axis #else Vector3f #endif .YN.rotationDegrees(degrees));
                poseStack.mulPose(transforms.get(transforms.size() - 1));

                break;
            }
            case -1: {
                transforms.add(#if MC_VER >= V1_19_4 Axis #else Vector3f #endif .XN.rotationDegrees(degrees));
                poseStack.mulPose(transforms.get(transforms.size() - 1));
                break;
            }
            case 1: {
                transforms.add(#if MC_VER >= V1_19_4 Axis #else Vector3f #endif .XP.rotationDegrees(degrees));
                poseStack.mulPose(transforms.get(transforms.size() - 1));
                break;
            }
            case 2: {
                transforms.add(#if MC_VER >= V1_19_4 Axis #else Vector3f #endif .YP.rotationDegrees(degrees));
                poseStack.mulPose(transforms.get(transforms.size() - 1));
                break;
            }
            case 3: {
                transforms.add(#if MC_VER >= V1_19_4 Axis #else Vector3f #endif .ZP.rotationDegrees(degrees));
                poseStack.mulPose(transforms.get(transforms.size() - 1));
                break;
            }
        }
    }

    public List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> manipStack(PoseStack poseStack, #if MC_VER >= V1_21_9 LayingItemBERS #else LayingItemEntity #endif entity, boolean fullBlock, boolean oldRendering, float size, int slot, int i, float delta) {
        boolean quad = i < 4;
        int x = slot * 4 + ((quad) ? i : 0);

        float degrees = Mth.lerp(0.1f * delta, entity.lastRot.get(x), entity.rot.get(x));
        List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> transforms = new ArrayList<>();

        if (fullBlock && !oldRendering) {
            poseStack.translate(0.5, 0.5, 0.5);
            mul(poseStack, transforms, rot.get(slot));

            if (quad) {
                poseStack.translate((i + 1) % 2 == 0 ? 0.25f : -0.25f, i > 1 ? 0.25f : -0.25f, 0);
            }
            poseStack.translate(0, 0, -0.5);
            poseStack.translate(0, 0, 0.25);

            mul(poseStack, transforms, 3, degrees);
            mul(poseStack, transforms, 1, 90);

            poseStack.translate(-0.25, -0.25, -0.25);
        } else {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(rot.get(slot));
            poseStack.translate(-0.5, -0.5, -0.5);

            if (quad) {
                poseStack.translate(0.25 + (((i + 1) % 2 == 0) ? 0.5f : 0), 0.25 + ((i > 1) ? 0.5 : 0), 0.03125 * size - margin);
            } else {
                poseStack.translate(pos1.x, pos1.y, 0.03125 * size - 0.0001 - margin);
            }

            poseStack.mulPose(#if MC_VER >= V1_19_4 Axis #else Vector3f #endif .ZP.rotationDegrees(degrees));
            poseStack.mulPose(#if MC_VER >= V1_19_4 Axis #else Vector3f #endif .ZP.rotationDegrees(180));
        }


        entity.lastRot.set(x, degrees);

        return transforms;
    }

    public static #if MC_VER <= V1_21_11 float[] #else int #endif getShadedColor(float baseR, float baseG, float baseB, Vector3f normal) {
        float nx = normal.x();
        float ny = normal.y();
        float nz = normal.z();

        // nx2 + ny2 + nz2 = 1
        float nx2 = nx * nx;
        float ny2 = ny * ny;
        float nz2 = nz * nz;

        float factor;
        if (ny > 0) {
            factor = ny2 + nz2 * 0.8f + nx2 * 0.6f;
        } else {
            factor = ny2 * 0.5f + nz2 * 0.8f + nx2 * 0.6f;
        }

        return #if MC_VER <= V1_21_11 new float[]{baseR * factor, baseG * factor, baseB * factor} #else (255 << 24) | (((int) (baseR * factor * 255.0f)) << 16) | (((int) (baseG * factor * 255.0f)) << 8) | ((int) (baseB * factor * 255.0f)) #endif;
    }

    #if MC_VER >= V1_21_9
    void drawStack(LayingItemBERS entity, Item item, List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> transforms, boolean renderAsBlock, PoseStack poseStack, SubmitNodeCollector nodeCollector, ItemStackRenderState irs) {
        if (renderAsBlock) {
            Block block = ((BlockItem) item).getBlock();
            #if MC_VER >= V26_1
            ModelManager manager = Minecraft.getInstance().getModelManager();
            BlockStateModel model = manager.getBlockStateModelSet().get(block.defaultBlockState());
            BlockColors colors = Minecraft.getInstance().getBlockColors();
            BlockTintSource tint = colors.getTintSource(block.defaultBlockState(), 0);
            int color = 16777215;
            if (tint != null) {
                color = tint.color(block.defaultBlockState());
            }
            #else
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            BlockStateModel model = blockRenderer.getBlockModel(block.defaultBlockState());
            BlockColors colors = Minecraft.getInstance().getBlockColors();
            int color = colors.getColor(block.defaultBlockState(), entity.level, entity.pos, 0);
            #endif

            renderModel(
                    poseStack,
                    transforms,
                    nodeCollector,
                    model,
                    ((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f,
                    entity.lightCoords,
                    0,
                    block.defaultBlockState().getLightEmission() > 0
                    #if MC_VER < V1_21_5 , state #endif
            );

        } else {
            irs.submit(poseStack, nodeCollector, entity.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
    }
    #else
    void drawStack(ItemStack stack, List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> transforms, boolean renderAsBlock, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, LayingItemEntity entity) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        if (renderAsBlock) {
            Block block = ((BlockItem) stack.getItem()).getBlock();
            BlockState state = block.defaultBlockState();
            #if MC_VER >= V1_21_5 BlockStateModel #else BakedModel #endif model = blockRenderer.getBlockModel(block.defaultBlockState());
            BlockColors colors = Minecraft.getInstance().getBlockColors();
            int color = colors.getColor(block.defaultBlockState(), entity.getLevel(), entity.getBlockPos(), 0);

            renderModel(
                    poseStack,
                    transforms,
                    buffer.getBuffer(RenderType.cutoutMipped()),
                    model,
                    ((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f,
                    packedLight,
                    packedOverlay,
                    state.getLightEmission() > 0
                    #if MC_VER < V1_21_5 , state #endif
            );
        } else {
            itemRenderer.renderStatic(
                    stack,
                    #if MC_VER >= V1_19_4 ItemDisplayContext.FIXED #else ItemTransforms.TransformType.FIXED #endif,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    #if MC_VER >= V1_19_4 entity.getLevel(), #endif
                    1
            );
        }
    }
    #endif


    static final Direction[] DIRECTIONS = Direction.values();

    public void renderModel(PoseStack poseStack, List<#if MC_VER >= V1_19_4 Quaternionf #else Quaternion #endif> transforms, #if MC_VER >= V1_21_9 SubmitNodeCollector nodeCollector, #else VertexConsumer consumer, #endif #if MC_VER >= V1_21_5 BlockStateModel #else BakedModel #endif model, float red, float green, float blue, int packedLight, int packedOverlay, boolean emissive #if MC_VER < V1_21_5 , BlockState state #endif) {
        Vector3f normal = null;

        #if MC_VER >= V26_1
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);
        #endif
        random.setSeed(42);
        #if MC_VER >= V1_21_5 for(#if MC_VER >= V26_1 BlockStateModelPart #else BlockModelPart #endif blockModelPart : #if MC_VER >= V26_1 parts #else model.collectParts(random) #endif) { #endif

            for (Direction direction : DIRECTIONS) {
                normal = direction.step();
                normal.normalize();

                for (int i = transforms.size() - 1; i >= 0; i--) {
                    normal.#if MC_VER >= V1_19_4 rotate #else transform #endif(transforms.get(i));
                }



                #if MC_VER <= V1_21_11 float[] #else int #endif rgb_tint = (emissive) ? #if MC_VER <= V1_21_11 new float[]{1,1,1} #else (255 << 24) | (((int) (red * 255.0f)) << 16) | (((int) (green * 255.0f)) << 8) | ((int) (blue * 255.0f))  #endif : getShadedColor(red, green, blue, normal);
                #if MC_VER <= V1_21_11 float[] #else int #endif rgb = (emissive) ? #if MC_VER <= V1_21_11 new float[]{1,1,1} #else (255 << 24) | (255 << 16) | (255 << 8) | (255) #endif : getShadedColor(1, 1, 1, normal);



                #if MC_VER >= V1_21_9
                nodeCollector.submitCustomGeometry(
                        poseStack,
                        #if MC_VER >= V1_21_11 RenderTypes.cutoutMovingBlock() #else RenderType.cutout() #endif,
                        (pose, vertexConsumer) -> {
                            for (BakedQuad quad : blockModelPart.getQuads(direction)) {
                                #if MC_VER >= V26_1

                                QuadInstance quadInstance = new QuadInstance();

                                quadInstance.setColor((quad.materialInfo().isTinted()) ? rgb_tint : rgb);
                                quadInstance.setLightCoords(packedLight);

                                vertexConsumer.putBakedQuad(pose, quad, quadInstance);
                                #else
                                if (quad.isTinted()) {
                                    vertexConsumer.putBulkData(
                                            pose,
                                            quad,
                                            rgb_tint[0], rgb_tint[1], rgb_tint[2], 1f,
                                            packedLight,
                                            OverlayTexture.NO_OVERLAY
                                    );
                                } else {
                                    vertexConsumer.putBulkData(
                                            pose,
                                            quad,
                                            rgb[0], rgb[1], rgb[2], 1f,
                                            packedLight,
                                            OverlayTexture.NO_OVERLAY
                                    );
                                }

                                #endif
                            }
                        }
                );
                #else
                random.setSeed(42);
                renderQuadList(normal, poseStack, consumer, red, green, blue, #if MC_VER >= V1_21_5 blockModelPart #else model #endif.getQuads(#if MC_VER < V1_21_5 state, direction, random #else direction #endif), packedLight, packedOverlay, emissive);
                #endif
            }

            #if MC_VER >= V1_21_9
            List<BakedQuad> generalQuads = blockModelPart.getQuads(null);
            #if MC_VER <= V1_21_11 float[] #else int #endif rgb_tint = (emissive) ? #if MC_VER <= V1_21_11 new float[]{1,1,1} #else (255 << 24) | (((int) (red * 255.0f)) << 16) | (((int) (green * 255.0f)) << 8) | ((int) (blue * 255.0f))  #endif : getShadedColor(red, green, blue, normal);
            #if MC_VER <= V1_21_11 float[] #else int #endif rgb = (emissive) ? #if MC_VER <= V1_21_11 new float[]{1,1,1} #else (255 << 24) | (255 << 16) | (255 << 8) | (255) #endif : getShadedColor(1, 1, 1, normal);
            if (!generalQuads.isEmpty()) {
                nodeCollector.submitCustomGeometry(
                        poseStack,
                        #if MC_VER >= V1_21_11 RenderTypes.cutoutMovingBlock() #else RenderType.cutout() #endif,
                        (pose, vertexConsumer) -> {
                            for (BakedQuad quad : blockModelPart.getQuads(null)) {
                                #if MC_VER >= V26_1

                                QuadInstance quadInstance = new QuadInstance();

                                quadInstance.setColor((quad.materialInfo().isTinted()) ? rgb_tint : rgb);
                                quadInstance.setLightCoords(packedLight);

                                vertexConsumer.putBakedQuad(pose, quad, quadInstance);
                                #else
                                if (quad.isTinted()) {
                                    vertexConsumer.putBulkData(
                                            pose,
                                            quad,
                                            rgb_tint[0], rgb_tint[1], rgb_tint[2], 1f,
                                            packedLight,
                                            OverlayTexture.NO_OVERLAY
                                    );
                                } else {
                                    vertexConsumer.putBulkData(
                                            pose,
                                            quad,
                                            rgb[0], rgb[1], rgb[2], 1f,
                                            packedLight,
                                            OverlayTexture.NO_OVERLAY
                                    );
                                }
                                #endif
                            }
                        }
                );


            }
            #else
            random.setSeed(42);
            renderQuadList(normal, poseStack, consumer, red, green, blue, #if MC_VER >= V1_21_5 blockModelPart #else model #endif.getQuads(#if MC_VER < V1_21_5 state, null, random #else null #endif), packedLight, packedOverlay, emissive);
            #endif
        #if MC_VER >= V1_21_5 } #endif
    }


    #if MC_VER < V1_21_9
    private static void renderQuadList(Vector3f normal, PoseStack pose, VertexConsumer consumer, float red, float green, float blue, List<BakedQuad> quads, int packedLight, int packedOverlay, boolean emissive) {
        boolean shaded = (quads.size() <= 2) ? false : emissive;

        for(BakedQuad quad : quads) {
            float[] rgb;

            if (quad.isTinted()) {
                rgb = (!shaded) ? new float[]{red, green, blue} : getShadedColor(red, green, blue, normal);
            } else {
                rgb = (!shaded) ? new float[]{1.0f, 1.0f, 1.0f} : getShadedColor(1.0f, 1.0f, 1.0f, normal);
            }

            consumer.putBulkData(pose.last(), quad, rgb[0], rgb[1], rgb[2], #if MC_VER >= V1_21 1.0f, #endif packedLight, packedOverlay);
        }
    }
    #endif

    #if MC_VER >= V1_21_9
    public abstract void submit(LayingItemBERS renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState);
    #elif MC_VER < V1_21_5
    public abstract void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
    #else
    public abstract void render(LayingItemEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 cameraPos);
    #endif
}
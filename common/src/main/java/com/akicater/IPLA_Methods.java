package com.akicater;

import com.akicater.blocks.LayingItemEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
#if MC_VER >= V1_19_2
import net.minecraft.util.RandomSource;
#else
import java.lang.reflect.Field;
import java.util.*;
#endif
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.*;


public class IPLA_Methods {
    #if MC_VER >= V1_19_2
    static RandomSource random = RandomSource.create();
    #else
    static Random random = new Random();
    #endif

    static void spawnItemParticles(Player player, ItemStack stack, int amount) {
        for (int i = 0; i < amount; i++) {
            Vec3 vec3 = new Vec3((random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3 = vec3.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
            vec3 = vec3.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
            double d = -random.nextFloat() * 0.6 - 0.3;
            Vec3 vec32 = new Vec3((random.nextFloat() - 0.5) * 0.3, d, 0.6);
            vec32 = vec32.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
            vec32 = vec32.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
            vec32 = vec32.add(player.getX(), player.getEyeY(), player.getZ());
            #if MC_VER >= V1_20_1 player.level() #else player.level #endif.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack #if MC_VER >= V26_1 .getItem() #endif), vec32.x, vec32.y, vec32.z, vec3.x, vec3.y + 0.05, vec3.z);
        }
    }

    public static void clearEating(Player player) {
        ((EatingPlayer) player).ipla$setEatingTicks(0);
        ((EatingPlayer) player).ipla$setFoodPos(null);
        ((EatingPlayer) player).ipla$setEating(false);
        ((EatingPlayer) player).ipla$setHit(null);
        ((EatingPlayer) player).ipla$setLayingItemEntity(null);
    }

    public static void clearEatingPlayer(Player player) {
        ((EatingPlayer) player).ipla$setEatingTicks(0);
        ((EatingPlayer) player).ipla$setFoodPos(null);
        ((EatingPlayer) player).ipla$setHit(null);
        ((EatingPlayer) player).ipla$setLayingItemEntity(null);
    }

    public static BlockHitResult getBlockHitResult(HitResult hit) {
        if (hit.getType() == HitResult.Type.BLOCK) {
            return (BlockHitResult) hit;
        }
        return null;
    }

    static final double EPS = 1e-6;

    public static boolean contains(double x, double y, double z, AABB box) {
        return x >= box.minX - EPS && x <= box.maxX + EPS
                && y >= box.minY - EPS && y <= box.maxY + EPS
                && z >= box.minZ - EPS && z <= box.maxZ + EPS;
    }

    public static int getSubSlotFromPos(int slot, double x, double y, double z) {
        switch (slot) {
            case 0, 1 -> {
                return (slot == 1) ? getIndexFromXY(x, 1 - z) : getIndexFromXY(x, z);
            }
            case 2, 3 -> {
                return (slot == 2) ? getIndexFromXY(1 - x, y) : getIndexFromXY(x, y);
            }
            case 4, 5 -> {
                return (slot == 5) ? getIndexFromXY(1 - z, y) : getIndexFromXY(z, y);
            }
        }

        return 0;
    }

    public static Pair<Integer, Boolean> getSlotFromHit(BlockHitResult hitResult, boolean isLayingItem, LayingItemEntity entity, Player player) {
        Vec3 location = hitResult.getLocation();
        Direction direction = hitResult.getDirection();
        Vec3 relativeLocation =
                location.add(
                        direction.getStepX() * 0.249,
                        direction.getStepY() * 0.249,
                        direction.getStepZ() * 0.249
                );

        double x = relativeLocation.x - Math.floor(relativeLocation.x);
        double y = relativeLocation.y - Math.floor(relativeLocation.y);
        double z = relativeLocation.z - Math.floor(relativeLocation.z);


        int slot;
        boolean quad;
        int directionIndex;


        if (!isLayingItem) {
            directionIndex = direction.get3DDataValue();
        } else {
            directionIndex = direction.getOpposite().get3DDataValue();
        }

        slot = directionIndex;

        quad = entity.quad.get(directionIndex) || player.isDiscrete();

        return Pair.of(slot * 4 + ((quad) ? getSubSlotFromPos(slot, x, y, z) : 0), quad);
    }

    public static List<Integer> getPreciseIndexFromHit(LayingItemEntity entity, BlockHitResult hit) {
        List<Integer> list = new ArrayList<>(0);

        for (int i = 0; i < entity.inv.size(); i++) {
            ItemStack stack = entity.inv.get(i);
            boolean cuboid = entity.isCuboid(i);
            boolean quad = entity.quad.get((int) i / 4);

            if (!stack.isEmpty()) {
                BlockPos blockPos = hit.getBlockPos();
                Vec3 pos = hit.getLocation();

                double x = Math.abs(pos.x - blockPos.getX());
                double y = Math.abs(pos.y - blockPos.getY());
                double z = Math.abs(pos.z - blockPos.getZ());

                boolean contains = contains(x, y, z, ((quad) ? ((cuboid) ? LayingItemEntity.basicQuadShapesBlock.get(i) : LayingItemEntity.basicQuadShapesItem.get(i)) : ((cuboid) ? LayingItemEntity.basicShapesBlock.get((int) i / 4) : LayingItemEntity.basicShapesItem.get((int) i / 4))).bounds());

                if (contains) {
                    list.add(i);
                }
            }
        }

        return list;
    }

    public static int getIndexFromXY(double a, double b) {
        return ((a > 0.5) ? 1 : 0) + ((b > 0.5) ? 2 : 0);
    }

    static Set<Block> BER_cache = new HashSet<>();

    public static boolean hasBER(BlockState state) {
        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();

        try {
            Field field = BlockEntityRenderDispatcher.class.getDeclaredField("renderers");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            #if MC_VER >= V1_21_9
            Map<BlockEntityType<?>, BlockEntityRenderer <?, ?>> renderers = (Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>>) field.get(dispatcher);
            #else
            Map<BlockEntityType<?>, BlockEntityRenderer <?>> renderers = (Map<BlockEntityType<?>, BlockEntityRenderer<?>>) field.get(dispatcher);
            #endif

            for (BlockEntityType<?> type : renderers.keySet()) {
                if (type.isValid(state)) return true;
            }

            return false;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return true;
        }
    }

    public static boolean isItemLike(Block block) {
        return block instanceof CrossCollisionBlock
                || block instanceof FenceBlock
                || block instanceof FenceGateBlock
                || block instanceof WallBlock
                || block instanceof DoorBlock
                || block instanceof BedBlock
                || block instanceof RedStoneWireBlock
                || block instanceof LadderBlock
                || block instanceof TripWireHookBlock
                || block instanceof VineBlock
                || block instanceof GlowLichenBlock
                || block instanceof BaseRailBlock
                || block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof TripWireBlock ||
                #if MC_VER >= V1_21_9
                block instanceof ShelfBlock ||
                #endif
                block instanceof ChestBlock
                ;
    }

    public static boolean isCuboid(Item item) {
        boolean isBlock = item instanceof BlockItem;
        boolean cuboid = false;
        if (isBlock) {
            Block block = ((BlockItem) item).getBlock();
            cuboid = !isItemLike(block);
        }

        return cuboid;
    }

    public static boolean renderAsBlock(Item item) {
        boolean isBlock = item instanceof BlockItem;
        boolean hasBER = false;
        boolean isBadBlockModel = false;

        if (isBlock) {
            Block block = ((BlockItem) item).getBlock();
            hasBER = BER_cache.contains(block) || hasBER(block.defaultBlockState());
            if (hasBER) BER_cache.add(block);

            isBadBlockModel = isItemLike(block);
        }
        return isBlock && !hasBER && !isBadBlockModel;
    }
}

package com.akicater;

import com.akicater.blocks.LayingItemEntity;
import com.akicater.client.EatingPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
#if MC_VER >= V1_19_2
import net.minecraft.util.RandomSource;
#else
import java.util.Random;
#endif
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;


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
            #if MC_VER >= V1_20_1 player.level() #else player.level #endif.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), vec32.x, vec32.y, vec32.z, vec3.x, vec3.y + 0.05, vec3.z);
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

    static List<AABB> boxes = new ArrayList<>(
            List.of(
                    new AABB(0.0, 1.0 - 1.0 / 16 * 4, 0.0, 1.0, 1.0, 1.0), // TOP
                    new AABB(0.0, 0.0, 0.0, 1.0, 1.0 / 16 * 4, 1.0), // DOWN
                    new AABB(0.0, 0.0, 1.0 - 1.0 / 16 * 4, 1.0, 1.0, 1.0), // SOUTH
                    new AABB(0.0, 0.0, 0, 1.0, 1.0, 1.0 / 16 * 4), // NORTH
                    new AABB(1.0 / 16 * 4, 0.0, 0.0, 1.0 / 16 * 4, 1.0f, 1.0f), // WEST
                    new AABB(1.0 - 1.0 / 16 * 4, 0.0, 0.0, 1.0 - 1.0 / 16 * 4, 1.0f, 1.0f) // EAST
            )
    );

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


    public static int getSlotFromShape(double x, double y, double z) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < boxes.size(); i++) {
            if (contains(x, y, z, boxes.get(i))) {
                return i;
            }
        }

        return -1;
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

    public static int getSlotFromHit(BlockHitResult hit, boolean empty, boolean quad) {
        BlockPos blockPos = hit.getBlockPos();
        Vec3 pos = hit.getLocation();

        double x = Math.abs(pos.x - blockPos.getX());
        double y = Math.abs(pos.y - blockPos.getY());
        double z = Math.abs(pos.z - blockPos.getZ());

        int slot;

        if (empty) {
            slot = hit.getDirection().get3DDataValue();
        } else {
            slot = getSlotFromShape(x, y, z);
        }

        return slot * 4 + ((quad) ? getSubSlotFromPos(slot, x, y, z) : 0);
    }

    public static List<Integer> getPreciseIndexFromHit(LayingItemEntity entity, BlockHitResult hit, Boolean empty) {
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
}

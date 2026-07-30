package com.akicater.network;

import com.mojang.datafixers.util.Pair;

#if MC_VER >= V1_21
import com.akicater.IPLA_Client;
import com.akicater.IPLA_Methods;
import com.akicater.blocks.LayingItem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
#endif

#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else

import net.minecraft.resources.ResourceLocation;
#endif

import net.minecraft.core.Vec3i;
import com.akicater.IPLA_Methods;
import com.akicater.blocks.LayingItem;
import net.minecraft.core.Direction;

import com.akicater.IPLA;
import com.akicater.blocks.LayingItemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.logging.Logger;

import static com.akicater.IPLA.*;

public #if MC_VER >= V1_21 record #else class #endif ItemPlacePayload #if MC_VER >= V1_21 (BlockPos pos, BlockHitResult hitResult) implements CustomPacketPayload #endif {
    #if MC_VER >= V1_21
    public static final Type<ItemPlacePayload> TYPE = new Type<>(#if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath(MOD_ID, "place_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemPlacePayload> CODEC = StreamCodec.of((buf, value) -> buf.writeBlockPos(value.pos).writeBlockHitResult(value.hitResult), buf -> new ItemPlacePayload(buf.readBlockPos() ,buf.readBlockHitResult()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    #endif

    public static float getDegrees() {
        Random random = new Random();

        float rotationDegrees = 90;
        float rotatedDegrees = random.nextFloat(180, 360) * (random.nextInt(0, 2) * 2 - 1);

        return rotatedDegrees - (rotatedDegrees % rotationDegrees);
    }

    public static void createBlockEntity(Level level, BlockState replacedBlockState, Block replacedBlock, BlockPos pos, ItemStack stack) {
        BlockState state = lItemBlock.get().defaultBlockState();

        if (replacedBlock == Blocks.WATER && replacedBlockState.getValue(BlockStateProperties.LEVEL) == 0) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, true);
        }

        if (stack.getItem() instanceof BlockItem) {
            state = state.setValue(LayingItem.LIGHT, ((BlockItem) stack.getItem()).getBlock().defaultBlockState().getLightEmission());
        }

        level.setBlockAndUpdate(pos, state);
    }

    public static void receive(Player player, BlockPos fuck, BlockHitResult hitResult) {
        ItemStack stack = player.getMainHandItem(); if (stack.isEmpty()) return; // Return if hand empty
        Level level = player #if MC_VER < V1_20_1 .level #else .level() #endif;

        BlockPos pos = hitResult.getBlockPos();



        Block hittedBlock = level.getBlockState(pos).getBlock();
        if (hittedBlock == Blocks.AIR || hittedBlock == Blocks.CAVE_AIR || hittedBlock == Blocks.WATER) return; // Preventing placing items in midair

        Vec3 location = hitResult.getLocation();
        Direction direction = hitResult.getDirection();
        Vec3 relativeLocation =
                location.add(
                        direction.getStepX() * 0.249,
                        direction.getStepY() * 0.249,
                        direction.getStepZ() * 0.249
                );
        int x = (int) relativeLocation.x - ((relativeLocation.x < 0) ? 1 : 0);
        int y = (int) relativeLocation.y - ((relativeLocation.y < 0) ? 1 : 0);
        int z = (int) relativeLocation.z - ((relativeLocation.z < 0) ? 1 : 0);
        BlockPos relativePos = new BlockPos(x, y, z);

        BlockState relativeBlockState = level.getBlockState(relativePos);
        Block relativeBlock = relativeBlockState.getBlock();

        boolean isEmpty = relativeBlock == Blocks.AIR || relativeBlock == Blocks.CAVE_AIR || relativeBlock == Blocks.WATER; // Checking if block is empty
        boolean isLayingItemRelative = relativeBlock instanceof LayingItem; // Checking if block is laying item
        boolean isLayingItemHitted = hittedBlock instanceof LayingItem;

        if (isEmpty || isLayingItemRelative) {
            BlockState state = null;
            if (isEmpty) {
                createBlockEntity(level, relativeBlockState, relativeBlock, relativePos, stack);
            } else {
                state = level.getChunk(relativePos).getBlockState(relativePos);
            }

            LayingItemEntity entity = (LayingItemEntity) level #if MC_VER < V1_21 .getChunk(relativePos) #endif.getBlockEntity(relativePos);
            if (entity == null) return;

            int directionIndex = hitResult.getDirection().get3DDataValue();

            Pair<Integer, Boolean> pair = IPLA_Methods.getSlotFromHit(hitResult, isLayingItemRelative && isLayingItemHitted, entity, player);
            int slot = pair.getFirst();
            boolean quad = pair.getSecond();

            if ((quad) ? !entity.isSubSlotEmpty(slot) : !entity.isSlotEmpty(slot / 4)) {
                return;
            }
            float flooredDegrees = getDegrees();

            entity.setItem(slot, stack);

            entity.rot.set(slot, flooredDegrees);
            entity.lastRot.set(slot, flooredDegrees);

            entity.quad.set(slot / 4, player.isDiscrete() || quad);

            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.4f);

            if (state != null) {
                state = state.setValue(LayingItem.LIGHT, entity.getMaxLightLevel());
                level.setBlockAndUpdate(relativePos, state);
            }

            entity.markDirty();
        }
    }
}


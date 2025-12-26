package com.akicater.network;

#if MC_VER >= V1_21
import com.akicater.blocks.LayingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
#endif

#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else
import com.akicater.blocks.LayingItem;
import net.minecraft.resources.ResourceLocation;
#endif

import com.akicater.IPLA;
import com.akicater.blocks.LayingItemEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;


import java.util.Random;

import static com.akicater.IPLA.*;

public #if MC_VER >= V1_21 record #else class #endif ItemPlacePayload #if MC_VER >= V1_21 (BlockHitResult hitResult) implements CustomPacketPayload #endif {
    #if MC_VER >= V1_21
    public static final Type<ItemPlacePayload> TYPE = new Type<>(#if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath(MOD_ID, "place_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemPlacePayload> CODEC = StreamCodec.of((buf, value) -> buf.writeBlockHitResult(value.hitResult), buf -> new ItemPlacePayload(buf.readBlockHitResult()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    #endif

    public static float getDegrees() {
        Random random = new Random();

        float rotationDegrees = config.getRotationDegrees();
        float rotatedDegrees = random.nextFloat(180, 360) * (random.nextInt(0, 2) * 2 - 1);

        return rotatedDegrees - (rotatedDegrees % rotationDegrees);
    }

    public static void createBlockEntity(Level level, BlockState replacedBlockState, Block replacedBlock, BlockPos pos) {
        BlockState state = IPLA.lItemBlock.get().defaultBlockState();

        if (replacedBlock == Blocks.WATER && replacedBlockState.getValue(BlockStateProperties.LEVEL) == 0) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, true);
        }

        level.setBlockAndUpdate(pos, state);
    }

    public static void receive(Player player, BlockHitResult hitResult) {
        ItemStack stack = player.getMainHandItem(); if (stack.isEmpty()) return; // Return if hand empty
        Level level = player #if MC_VER < V1_20_1 .level #else .level() #endif;

        BlockPos pos = hitResult.getBlockPos();

        Block hittedBlock = level.getBlockState(pos).getBlock();
        if (hittedBlock == Blocks.AIR || hittedBlock == Blocks.CAVE_AIR || hittedBlock == Blocks.WATER) return; // Preventing placing items in midair

        BlockPos relativePos = pos.relative(hitResult.getDirection(), 1);

        BlockState relativeBlockState = level.getBlockState(relativePos);
        Block relativeBlock = relativeBlockState.getBlock();

        boolean isEmpty = relativeBlock == Blocks.AIR || relativeBlock == Blocks.CAVE_AIR || relativeBlock == Blocks.WATER; // Checking if block is empty
        boolean isLayingItem = relativeBlock instanceof LayingItem; // Checking if block is layi

        if (isEmpty || isLayingItem) {
            if (isEmpty) {
                createBlockEntity(level, relativeBlockState, relativeBlock, relativePos);
            }

            LayingItemEntity entity = (LayingItemEntity) level #if MC_VER < V1_21 .getChunk(finalPos) #endif.getBlockEntity(relativePos);
            int directionIndex = hitResult.getDirection().get3DDataValue();

            if (entity == null) return;
            boolean quad = entity.quad.get(directionIndex);

            int slot = getSlotFromHit(hitResult, true, quad || player.isDiscrete());
            if (!((quad) ? entity.isSubSlotEmpty(slot) : entity.isSlotEmpty(directionIndex))) return;
            float flooredDegrees = getDegrees();

            entity.setItem(slot, stack);
            entity.rot.set(slot, flooredDegrees);

            entity.quad.set(directionIndex, player.isDiscrete() || quad);

            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.4f);

            entity.markDirty();
        }
    }
}


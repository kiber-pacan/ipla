package com.akicater.network;

#if MC_VER >= V1_21
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
#endif

import com.akicater.Ipla;
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

import static com.akicater.Ipla.MOD_ID;

public #if MC_VER >= V1_21 record #else class #endif ItemPlacePayload #if MC_VER >= V1_21 (BlockPos pos, BlockHitResult hitResult) implements CustomPacketPayload #endif {
    #if MC_VER >= V1_21
    public static final Type<ItemPlacePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "place_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemPlacePayload> CODEC = StreamCodec.of((buf, value) -> buf.writeBlockPos(value.pos).writeBlockHitResult(value.hitResult), buf -> new ItemPlacePayload(buf.readBlockPos(), buf.readBlockHitResult()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    #endif

    public static void receive(Player player, BlockPos pos, BlockHitResult hitResult) {
        ItemStack stack = player.getMainHandItem();
        if (stack == ItemStack.EMPTY) return;

        Level level = player #if MC_VER < V1_20_1 .level #else .level() #endif;
        BlockPos tempPos = pos;

        if (level.getBlockState(pos).getBlock() != Ipla.lItemBlock #if MC_VER < V1_21_3 .get() #endif) {
            tempPos = pos.relative(hitResult.getDirection(), 1);
        }

        Block replBlock = level.getBlockState(tempPos).getBlock();
        int i = hitResult.getDirection().get3DDataValue();

        Random random = new Random();

        if (replBlock == Blocks.AIR || replBlock == Blocks.WATER) {
            BlockState state = Ipla.lItemBlock #if MC_VER < V1_21_3 .get() #endif.defaultBlockState();

            if (replBlock == Blocks.WATER && level.getBlockState(tempPos).getValue(BlockStateProperties.LEVEL) == 0) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            level.setBlockAndUpdate(tempPos, state);

            LayingItemEntity entity = (LayingItemEntity)level#if MC_VER < V1_21 .getChunk(tempPos) #endif.getBlockEntity(tempPos);

            if (entity != null) {
                if (player.isDiscrete()) {
                    Pair<Integer,Integer> pair = Ipla.getIndexFromHit(hitResult, true);

                    int x = pair.getFirst() * 4 + pair.getSecond();

                    entity.setItem(x, stack);
                    entity.quad.set(i, true);
                    entity.rot.set(x, random.nextFloat(-360, 360));
                } else {
                    entity.setItem(i * 4, stack);
                    entity.rot.set(i * 4, random.nextFloat(-360, 360));
                }

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.4f);

                entity.markDirty();
            }
        } else if (level.getBlockState(tempPos).getBlock() == Ipla.lItemBlock #if MC_VER < V1_21_3 .get() #endif) {
            LayingItemEntity entity = (LayingItemEntity)level#if MC_VER < V1_21 .getChunk(tempPos) #endif.getBlockEntity(tempPos);

            if (entity != null) {
                boolean hitIsLItem = tempPos != pos;

                Pair<Integer,Integer> pair = Ipla.getIndexFromHit(hitResult, hitIsLItem);
                boolean quad = entity.quad.get(pair.getFirst()) || player.isDiscrete();

                int x = pair.getFirst() * 4 + ((quad) ? pair.getSecond() : 0);

                if(entity.inv.get(x).isEmpty()) {
                    entity.setItem(x, stack);
                    entity.rot.set(x, random.nextFloat(-360, 360));

                    if (quad)
                        entity.quad.set(pair.getFirst(), true);

                    level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.4f);
                }

                entity.markDirty();
            }
        }
    }
}


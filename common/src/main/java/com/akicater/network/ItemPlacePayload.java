package com.akicater.network;

import com.akicater.Ipla;
import com.akicater.blocks.LayingItemEntity;
import com.mojang.serialization.Codec;
import dev.architectury.hooks.level.biome.EffectsProperties;
import dev.architectury.impl.NetworkAggregator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import static com.akicater.Ipla.MOD_ID;

public record ItemPlacePayload(BlockPos pos, BlockHitResult hitResult) implements CustomPacketPayload {
    public static final Type<ItemPlacePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "place_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemPlacePayload> CODEC = StreamCodec.of((buf, value) -> buf.writeBlockPos(value.pos).writeBlockHitResult(value.hitResult), buf -> new ItemPlacePayload(buf.readBlockPos(), buf.readBlockHitResult()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(Player player, BlockPos pos, BlockHitResult hitResult) {
        Level level = player.level();
        ItemStack stack = player.getMainHandItem();
        if (level.getBlockState(pos).getBlock() == Blocks.AIR || level.getBlockState(pos).getBlock() == Blocks.WATER) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            Direction dir = hitResult.getDirection().getOpposite();
            BlockState state = Ipla.lItemBlock.get().defaultBlockState();
            if (stack == ItemStack.EMPTY) return;
            if (level.getBlockState(pos).getBlock() == Blocks.WATER) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            level.setBlock(pos, state,0);
            state.initCache();
            LayingItemEntity blockEntity = (LayingItemEntity)level.getChunk(pos).getBlockEntity(pos);
            if (blockEntity != null) {
                int i = dir.get3DDataValue();
                blockEntity.items.set(i, stack);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                //blockEntity.markDirty();
            }
        } else if (level.getBlockState(pos).getBlock() == Ipla.lItemBlock.get()) {
            Direction dir = hitResult.getDirection().getOpposite();
            LayingItemEntity blockEntity = (LayingItemEntity)level.getChunk(pos).getBlockEntity(pos);
            if (blockEntity != null) {
                int i = dir.get3DDataValue();
                if(blockEntity.items.get(i).isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    blockEntity.items.set(i, stack);
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    //blockEntity.markDirty();
                }
            }
        }
    }
}

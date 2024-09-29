package com.akicater.network;

import com.akicater.Ipla;
import com.akicater.blocks.LayingItemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import static com.akicater.Ipla.MOD_ID;

public record ItemPlacePayload(BlockPos pos, BlockHitResult hitResult) implements CustomPacketPayload {
    public static final Type<ItemPlacePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "place_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemPlacePayload> CODEC = StreamCodec.of((buf, value) -> buf.writeBlockPos(value.pos).writeBlockHitResult(value.hitResult), buf -> new ItemPlacePayload(buf.readBlockPos(), buf.readBlockHitResult()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(Player player, BlockPos pos, BlockHitResult hitResult) {
        ItemStack stack = player.getMainHandItem();
        if (stack == ItemStack.EMPTY) return;

        Level level = player.level();
        BlockPos tempPos = pos;
        if (level.getBlockState(pos).getBlock() != Ipla.lItemBlock.get()) {
            tempPos = pos.relative(hitResult.getDirection(), 1);
        }

        Block replBlock = level.getBlockState(tempPos).getBlock();

        if (replBlock == Blocks.AIR || replBlock == Blocks.WATER) {
            BlockState state = Ipla.lItemBlock.get().defaultBlockState();

            if (replBlock == Blocks.WATER) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            level.setBlockAndUpdate(tempPos, state);
            LayingItemEntity entity = (LayingItemEntity)level.getBlockEntity(tempPos);

            if (entity != null) {
                int i = hitResult.getDirection().get3DDataValue();

                if (player.isCrouching()) {
                    entity.inv.set(Ipla.getIndexFromHit(hitResult, true), stack);
                    entity.quad.set(i, true);
                } else {
                    entity.inv.set(i * 4, stack);
                }
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

        } else if (level.getBlockState(tempPos).getBlock() == Ipla.lItemBlock.get()) {
            LayingItemEntity entity = (LayingItemEntity)level.getBlockEntity(tempPos);

            if (entity != null) {
                int i = hitResult.getDirection().get3DDataValue();

                if (player.isCrouching()) {
                    int x = Ipla.getIndexFromHit(hitResult, (level.getBlockState(pos).getBlock()!= Ipla.lItemBlock.get()));
                    if(entity.inv.get(x).isEmpty()) {
                        entity.inv.set(x, stack);
                        entity.quad.set(i, true);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                } else {
                    if(entity.inv.get(i * 4).isEmpty()) {
                        entity.inv.set(i * 4, stack);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }

                entity.markDirty();

            }
        }
    }
}

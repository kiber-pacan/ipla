package com.akicater.blocks;

import com.akicater.Ipla;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

import static com.akicater.Ipla.HIDE_ITEM_KEY;

public class LayingItem extends BaseEntityBlock {
    public static final MapCodec<LayingItem> CODEC = simpleCodec(LayingItem::new);

    public LayingItem(Properties properties) {
        super(properties);
    }

    @Override
    public LayingItemEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LayingItemEntity(pos, state);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }


    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {}


    // Get voxel shape of placed items
    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        LayingItemEntity entity = (LayingItemEntity) level.getBlockEntity(pos);

        if (entity != null) {
            return entity.getShape();
        }

        return super.getShape(state, level, pos, context);
    }


    // Drop items on break
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            LayingItemEntity entity = (LayingItemEntity) level.getBlockEntity(pos);
            if (entity != null) {
                if (!entity.isEmpty()) {
                    for(int i = 0; i < 24; ++i) {
                        ItemStack itemStack = entity.inv.get(i);
                        if (!itemStack.isEmpty()) {
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemStack);
                        }
                    }

                    entity.inv.clear();
                    entity.setRemoved();

                    level.updateNeighbourForOutputSignal(pos, this);
                }
            }

            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    public static void dropItem(Level level, BlockPos pos, Pair<Integer, Integer> pair, LayingItemEntity entity) {
        int s = pair.getFirst();
        int i = pair.getSecond();

        if (entity.quad.get(s)) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), entity.inv.get(s * 4 + i));
        } else {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), entity.inv.get(s * 4));
        }
    }


    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        LayingItemEntity entity = (LayingItemEntity) level.getBlockEntity(pos);
        if (entity != null) {
            Pair<Integer, Integer> pair = Ipla.getIndexFromHit(hit, false);
            int x = pair.getFirst();
            int i = pair.getSecond();

            if (player.getMainHandItem().isEmpty()) {
                if (!entity.quad.get(x)) i = 0;

                player.setItemInHand(InteractionHand.MAIN_HAND, entity.inv.get(x * 4 + i));
                entity.inv.set(x * 4 + i, ItemStack.EMPTY);
                level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 1, 1.4f, 1);
            } else {
                dropItem(level, pos, pair, entity);
                level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 1, 1.4f, 1);
            }

            if (entity.isEmpty()) {
                level.removeBlock(pos, false);
                entity.setRemoved();
            } else if (entity.isSlotEmpty(x)) {
                entity.quad.set(x, false);
                entity.markDirty();
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }
}



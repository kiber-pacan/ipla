package com.akicater.blocks;

import com.akicater.Ipla;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

import static com.akicater.Ipla.HIDE_ITEM_KEY;

public class LayingItem extends BaseEntityBlock {
    #if MC_VER > V1_20_1 public static final MapCodec<LayingItem> CODEC = simpleCodec(LayingItem::new); #endif

    public LayingItem(Properties properties) {
        super(properties);
    }

    @Override
    public LayingItemEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LayingItemEntity(pos, state);
    }

    #if MC_VER > V1_20_1
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    #endif

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {}


    // Get voxel shape of placed items
    @Override
    #if MC_VER >= V1_21 protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) #else public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)  #endif {
       LayingItemEntity entity = (LayingItemEntity) level.getBlockEntity(pos);

        if (entity != null) {
            return entity.getShape();
        }

        return super.getShape(state, level, pos, context);
    }


    // Drop items on break
    @Override
    #if MC_VER >= V1_21 protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) #else public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) #endif {
        if (!state.is(newState.getBlock())) {
            LayingItemEntity entity = (LayingItemEntity) level#if MC_VER < V1_21 .getChunk(pos) #endif.getBlockEntity(pos);
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

    @Override
    #if MC_VER >= V1_21 protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) #else public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) #endif {
        LayingItemEntity entity = (LayingItemEntity) level#if MC_VER < V1_21 .getChunk(pos) #endif.getBlockEntity(pos);

        if (entity != null) {
            Pair<Integer, Integer> pair = Ipla.getIndexFromHit(hit, false);
            int s = pair.getFirst();
            int i = pair.getSecond();

            if (!entity.quad.get(s)) i = 0;
            if (entity.inv.get(s * 4 + i).isEmpty()) return InteractionResult.FAIL;

            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, entity.inv.get(s * 4 + i));
                entity.inv.set(s * 4 + i, ItemStack.EMPTY);

                level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 1, 1.4f, 1);
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), entity.inv.get(s * 4 + i));

                level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 1, 1.4f, 1);
            }

            if (entity.isEmpty()) {
                level.removeBlock(pos, false);
                entity.setRemoved();
            } else if (entity.isSlotEmpty(s)) {
                entity.quad.set(s, false);
                entity.markDirty();
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }
}



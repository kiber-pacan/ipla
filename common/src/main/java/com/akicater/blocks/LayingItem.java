package com.akicater.blocks;

import com.akicater.Ipla;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
#if MC_VER >= V1_19_4
import net.minecraft.core.registries.Registries;
#endif
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

#if MC_VER > V1_21
import net.minecraft.util.RandomSource;

import static com.akicater.Ipla.MOD_ID;
#endif

public class LayingItem extends BaseEntityBlock implements SimpleWaterloggedBlock {
    #if MC_VER > V1_20_1 public static final MapCodec<LayingItem> CODEC = simpleCodec(LayingItem::new); #endif
    public static BooleanProperty WATERLOGGED = BooleanProperty.create("waterlogged");

    public LayingItem(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    #if MC_VER < V1_21_3
    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)  {
        if (state.getValue(WATERLOGGED)) {
            level #if MC_VER <= V1_17_1 .getLiquidTicks() #endif.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos,  neighborPos);
    }
    #else
    @Override
    public @NotNull BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos arg6, BlockState arg7, RandomSource arg8) {
        if (blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }

        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, arg6, arg7, arg8);
    }
    #endif

    #if MC_VER >= V1_21_4
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    #endif


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
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
    #if MC_VER < V1_21_5
    @Override
    #if MC_VER >= V1_21  protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) #else public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) #endif {
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
            #if MC_VER >= V1_21_5
            super.onRemove(state, level, pos, newState, movedByPiston);
            #else
            super.onRemove(state, level, pos, newState, movedByPiston);
            #endif
        }
    }
    #endif

    @Override
    #if MC_VER >= V1_21 protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) #else public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) #endif {
        LayingItemEntity entity = (LayingItemEntity) level#if MC_VER < V1_21 .getChunk(pos) #endif.getBlockEntity(pos);

        if (entity != null) {
            Pair<Integer, Integer> pair = Ipla.getIndexFromHit(hit, false);
            int s = pair.getFirst();
            int i = pair.getSecond();

            if (!entity.quad.get(s)) i = 0;
            if (entity.inv.get(s * 4 + i).isEmpty()) return InteractionResult.FAIL;

            ItemStack itemStack = entity.inv.get(s * 4 + i).copy();
            boolean success = player.addItem(itemStack);

            if (success && itemStack.isEmpty()) {
                entity.inv.set(s * 4 + i, ItemStack.EMPTY);
                entity.markDirty(player);
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), #if MC_VER >= V1_18_2 SoundEvents.BUNDLE_REMOVE_ONE #else SoundEvents.DISPENSER_FAIL #endif , SoundSource.BLOCKS, 1, 1.4f);

                if (entity.isEmpty()) {
                    level.removeBlock(pos, false);
                    entity.setRemoved();
                } else if (entity.isSlotEmpty(s)) {
                    entity.quad.set(s, false);
                }

                entity.markDirty(player);

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    static {
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
    }
}



package com.akicater.blocks;

import com.akicater.Ipla;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static void dropItem(Level level, BlockPos pos, int i, LayingItemEntity entity) {
        Containers.dropItemStack(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), entity.inv.get(i));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        LayingItemEntity entity = (LayingItemEntity) level.getBlockEntity(pos);
        if (entity != null) {
            dropItem(level, pos, Ipla.getIndexForDropping(hit, entity), entity);

            if (entity.isEmpty()) {
                level.removeBlock(pos, false);
                entity.setRemoved();
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }
}

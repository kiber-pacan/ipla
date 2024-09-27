package com.akicater.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LayingItem extends BaseEntityBlock {
    public static final MapCodec<FurnaceBlock> CODEC = simpleCodec(FurnaceBlock::new);

    public LayingItem(Properties properties) {
        super(properties);
    }

    @Override
    public LayingItemEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LayingItemEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

}

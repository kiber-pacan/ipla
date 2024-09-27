package com.akicater.blocks;

import com.akicater.Ipla;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LayingItemEntity extends BlockEntity {
    public NonNullList<ItemStack> items = NonNullList.createWithCapacity(24);
    public List<Boolean> quad = new ArrayList<>(6);
    public List<Integer> rot = new ArrayList<>(24);

    public LayingItemEntity(BlockPos pos, BlockState blockState) {
        super(Ipla.lItemBlockEntity.get(), pos, blockState);
    }

    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        ContainerHelper.loadAllItems(compoundTag, this.items, provider);
    }
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        ContainerHelper.saveAllItems(compoundTag, this.items, provider);
    }
}

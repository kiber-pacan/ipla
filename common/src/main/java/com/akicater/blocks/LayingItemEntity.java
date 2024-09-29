package com.akicater.blocks;

import com.akicater.Ipla;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LayingItemEntity extends BlockEntity {
    // Inventory
    public NonNullList<ItemStack> inv;
    // Item rotations
    public List<Integer> rot;
    // Quad mode for sides
    public List<Boolean> quad;


    public LayingItemEntity(BlockPos pos, BlockState blockState) {
        super(Ipla.lItemBlockEntity.get(), pos, blockState);

        inv = NonNullList.withSize(24, ItemStack.EMPTY);
        rot = NonNullList.withSize(24, 0);
        quad = NonNullList.withSize(6, false);
    }


    // Load nbt data shit
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ContainerHelper.loadAllItems(compoundTag, this.inv, provider);

        for (int i = 0; i < 6; i++) {
            quad.set(i, compoundTag.getBoolean("s" + i));
        }

        super.loadAdditional(compoundTag, provider);
    }

    // Save nbt data
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        ContainerHelper.saveAllItems(compoundTag, this.inv, provider);

        for (int i = 0; i < 6; i++) {
            compoundTag.putBoolean("s" + i, quad.get(i));
        }
    }

    /* At this point i just wanna fucking kill myself
    for god's sake don't ever ever ever forget to add this stupid method*/
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }


    public VoxelShape getShape() {
        List<VoxelShape> tempShape = new ArrayList<>();

        if (!isSlotEmpty(0)) {
            tempShape.add(Shapes.box(0.125f, 0.875f, 0.125f, 0.875f, 1.0f, 0.875f));
        }
        if (!isSlotEmpty(1)) {
            tempShape.add(Shapes.box(0.125f, 0.0f, 0.125f, 0.875f, 0.125f, 0.875f));
        }
        if (!isSlotEmpty(2)) {
            tempShape.add(Shapes.box(0.125f, 0.125f, 0.875f, 0.875f, 0.875f, 1.0f));
        }
        if (!isSlotEmpty(3)) {
            tempShape.add(Shapes.box(0.125f, 0.125f, 0.0f, 0.875f, 0.875f, 0.125f));
        }
        if (!isSlotEmpty(4)) {
            tempShape.add(Shapes.box(0.875f, 0.125f, 0.125f, 1.0f, 0.875f, 0.875f));
        }
        if (!isSlotEmpty(5)) {
            tempShape.add(Shapes.box(0.0f, 0.125f, 0.125f, 0.125f, 0.875f, 0.875f));
        }

        Optional<VoxelShape> shape = tempShape.stream().reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR));
        return shape.orElseGet(() -> Shapes.box(0, 0, 0, 1, 1, 1));
    }


    public boolean isSlotEmpty(int slot) {
        for (int i = slot * 4; i < slot * 4 + 4; i++) {
            if (!this.inv.get(i).isEmpty()) return false;
        }

        return true;
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this.inv) {
            if (!itemStack.isEmpty()) return false;
        }

        return true;
    }


    // Mark dirty so client get synced with server
    public void markDirty() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
}

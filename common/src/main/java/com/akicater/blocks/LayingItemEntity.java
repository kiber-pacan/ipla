package com.akicater.blocks;

import com.akicater.Ipla;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
#if MC_VER >= V1_21
import net.minecraft.core.HolderLookup;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LayingItemEntity extends BlockEntity {
    // Inventory
    public NonNullList<ItemStack> inv;

    // Item rotations
    public NonNullList<Float> rot;
    public NonNullList<Float> lastRot;

    // Quad mode for sides
    public NonNullList<Boolean> quad;


    public LayingItemEntity(BlockPos pos, BlockState blockState) {
        super(Ipla.lItemBlockEntity #if MC_VER < V1_21_3 .get() #endif, pos, blockState);

        inv = NonNullList.withSize(24, ItemStack.EMPTY);

        rot = NonNullList.withSize(24, 0.0f);
        lastRot = NonNullList.withSize(24, 0.0f);

        quad = NonNullList.withSize(6, false);
    }


    // Load nbt data shit
    #if MC_VER >= V1_21 protected #else public #endif void #if MC_VER >= V1_21 loadAdditional #else load #endif(CompoundTag compoundTag#if MC_VER >= V1_21 , HolderLookup.Provider provider#endif) {
        #if MC_VER >= V1_21 super.loadAdditional(compoundTag, provider); #else super.load(compoundTag); #endif
        this.inv.clear();
        ContainerHelper.loadAllItems(compoundTag, this.inv #if MC_VER >= V1_21 , provider #endif);

        for (int i = 0; i < 6; i++) {
            quad.set(i, compoundTag.getBoolean("s" + i) #if MC_VER >= V1_21_5 .get() #endif);
        }

        for (int i = 0; i < 24; i++) {
            rot.set(i, compoundTag.getFloat("r" + i) #if MC_VER >= V1_21_5 .get() #endif);
        }
    }

    // Save nbt data
    #if MC_VER >= V1_21 protected #else public @NotNull #endif  #if MC_VER < V1_18_2 CompoundTag save #else void saveAdditional #endif (CompoundTag compoundTag#if MC_VER >= V1_21 , HolderLookup.Provider provider#endif) {
        super.#if MC_VER < V1_18_2 save #else saveAdditional #endif(compoundTag #if MC_VER >= V1_21 , provider #endif);

        ContainerHelper.saveAllItems(compoundTag, this.inv #if MC_VER >= V1_21 , provider #endif);

        for (int i = 0; i < 6; i++) {
            compoundTag.putBoolean("s" + i, quad.get(i));
        }

        for (int i = 0; i < 24; i++) {
            compoundTag.putFloat("r" + i, rot.get(i));
        }

        #if MC_VER < V1_18_2
        return compoundTag;
        #endif
    }

    /* At this point i just wanna fucking kill myself
    for god's sake don't ever ever ever forget to add these stupid methods*/
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        #if MC_VER < V1_18_2
        return new ClientboundBlockEntityDataPacket(this.worldPosition, -1, this.save(new CompoundTag()));
        #else
        return ClientboundBlockEntityDataPacket.create(this);
        #endif
    }

    public @NotNull CompoundTag getUpdateTag(#if MC_VER >= V1_21 HolderLookup.Provider provider #endif) {
        CompoundTag compoundTag = #if MC_VER < V1_18_2 this.save(new CompoundTag()) #elif MC_VER >= V1_21 this.saveCustomOnly(provider) #else this.saveWithoutMetadata() #endif;

        ContainerHelper.saveAllItems(compoundTag, this.inv #if MC_VER >= V1_21 , provider #endif);

        return compoundTag;
    }


    public void setItem(int index, ItemStack stack) {
        inv.set(index, stack.split(1));
    }

    //#if MC_VER >= V1_21
    public VoxelShape getShape() {
        List<VoxelShape> tempShape = new ArrayList<>();

        if (!isSlotEmpty(0)) {
            tempShape.add(Shapes.box(0.125, 0.875, 0.125, 0.875, 1.0, 0.875f));
        }
        if (!isSlotEmpty(1)) {
            tempShape.add(Shapes.box(0.125, 0.0, 0.125, 0.875, 0.125, 0.875f));
        }
        if (!isSlotEmpty(2)) {
            tempShape.add(Shapes.box(0.125, 0.125, 0.875, 0.875, 0.875, 1.0f));
        }
        if (!isSlotEmpty(3)) {
            tempShape.add(Shapes.box(0.125, 0.125, 0.0, 0.875, 0.875, 0.125f));
        }
        if (!isSlotEmpty(4)) {
            tempShape.add(Shapes.box(0.875, 0.125, 0.125, 1.0, 0.875, 0.875f));
        }
        if (!isSlotEmpty(5)) {
            tempShape.add(Shapes.box(0.0, 0.125, 0.125, 0.125, 0.875, 0.875f));
        }

        Optional<VoxelShape> shape = tempShape.stream().reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR));
        return shape.orElseGet(() -> Shapes.box(0, 0, 0, 1, 0.1, 1));
    }
    //#endif


    public boolean isSlotEmpty(int slot) {
        for (int i = slot * 4; i < slot * 4 + 4; i++) {
            if (!this.inv.get(i).isEmpty()) {
                return false;
            }
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
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);

    }
    public void markDirty(@Nullable Entity entity) {
        this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos() #if MC_VER > V1_18_2, GameEvent.Context.of(entity, this.getBlockState()) #endif);
        this.markDirty();
    }

    #if MC_VER >= V1_21_5
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
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

                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
        }
        super.preRemoveSideEffects(pos, state);
    }
    #endif
}

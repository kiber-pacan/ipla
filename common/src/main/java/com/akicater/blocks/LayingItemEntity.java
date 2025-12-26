package com.akicater.blocks;

import com.akicater.IPLA;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
#if MC_VER >= V1_21
import net.minecraft.core.HolderLookup;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
#if MC_VER >= V1_21_6
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
#endif
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

#if MC_VER >= V1_21_5
import net.minecraft.world.Containers;

#endif

public class LayingItemEntity extends BlockEntity {
    // Inventory
    public NonNullList<ItemStack> inv;

    // Item rotations
    public NonNullList<Float> rot;
    public NonNullList<Float> lastRot;

    // Quad mode for sides
    public NonNullList<Boolean> quad;


    public LayingItemEntity(BlockPos pos, BlockState blockState) {
        super(IPLA.lItemBlockEntity.get(), pos, blockState);

        inv = NonNullList.withSize(24, ItemStack.EMPTY);

        rot = NonNullList.withSize(24, 0.0f);
        lastRot = NonNullList.withSize(24, 0.0f);

        quad = NonNullList.withSize(6, false);
    }


    // Load nbt data shit
    #if MC_VER >= V1_21 protected #else public #endif void #if MC_VER >= V1_21 loadAdditional #else load #endif(#if MC_VER >= V1_21_6 ValueInput compoundTag #else CompoundTag compoundTag #if MC_VER >= V1_21, HolderLookup.Provider provider #endif #endif) {
        #if MC_VER >= V1_21 super.loadAdditional(compoundTag #if MC_VER <= V1_21_5, provider #endif); #else super.load(compoundTag); #endif
        this.inv.clear();
        ContainerHelper.loadAllItems(compoundTag, this.inv #if MC_VER >= V1_21 && MC_VER <= V1_21_5 , provider #endif);

        for (int i = 0; i < 6; i++) {
            quad.set(i, compoundTag #if MC_VER <= V1_21_5 .getBoolean #else .getBooleanOr #endif("s" + i #if MC_VER >= V1_21_6, true #endif) #if MC_VER == V1_21_5 .get() #endif);
        }

        for (int i = 0; i < 24; i++) {
            rot.set(i, compoundTag #if MC_VER <= V1_21_5 .getFloat #else .getFloatOr #endif("r" + i #if MC_VER >= V1_21_6, 1.0f #endif) #if MC_VER == V1_21_5 .get() #endif);
        }
    }

    // Save nbt data
    #if MC_VER >= V1_21 protected #else public @NotNull #endif  #if MC_VER < V1_18_2 CompoundTag save #else void saveAdditional #endif (#if MC_VER >= V1_21_6 ValueOutput compoundTag #else CompoundTag compoundTag #if MC_VER >= V1_21, HolderLookup.Provider provider #endif #endif) {
        super.#if MC_VER < V1_18_2 save #else saveAdditional #endif(compoundTag #if MC_VER <= V1_21_5 && MC_VER >= V1_21, provider #endif);

        ContainerHelper.saveAllItems(compoundTag, this.inv #if MC_VER <= V1_21_5 && MC_VER >= V1_21, provider #endif);

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

    #if MC_VER >= V1_21_6
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
    #else
    public @NotNull CompoundTag getUpdateTag(#if MC_VER >= V1_21 HolderLookup.Provider provider #endif) {
        CompoundTag compoundTag = #if MC_VER < V1_18_2 this.save(new CompoundTag()) #elif MC_VER >= V1_21 this.saveCustomOnly(provider) #else this.saveWithoutMetadata() #endif;

        ContainerHelper.saveAllItems(compoundTag, this.inv #if MC_VER <= V1_21_5 && MC_VER >= V1_21, provider #endif);

        return compoundTag;
    }
    #endif

    public boolean isCuboid(int slot, int subSlot) {
        return this.inv.get(slot * 4 + subSlot).getItem() instanceof BlockItem && ((BlockItem) this.inv.get(slot * 4 + subSlot).getItem()).getBlock().defaultBlockState().isCollisionShapeFullBlock(this.getLevel(), this.getBlockPos());
    }

    public boolean isCuboid(int slot) {
        return this.inv.get(slot).getItem() instanceof BlockItem && ((BlockItem) this.inv.get(slot).getItem()).getBlock().defaultBlockState().isCollisionShapeFullBlock(this.getLevel(), this.getBlockPos());
    }

    public static List<VoxelShape> basicShapesItem = List.of(
            Shapes.box(0.0, 1.0 - 1.0 / 16, 0.0, 1.0, 1.0, 1.0), // TOP
            Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0 / 16, 1.0), // DOWN
            Shapes.box(0.0, 0.0, 1.0 - 1.0 / 16, 1.0, 1.0, 1.0), // SOUTH
            Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0 / 16), // NORTH
            Shapes.box(1.0 - 1.0 / 16, 0.0, 0.0, 1.0, 1.0, 1.0), // WEST
            Shapes.box(0.0, 0.0, 0.0, 1.0 / 16, 1.0, 1.0) // EAST
    );

    public static List<VoxelShape> basicShapesBlock = List.of(
            Shapes.box(1.0 / 4, 1.0 / 2, 1.0 / 4, 3 * 1.0 / 4, 1.0, 3 * 1.0 / 4), // TOP
            Shapes.box(1.0 / 4, 0.0, 1.0 / 4, 3 * 1.0 / 4, 1.0 / 2, 3 * 1.0 / 4), // DOWN
            Shapes.box(1.0 / 4, 1.0 / 4, 1.0 / 2, 3 * 1.0 / 4, 3 * 1.0 / 4, 1.0), // SOUTH
            Shapes.box(1.0 / 4, 1.0 / 4, 0, 3 * 1.0 / 4, 3 * 1.0 / 4, 1.0 / 2), // SOUTH
            Shapes.box(1.0 / 2, 1.0 / 4, 1.0 / 4, 1.0, 3 * 1.0 / 4, 3 * 1.0 / 4), // WEST
            Shapes.box(0, 1.0 / 4, 1.0 / 4, 1.0 / 2, 3 * 1.0 / 4, 3 * 1.0 / 4) // EAST
    );

    public static List<VoxelShape> basicQuadShapesItem = List.of(
            // TOP
            Shapes.box(0.0, 1.0 - 1.0 / 16, 0.0, 1.0 / 2, 1.0, 1.0 / 2),
            Shapes.box(1.0 / 2, 1.0 - 1.0 / 16, 0.0, 1.0, 1.0, 1.0 / 2),
            Shapes.box(0.0, 1.0 - 1.0 / 16, 1.0 / 2, 1.0 / 2, 1.0, 1.0),
            Shapes.box(1.0 / 2, 1.0 - 1.0 / 16, 1.0 / 2, 1.0, 1.0, 1.0),

            // DOWN
            Shapes.box(0.0, 0, 1.0 / 2, 1.0 / 2, 1.0 / 16, 1.0),
            Shapes.box(1.0 / 2, 0, 1.0 / 2, 1.0, 1.0 / 16, 1.0),
            Shapes.box(0.0, 0, 0.0, 1.0 / 2, 1.0 / 16, 1.0 / 2),
            Shapes.box(1.0 / 2, 0, 0.0, 1.0, 1.0 / 16, 1.0 / 2),

            // SOUTH
            Shapes.box(1.0 / 2, 0, 1.0 - 1.0 / 16, 1.0, 1.0 / 2, 1.0),
            Shapes.box(0, 0, 1.0 - 1.0 / 16, 1.0 / 2, 1.0 / 2, 1.0),
            Shapes.box(1.0 / 2, 1.0 / 2, 1.0 - 1.0 / 16, 1.0, 1.0, 1.0),
            Shapes.box(0, 1.0 / 2, 1.0 - 1.0 / 16, 1.0 / 2, 1.0, 1.0),

            // NORTH
            Shapes.box(0, 0, 0, 1.0 / 2, 1.0 / 2, 1.0 / 16),
            Shapes.box(1.0 / 2, 0, 0, 1.0, 1.0 / 2, 1.0 / 16),
            Shapes.box(0, 1.0 / 2, 0, 1.0 / 2, 1.0, 1.0 / 16),
            Shapes.box(1.0 / 2, 1.0 / 2, 0, 1.0, 1.0, 1.0 / 16),

            // WEST
            Shapes.box(1.0 - 1.0 / 16, 0, 0, 1.0, 1.0 / 2, 1.0 / 2),
            Shapes.box(1.0 - 1.0 / 16, 0, 1.0 / 2, 1.0, 1.0 / 2, 1.0),
            Shapes.box(1.0 - 1.0 / 16, 1.0 / 2, 0, 1.0, 1.0, 1.0 / 2),
            Shapes.box(1.0 - 1.0 / 16, 1.0 / 2, 1.0 / 2, 1.0, 1.0, 1.0),

            // EAST
            Shapes.box(0.0, 0, 1.0 / 2, 1.0 / 16, 1.0 / 2, 1.0),
            Shapes.box(0.0, 0, 0, 1.0 / 16, 1.0 / 2, 1.0 / 2),
            Shapes.box(0.0, 1.0 / 2, 1.0 / 2, 1.0 / 16, 1.0, 1.0),
            Shapes.box(0.0, 1.0 / 2, 0, 1.0 / 16, 1.0, 1.0 / 2)
    );

    public static List<VoxelShape> basicQuadShapesBlock = List.of(
            // TOP
            Shapes.box(1.0 / 16 * 2, 1.0 - 1.0 / 16 * 4, 1.0 / 16 * 2, 1.0 / 16 * 6, 1.0, 1.0 / 16 * 6),
            Shapes.box(1.0 / 2 + 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 4, 1.0 / 16 * 2, 1.0 / 2 + 1.0 / 16 * 6, 1.0, 1.0 / 16 * 6),
            Shapes.box(1.0 / 16 * 2, 1.0 - 1.0 / 16 * 4, 1.0 / 2 + 1.0 / 16 * 2, 1.0 / 16 * 6, 1.0, 1.0 / 2 + 1.0 / 16 * 6),
            Shapes.box(1.0 / 2 + 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 4, 1.0 / 2 +  1.0 / 16 * 2, 1.0 / 2 + 1.0 / 16 * 6, 1.0, 1.0 / 2 + 1.0 / 16 * 6),

            // DOWN
            Shapes.box(1.0 / 16 * 2, 0, 1.0 / 2 + 1.0 / 16 * 2, 1.0 / 16 * 6, 1.0 / 16 * 4, 1.0 / 2 +  1.0 / 16 * 6),
            Shapes.box(1.0 / 2 + 1.0 / 16 * 2, 0, 1.0 / 2 +  1.0 / 16 * 2, 1.0 / 2 + 1.0 / 16 * 6, 1.0 / 16 * 4, 1.0 / 2 +  1.0 / 16 * 6),
            Shapes.box(1.0 / 16 * 2, 0, 1.0 / 16 * 2, 1.0 / 16 * 6, 1.0 / 16 * 4, 1.0 / 16 * 6),
            Shapes.box(1.0 / 2 + 1.0 / 16 * 2, 0, 1.0 / 16 * 2, 1.0 / 2 + 1.0 / 16 * 6, 1.0 / 16 * 4, 1.0 / 16 * 6),


            // SOUTH
            Shapes.box(1.0 - 1.0 / 16 * 6, 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 4, 1.0 - 1.0 / 16 * 2, 1.0 / 16 * 6, 1.0),
            Shapes.box(1.0 / 16 * 2, 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 4, 1.0 / 16 * 6, 1.0 / 16 * 6, 1.0),
            Shapes.box(1.0 - 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 4, 1.0 - 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 2, 1.0),
            Shapes.box(1.0 / 16 * 2, 1.0 - 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 4, 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 2, 1.0),


            // NORTH
            Shapes.box(1.0 / 16 * 2, 1.0 / 16 * 2, 0, 1.0 / 16 * 6, 1.0 / 16 * 6, 1.0 / 16 * 4),
            Shapes.box(1.0 - 1.0 / 16 * 6, 1.0 / 16 * 2, 0, 1.0 - 1.0 / 16 * 2, 1.0 / 16 * 6, 1.0 / 16 * 4),
            Shapes.box(1.0 / 16 * 2, 1.0 - 1.0 / 16 * 6, 0, 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 2, 1.0 / 16 * 4),
            Shapes.box(1.0 - 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 6, 0, 1.0 - 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 2, 1.0 / 16 * 4),

            // WEST
            Shapes.box(1.0 - 1.0 / 16 * 4, 1.0 / 16 * 2, 1.0 / 16 * 2, 1.0, 1.0 / 16 * 6, 1.0 / 16 * 6),
            Shapes.box(1.0 - 1.0 / 16 * 4, 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 6, 1.0, 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 2),
            Shapes.box(1.0 - 1.0 / 16 * 4, 1.0 - 1.0 / 16 * 6, 1.0 / 16 * 2, 1.0, 1.0 - 1.0 / 16 * 2, 1.0 / 16 * 6),
            Shapes.box(1.0 - 1.0 / 16 * 4, 1.0 - 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 6, 1.0, 1.0 - 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 2),

            // EAST 1.0 / 16 * 4
            Shapes.box(0.0, 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 6, 1.0 / 16 * 4, 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 2),
            Shapes.box(0.0, 1.0 / 16 * 2, 1.0 / 16 * 2, 1.0 / 16 * 4, 1.0 / 16 * 6, 1.0 / 16 * 6),
            Shapes.box(0.0, 1.0 - 1.0 / 16 * 6, 1.0 - 1.0 / 16 * 6, 1.0 / 16 * 4, 1.0 - 1.0 / 16 * 2, 1.0 - 1.0 / 16 * 2),
            Shapes.box(0.0, 1.0 - 1.0 / 16 * 6, 1.0 / 16 * 2, 1.0 / 16 * 4, 1.0 - 1.0 / 16 * 2, 1.0 / 16 * 6)

    );

    public void setItem(int index, ItemStack stack) {
        inv.set(index, stack.split(1));
    }

    //#if MC_VER >= V1_21
    public VoxelShape getShape() {
        List<VoxelShape> tempShape = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            if (!isSlotEmpty(i)) {
                if (!quad.get(i)) {
                    if (isCuboid(i, 0)) tempShape.add(basicShapesBlock.get(i)); else tempShape.add(basicShapesItem.get(i));
                } else {
                    if (!isSubSlotEmpty(i, 0)) if (isCuboid(i, 0)) tempShape.add(basicQuadShapesBlock.get(i * 4)); else tempShape.add(basicQuadShapesItem.get(i * 4));
                    if (!isSubSlotEmpty(i, 1)) if (isCuboid(i, 1)) tempShape.add(basicQuadShapesBlock.get(i * 4 + 1)); else tempShape.add(basicQuadShapesItem.get(i * 4 + 1));
                    if (!isSubSlotEmpty(i, 2)) if (isCuboid(i, 2)) tempShape.add(basicQuadShapesBlock.get(i * 4 + 2)); else tempShape.add(basicQuadShapesItem.get(i * 4 + 2));
                    if (!isSubSlotEmpty(i, 3)) if (isCuboid(i, 3)) tempShape.add(basicQuadShapesBlock.get(i * 4 + 3)); else tempShape.add(basicQuadShapesItem.get(i * 4 + 3));
                }
            }
        }

        Optional<VoxelShape> shape = tempShape.stream().reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR));
        return shape.orElseGet(() -> Shapes.box(0, 0, 0, 1, 0.1, 1));
    }
    //#endif


    public boolean isSubSlotEmpty(int slot, int subslot) {
        return this.inv.get(slot * 4 + subslot).isEmpty();
    }

    public boolean isSubSlotEmpty(int slot) {
        return this.inv.get(slot).isEmpty();
    }


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

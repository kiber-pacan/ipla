
package com.akicater.client;
#if MC_VER >= V1_21_9
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
#endif

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;

public class LayingItemBERS #if MC_VER >= V1_21_9 extends BlockEntityRenderState #endif {
    public LayingItemBERS() {}

    // Inventory
    #if MC_VER >= V1_21_9 public List<ItemStackRenderState> inv = Collections.emptyList(); #endif
    #if MC_VER >= V1_21_9 public List<Item> items = Collections.emptyList(); #endif
    public List<Boolean> renderAsBlock = Collections.emptyList();

    // Item rotations
    public NonNullList<Float> rot;
    public NonNullList<Float> lastRot;

    // Quad mode for sides
    public NonNullList<Boolean> quad;

    public Level level;
    public BlockPos pos;
}

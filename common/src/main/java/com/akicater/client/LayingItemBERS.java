#if MC_VER >= V1_21_9
package com.akicater.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class LayingItemBERS extends BlockEntityRenderState {
    // Inventory
    public List<ItemStackRenderState> inv = Collections.emptyList();
    public List<Boolean> isFullBlock = Collections.emptyList();

    // Item rotations
    public NonNullList<Float> rot;
    public NonNullList<Float> lastRot;

    // Quad mode for sides
    public NonNullList<Boolean> quad;

    public LayingItemBERS() {
    }
}
#endif
#if MC_VER >= V1_21_9
package com.akicater.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

import java.util.Collections;
import java.util.List;

public class LayingItemBERS extends BlockEntityRenderState {
    public List<ItemStackRenderState> items = Collections.emptyList();
    public List<Boolean> fullBlock = Collections.emptyList();
    public Direction facing;

    public LayingItemBERS() {
        this.facing = Direction.NORTH;
    }
}
#endif
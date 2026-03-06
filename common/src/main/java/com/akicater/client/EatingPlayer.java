package com.akicater.client;


import com.akicater.blocks.LayingItemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface EatingPlayer {
    int ipla$getEatingTicks();
    void ipla$tickEating();
    void ipla$setEatingTicks(int ipla$eatingTicks);

    ItemStack ipla$getTargetFood();
    void ipla$setTargetFood(ItemStack food);

    boolean ipla$isEating();
    void ipla$setEating(boolean eating);

    BlockPos ipla$getFoodPos();
    void ipla$setFoodPos(BlockPos pos);

    void ipla$setHit(Integer hits);
    Integer ipla$getHit();

    void ipla$setLayingItemEntity(LayingItemEntity entity);
    LayingItemEntity ipla$getLayingItemEntity();
}
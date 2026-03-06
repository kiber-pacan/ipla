package com.akicater.client.mixin;

import com.akicater.blocks.LayingItemEntity;
import com.akicater.client.EatingPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(Player.class)
public class PlayerMixin implements EatingPlayer {
    @Unique private Integer ipla$eatingIplaTicks = 0;
    @Unique private ItemStack ipla$targetIplaFood = null;
    @Unique private Boolean ipla$isEatingIpla = false;
    @Unique private BlockPos ipla$foodPos = null;
    @Unique private Integer ipla$hit = null;
    @Unique private LayingItemEntity ipla$layingItemEntity = null;

    @Override
    public int ipla$getEatingTicks() {
        return this.ipla$eatingIplaTicks;
    }

    @Override
    public void ipla$tickEating() {
        this.ipla$eatingIplaTicks += 1;
    }

    public void ipla$setEatingTicks(int ipla$eatingTicks) {
        this.ipla$eatingIplaTicks = ipla$eatingTicks;
    }

    @Override
    public ItemStack ipla$getTargetFood() {
        return this.ipla$targetIplaFood;
    }

    @Override
    public void ipla$setTargetFood(ItemStack food) {
        this.ipla$targetIplaFood = food;
    }

    @Override
    public boolean ipla$isEating() {
        return this.ipla$isEatingIpla;
    }

    @Override
    public void ipla$setEating(boolean eating) {
        this.ipla$isEatingIpla = eating;
    }


    @Override
    public BlockPos ipla$getFoodPos() {
        return this.ipla$foodPos;
    }
    @Override
    public void ipla$setFoodPos(BlockPos pos) {
        this.ipla$foodPos = pos;
    }


    @Override
    public void ipla$setHit(Integer hit) {
        this.ipla$hit = hit;
    }

    @Override
    public Integer ipla$getHit() {
        return this.ipla$hit;
    }


    @Override
    public void ipla$setLayingItemEntity(LayingItemEntity entity) {
        this.ipla$layingItemEntity = entity;
    }
    @Override
    public LayingItemEntity ipla$getLayingItemEntity() {
        return this.ipla$layingItemEntity;
    }
}

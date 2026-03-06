package com.akicater.network;

#if MC_VER >= V1_21
import com.akicater.IPLA;
import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;
#endif

import com.akicater.IPLA_Methods;
import com.akicater.client.EatingPlayer;
import net.minecraft.world.entity.player.Player;

public #if MC_VER >= V1_21 record #else class #endif ItemEatPayload #if MC_VER >= V1_21 (Boolean ipla$isEatingIpla) implements CustomPacketPayload #endif {
    #if MC_VER >= V1_21
    public static final Type<ItemEatPayload> TYPE = new Type<>(#if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath(IPLA.MOD_ID, "eat_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemEatPayload> CODEC = StreamCodec.of((buf, value) -> buf.writeBoolean(value.ipla$isEatingIpla), buf -> new ItemEatPayload(buf.readBoolean()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    #endif

    public static void receive(Player player, Boolean isEating) {
        EatingPlayer eatingPlayer = (EatingPlayer) player;

        if (!isEating) {
            IPLA_Methods.clearEating(player);
        } else {
            eatingPlayer.ipla$setEating(isEating);
        }
    }
}


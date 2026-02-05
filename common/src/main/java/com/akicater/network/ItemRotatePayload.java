package com.akicater.network;

#if MC_VER >= V1_21
import com.akicater.IPLA;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
#endif

#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif

import com.akicater.blocks.LayingItemEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import static com.akicater.IPLA.*;

public #if MC_VER >= V1_21 record #else class #endif ItemRotatePayload #if MC_VER >= V1_21 (int y, float rotationDegrees, BlockHitResult hitResult) implements CustomPacketPayload #endif {
    #if MC_VER >= V1_21
    public static final Type<ItemRotatePayload> TYPE = new Type<>(#if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath(MOD_ID, "rotate_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemRotatePayload> CODEC = StreamCodec.of((buf, value) -> buf.writeInt(value.y).writeFloat(value.rotationDegrees).writeBlockHitResult(value.hitResult), buf -> new ItemRotatePayload(buf.readInt(), buf.readFloat(), buf.readBlockHitResult()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    #endif

    public static void receive(Player player, int y, float rotationDegrees, BlockHitResult hitResult) {
        Level level = player #if MC_VER < V1_20_1 .level #else .level() #endif;
        LayingItemEntity entity;

        if ((entity = (LayingItemEntity) level.getChunk(hitResult.getBlockPos()).getBlockEntity(hitResult.getBlockPos())) != null) {
            List<Integer> slots = getPreciseIndexFromHit(entity, hitResult, false);
            for (int rawSlot : slots) {
                boolean quad = entity.quad.get((int) rawSlot / 4);
                int slot = ((quad) ? rawSlot : rawSlot - rawSlot % 4);

                float rotatedDegrees = (entity.rot.get(slot) + rotationDegrees * y);
                float flooredDegrees = rotatedDegrees - (rotatedDegrees % rotationDegrees);

                entity.rot.set(slot, flooredDegrees);

                entity.markDirty();
            }
        }
    }
}
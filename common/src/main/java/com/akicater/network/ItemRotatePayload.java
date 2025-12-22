package com.akicater.network;

#if MC_VER >= V1_21
import com.akicater.IPLA;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
#endif

import com.akicater.blocks.LayingItemEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import static com.akicater.IPLA.*;

public #if MC_VER >= V1_21 record #else class #endif ItemRotatePayload #if MC_VER >= V1_21 (int y, BlockHitResult hitResult) implements CustomPacketPayload #endif {
    #if MC_VER >= V1_21
    public static final Type<ItemRotatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rotate_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemRotatePayload> CODEC = StreamCodec.of((buf, value) -> buf.writeInt(value.y).writeBlockHitResult(value.hitResult), buf -> new ItemRotatePayload(buf.readInt(), buf.readBlockHitResult()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    #endif

    public static void receive(Player player, int y, BlockHitResult hitResult) {
        Level level = player #if MC_VER < V1_20_1 .level #else .level() #endif;
        LayingItemEntity entity;

        if ((entity = (LayingItemEntity) level.getChunk(hitResult.getBlockPos()).getBlockEntity(hitResult.getBlockPos())) != null) {
            Pair<Integer, Integer> pair = getIndexFromHit(hitResult, false);

            boolean quad = entity.quad.get(pair.getFirst());
            int x = pair.getFirst() * 4 + ((quad) ? pair.getSecond() : 0);

            float rotationDegrees = config.getRotationDegrees();
            float rotatedDegrees = (entity.rot.get(x) + rotationDegrees * y);
            float flooredDegrees = rotatedDegrees - (rotatedDegrees % rotationDegrees);

            entity.rot.set(x, flooredDegrees);

            entity.markDirty();
        }
    }
}
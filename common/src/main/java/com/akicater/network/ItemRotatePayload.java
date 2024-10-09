package com.akicater.network;

#if MC_VER >= V1_21

import com.akicater.blocks.LayingItemEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import static com.akicater.Ipla.*;

public record ItemRotatePayload(float degrees, int y, boolean rounded, BlockHitResult hitResult) implements CustomPacketPayload {
    public static final Type<ItemRotatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rotate_item"));
    public static final StreamCodec<FriendlyByteBuf, ItemRotatePayload> CODEC = StreamCodec.of((buf, value) -> buf.writeFloat(value.degrees).writeInt(value.y).writeBoolean(value.rounded).writeBlockHitResult(value.hitResult), buf -> new ItemRotatePayload(buf.readFloat(), buf.readInt(), buf.readBoolean(), buf.readBlockHitResult()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(Player player, float degrees, int y, boolean rounded, BlockHitResult hitResult) {
        Level world = player.level();
        LayingItemEntity entity;

        if ((entity = (LayingItemEntity) world.getBlockEntity(hitResult.getBlockPos())) != null) {
            Pair<Integer, Integer> pair = getIndexFromHit(hitResult, false);

            boolean quad = entity.quad.get(pair.getFirst());
            int x = pair.getFirst() * 4 + ((quad) ? pair.getSecond() : 0);

            if (rounded) {
                entity.rot.set(x, (entity.rot.get(x) - entity.rot.get(x) % 22.5f) + 22.5f * y);
            } else {
                entity.rot.set(x, entity.rot.get(x) + degrees * y);
            }

            entity.markDirty();
        }
    }
}
#endif
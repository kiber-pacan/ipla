package com.akicater;

import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
#if MC_VER >= V1_21
import com.akicater.network.ItemPlacePayload;
import com.akicater.network.ItemRotatePayload;
#endif
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class Ipla {
    public static final String MOD_ID = "ipla";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final Registrar<Block> blocks = MANAGER.get().get(Registries.BLOCK);
    public static final Registrar<BlockEntityType<?>> blockEntities = MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);

    public static RegistrySupplier<LayingItem> lItemBlock;
    public static RegistrySupplier<BlockEntityType<LayingItemEntity>> lItemBlockEntity;

    public static KeyMapping PLACE_ITEM_KEY;
    public static KeyMapping ROTATE_ITEM_KEY;
    public static KeyMapping HIDE_ITEM_KEY;
    public static KeyMapping ROTATE_ROUNDED_ITEM_KEY;

    public static final Random RANDOM = new Random();

    public static ResourceLocation PLACE_ITEM;
    public static ResourceLocation ROTATE_ITEM;

    public static void initializeServer() {
        lItemBlock = blocks.register(#if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(MOD_ID, "l_item"), () -> new LayingItem(BlockBehaviour.Properties.of(#if MC_VER <= V1_20_1 Material.AIR #endif).instabreak().dynamicShape().noOcclusion()));

        lItemBlockEntity = blockEntities.register(
                #if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(MOD_ID, "l_item_entity"),
                () -> BlockEntityType.Builder.of(LayingItemEntity::new, lItemBlock.get()).build(null)
        );

        #if MC_VER >= V1_21
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ItemPlacePayload.TYPE, ItemPlacePayload.CODEC, (buf, context) ->
                ItemPlacePayload.receive(context.getPlayer(), buf.pos(), buf.hitResult())
        );

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ItemRotatePayload.TYPE, ItemRotatePayload.CODEC, (buf, context) ->
                ItemRotatePayload.receive(context.getPlayer(), buf.degrees(), buf.y(), buf.rounded(), buf.hitResult())
        );
        #else
        PLACE_ITEM = new ResourceLocation(MOD_ID, "place_item");
        ROTATE_ITEM = new ResourceLocation(MOD_ID, "rotate_item");

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PLACE_ITEM, (buf, context) -> {
            BlockPos pos = buf.readBlockPos();
            BlockHitResult hitResult = buf.readBlockHitResult();

            ItemStack stack = context.getPlayer().getMainHandItem();

            if (stack == ItemStack.EMPTY) return;

            Player player = context.getPlayer();

            Level level = #if MC_VER <= V1_20_1 player.level #else player.level() #endif;


            BlockPos tempPos = pos;
            if (level.getBlockState(pos).getBlock() != Ipla.lItemBlock.get()) {
                tempPos = pos.relative(hitResult.getDirection(), 1);
            }

            Block replBlock = level.getBlockState(tempPos).getBlock();
            int i = hitResult.getDirection().get3DDataValue();

            Random random = new Random();

            if (replBlock == Blocks.AIR || replBlock == Blocks.WATER) {
                BlockState state = Ipla.lItemBlock.get().defaultBlockState();

                if (replBlock == Blocks.WATER) {
                    state = state.setValue(BlockStateProperties.WATERLOGGED, true);
                }

                level.setBlockAndUpdate(tempPos, state);

                LayingItemEntity entity = (LayingItemEntity)level#if MC_VER < V1_21 .getChunk(tempPos) #endif.getBlockEntity(tempPos);

                if (entity != null) {
                    if (player.isDiscrete()) {
                        Pair<Integer,Integer> pair = Ipla.getIndexFromHit(hitResult, true);

                        int x = pair.getFirst() * 4 + pair.getSecond();

                        entity.setItem(x, stack);
                        entity.quad.set(i, true);
                        entity.rot.set(x, random.nextFloat(-360, 360));
                    } else {
                        entity.setItem(i * 4, stack);
                        entity.rot.set(i * 4, random.nextFloat(-360, 360));
                    }

                    level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.4f, 1);

                    entity.markDirty();
                }
            } else if (level.getBlockState(tempPos).getBlock() == Ipla.lItemBlock.get()) {
                LayingItemEntity entity = (LayingItemEntity)level#if MC_VER < V1_21 .getChunk(tempPos) #endif.getBlockEntity(tempPos);

                if (entity != null) {
                    boolean hitIsLItem = tempPos != pos;

                    Pair<Integer,Integer> pair = Ipla.getIndexFromHit(hitResult, hitIsLItem);
                    boolean quad = entity.quad.get(pair.getFirst()) || player.isDiscrete();

                    int x = pair.getFirst() * 4 + ((quad) ? pair.getSecond() : 0);

                    if(entity.inv.get(x).isEmpty()) {
                        entity.setItem(x, stack);
                        entity.rot.set(x, random.nextFloat(-360, 360));

                        if (quad)
                            entity.quad.set(pair.getFirst(), true);

                        level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.4f, 1);
                    }

                    entity.markDirty();
                }
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ROTATE_ITEM, (buf, context) -> {
            Level world = #if MC_VER <= V1_20_1 context.getPlayer().level #else player.level() #endif;

            float degrees = buf.readFloat();
            int y = buf.readInt();
            boolean rounded = buf.readBoolean();
            BlockHitResult hitResult = buf.readBlockHitResult();

            LayingItemEntity entity;

            if ((entity = (LayingItemEntity) world#if MC_VER < V1_21 .getChunk(hitResult.getBlockPos()) #endif.getBlockEntity(hitResult.getBlockPos())) != null) {
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
        });

		#endif
    }

    public static void initializeClient() {
        PLACE_ITEM_KEY = new KeyMapping(
                "key.ipla.place_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_V,
                "key.categories.ipla"
        );

        ROTATE_ITEM_KEY = new KeyMapping(
                "key.ipla.rotate_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LALT,
                "key.categories.ipla"
        );

        HIDE_ITEM_KEY = new KeyMapping(
                "key.ipla.retrieve_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_B,
                "key.categories.ipla"
        );

        ROTATE_ROUNDED_ITEM_KEY = new KeyMapping(
                "key.ipla.rotate_rounded_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_Z,
                "key.categories.ipla"
        );

        KeyMappingRegistry.register(PLACE_ITEM_KEY);
        KeyMappingRegistry.register(ROTATE_ITEM_KEY);
        KeyMappingRegistry.register(HIDE_ITEM_KEY);
        KeyMappingRegistry.register(ROTATE_ROUNDED_ITEM_KEY);

        ClientTickEvent.CLIENT_POST.register(client -> {
            if (PLACE_ITEM_KEY.consumeClick()) {
                if (client.hitResult instanceof BlockHitResult && client.player.getItemInHand(InteractionHand.MAIN_HAND) != ItemStack.EMPTY && Minecraft.getInstance().level.getBlockState(((BlockHitResult) client.hitResult).getBlockPos()).getBlock() != Blocks.AIR) {
					#if MC_VER < V1_21
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

                    buf.writeBlockPos(((BlockHitResult) client.hitResult).getBlockPos());
                    buf.writeBlockHitResult((BlockHitResult) client.hitResult);

                    NetworkManager.sendToServer(PLACE_ITEM, buf);
					#else
                    ItemPlacePayload payload = new ItemPlacePayload(
                            ((BlockHitResult) client.hitResult).getBlockPos(),
                            (BlockHitResult) client.hitResult
                    );

                    NetworkManager.sendToServer(payload);
					#endif
                }
            }
        });

        ClientRawInputEvent.MOUSE_SCROLLED.register((Minecraft minecraft, #if MC_VER > V1_20_1 double x, #endif double y) -> {
            BlockHitResult hitResult = getBlockHitResult(minecraft.hitResult);
            boolean rounded = false;

            if (hitResult != null && ROTATE_ITEM_KEY.isDown()) {
                if (ROTATE_ROUNDED_ITEM_KEY.isDown()) rounded = true;

                if (minecraft.level != null && minecraft.level.getBlockState(hitResult.getBlockPos()).getBlock() == lItemBlock.get()) {
                                    #if MC_VER < V1_21
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

                    buf.writeFloat(RANDOM.nextFloat(18, 22));
                    buf.writeInt((int) y);
                    buf.writeBoolean(rounded);
                    buf.writeBlockHitResult(hitResult);

                    NetworkManager.sendToServer(ROTATE_ITEM, buf);
                #else
                ItemRotatePayload payload = new ItemRotatePayload(
                        RANDOM.nextFloat(18, 22),
                        (int) y,
                        rounded,
                        hitResult
                );

                NetworkManager.sendToServer(payload);
                #endif

                    if (#if MC_VER < V1_20_4 Platform.isForge() #else Platform.isForgeLike() #endif)
                        return EventResult.interruptFalse();
                    else
                        return EventResult.interruptTrue();
                }
            }

            return EventResult.interruptDefault();
        });
    }

    static List<AABB> boxes = new ArrayList<>(
            List.of(
                    new AABB(0.125f, 0.875f, 0.125f, 0.875f, 1.0f, 0.875f),
                    new AABB(0.125f, 0.0f, 0.125f, 0.875f, 0.125f, 0.875f),
                    new AABB(0.125f, 0.125f, 0.875f, 0.875f, 0.875f, 1.0f),
                    new AABB(0.125f, 0.125f, 0.0f, 0.875f, 0.875f, 0.125f),
                    new AABB(0.875f, 0.125f, 0.125f, 1.0f, 0.875f, 0.875f),
                    new AABB(0.0f, 0.125f, 0.125f, 0.125f, 0.875f, 0.875f)
        )
    );

    public static BlockHitResult getBlockHitResult(HitResult hit) {
        if (hit.getType() == HitResult.Type.BLOCK) {
            return (BlockHitResult) hit;
        }
        return null;
    }

    static boolean contains(float x, float y, float z, AABB box) {
        return x >= box.minX
                && x <= box.maxX
                && y >= box.minY
                && y <= box.maxY
                && z >= box.minZ
                && z <= box.maxZ;
    }

    public static int getSlotFromShape(float x, float y, float z) {
        for (int i = 0; i < boxes.size(); i++) {
            if (contains(x, y, z, boxes.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static Pair<Integer, Integer> getIndexFromHit(BlockHitResult hit, Boolean empty) {
        BlockPos blockPos = hit.getBlockPos();
        Vec3 pos = hit.getLocation();

        float x = (float) Math.abs(pos.x - blockPos.getX());
        float y = (float) Math.abs(pos.y - blockPos.getY());
        float z = (float) Math.abs(pos.z - blockPos.getZ());

        int slot;

        if (empty) {
            slot = hit.getDirection().get3DDataValue();
        } else {
            slot = getSlotFromShape(x, y, z);
        }

        switch (slot) {
            case 0, 1 -> {
                return new Pair<>(slot, ((slot == 1) ? getIndexFromXY(x, 1 - z) : getIndexFromXY(x, z)));
            }
            case 2, 3 -> {
                return new Pair<>(slot, ((slot == 2) ? getIndexFromXY(1 - x, y) : getIndexFromXY(x, y)));
            }
            case 4, 5 -> {
                return new Pair<>(slot, ((slot == 5) ? getIndexFromXY(1 - z, y) : getIndexFromXY(z, y)));
            }
        }

        return new Pair<>(0,0);
    }

    private static int getIndexFromXY(float a, float b) {
        return ((a > 0.5f) ? 1 : 0) + ((b > 0.5f) ? 2 : 0);
    }
}

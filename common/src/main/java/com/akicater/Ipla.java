package com.akicater;

import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
import com.akicater.client.IplaConfig;

import com.akicater.network.ItemPlacePayload;
import com.akicater.network.ItemRotatePayload;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.hooks.block.BlockEntityHooks;
import io.netty.buffer.Unpooled;
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
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;


#if MC_VER >= V1_21
import com.akicater.network.ItemPlacePayload;
import com.akicater.network.ItemRotatePayload;
#endif

#if MC_VER >= V1_19_4
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.core.registries.Registries;
#endif

#if MC_VER >= V1_19_2 && MC_VER < V1_20_4
import eu.midnightdust.lib.config.MidnightConfig;
#endif

#if MC_VER <= V1_19_2
import dev.architectury.registry.registries.Registries;
#endif

#if MC_VER < V1_20_1
import net.minecraft.world.level.material.Material;
#endif

#if MC_VER <= V1_18_2
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import me.shedaniel.autoconfig.AutoConfig;
#endif

public final class Ipla {
    public static final String MOD_ID = "ipla";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    #if MC_VER >= V1_19_4
    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final Registrar<Block> blocks = MANAGER.get().get(Registries.BLOCK);
    public static final Registrar<BlockEntityType<?>> blockEntities = MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);
    #else
    public static final Supplier<Registries> MANAGER = Suppliers.memoize(() -> Registries.get(MOD_ID));

    public static final Registrar<Block> blocks = MANAGER.get().get(Registry.BLOCK);
    public static final Registrar<BlockEntityType<?>> blockEntities = MANAGER.get().get(Registry.BLOCK_ENTITY_TYPE);
    #endif

    public static #if MC_VER < V1_21_3 RegistrySupplier<LayingItem> #else LayingItem #endif lItemBlock;

    public static #if MC_VER >= V1_21_3 BlockEntityType<LayingItemEntity> #else RegistrySupplier<BlockEntityType<LayingItemEntity>>  #endif lItemBlockEntity;


    public static KeyMapping PLACE_ITEM_KEY;
    public static KeyMapping ROTATE_ITEM_KEY;
    public static KeyMapping ROTATE_ROUNDED_ITEM_KEY;

    public static final Random RANDOM = new Random();

    public static ResourceLocation ITEM_PLACE;
    public static ResourceLocation ITEM_ROTATE;



    public static void initializeServer() {
        #if MC_VER < V1_21_3
        lItemBlock = blocks.register(
                #if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(MOD_ID, "l_item"),
                () -> new LayingItem(BlockBehaviour.Properties.of(#if MC_VER < V1_20_1 Material.AIR #endif)
                        .instabreak()
                        .dynamicShape()
                        .noOcclusion()
                        #if MC_VER >= V1_21_3
                        .setId(key)
                        #endif
                )
        );

        lItemBlockEntity = blockEntities.register(
                #if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(MOD_ID, "l_item_entity"),
                () -> BlockEntityType.Builder.of(LayingItemEntity::new, lItemBlock.get()).build(null)
        );
        #endif

        #if MC_VER >= V1_21
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ItemPlacePayload.TYPE, ItemPlacePayload.CODEC, (buf, context) ->
                ItemPlacePayload.receive(context.getPlayer(), buf.pos(), buf.hitResult())
        );

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ItemRotatePayload.TYPE, ItemRotatePayload.CODEC, (buf, context) ->
                ItemRotatePayload.receive(context.getPlayer(), buf.degrees(), buf.y(), buf.rounded(), buf.hitResult())
        );
        #else
        ITEM_PLACE = new ResourceLocation(MOD_ID, "place_item");
        ITEM_ROTATE = new ResourceLocation(MOD_ID, "rotate_item");

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ITEM_PLACE, (buf, context) -> ItemPlacePayload.receive(context.getPlayer(), buf.readBlockPos(), buf.readBlockHitResult()));

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ITEM_ROTATE, (buf, context) -> ItemRotatePayload.receive(context.getPlayer(), buf.readFloat(), buf.readInt(), buf.readBoolean(), buf.readBlockHitResult()));

		#endif
    }

    public static void initializeClient() {
        #if MC_VER > V1_18_2 && MC_VER < V1_20_4
        MidnightConfig.init(MOD_ID, IplaConfig.class);
        #elif MC_VER <= V1_18_2
            AutoConfig.register(IplaConfig.class, Toml4jConfigSerializer::new);
        #endif

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

        ROTATE_ROUNDED_ITEM_KEY = new KeyMapping(
                "key.ipla.rotate_rounded_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_Z,
                "key.categories.ipla"
        );

        KeyMappingRegistry.register(PLACE_ITEM_KEY);
        KeyMappingRegistry.register(ROTATE_ITEM_KEY);
        KeyMappingRegistry.register(ROTATE_ROUNDED_ITEM_KEY);

        ClientTickEvent.CLIENT_POST.register(client -> {
            if (PLACE_ITEM_KEY.consumeClick()) {
                if (client.hitResult instanceof BlockHitResult && client.player.getItemInHand(InteractionHand.MAIN_HAND) != ItemStack.EMPTY && Minecraft.getInstance().level.getBlockState(((BlockHitResult) client.hitResult).getBlockPos()).getBlock() != Blocks.AIR) {
					#if MC_VER < V1_21
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

                    buf.writeBlockPos(((BlockHitResult) client.hitResult).getBlockPos());
                    buf.writeBlockHitResult((BlockHitResult) client.hitResult);

                    NetworkManager.sendToServer(ITEM_PLACE, buf);
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

                if (minecraft.level != null && minecraft.level.getBlockState(hitResult.getBlockPos()).getBlock() == lItemBlock) {
                    #if MC_VER < V1_21
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

                    buf.writeFloat(RANDOM.nextFloat(18, 22));
                    buf.writeInt((int) y);
                    buf.writeBoolean(rounded);
                    buf.writeBlockHitResult(hitResult);

                    NetworkManager.sendToServer(ITEM_ROTATE, buf);

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

        #if MC_VER >= V1_20_4
        ClientLifecycleEvent.CLIENT_STARTED.register((minecraft) -> {
            IplaConfig.HANDLER.load();
        });

        ClientLifecycleEvent.CLIENT_STOPPING.register((minecraft) -> {
            IplaConfig.HANDLER.save();
        });
        #endif
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

        return new Pair<>(0, 0);
    }

    private static int getIndexFromXY(float a, float b) {
        return ((a > 0.5f) ? 1 : 0) + ((b > 0.5f) ? 2 : 0);
    }
}

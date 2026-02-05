package com.akicater;

import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
import com.akicater.client.IPLA_Config;

import com.akicater.network.ItemPlacePayload;
import com.akicater.network.ItemRotatePayload;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
#endif

#if MC_VER >= V1_21_3
import net.minecraft.resources.ResourceKey;
#endif

import net.minecraft.world.InteractionHand;
#if MC_VER >= V1_20_1
import net.minecraft.world.item.CreativeModeTab;
#endif
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;


#if MC_VER >= V1_21
#endif

#if MC_VER >= V1_19_4
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.core.registries.Registries;
#endif

#if MC_VER <= V1_19_2
import dev.architectury.registry.registries.Registries;
#endif

#if MC_VER < V1_20_1
import net.minecraft.world.level.material.Material;
#endif


public final class IPLA {
    public static final String MOD_ID = "ipla";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    public static IPLA_Config config = new IPLA_Config();

    #if MC_VER >= V1_19_4
        public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));
        public static final Registrar<Block> blocks = MANAGER.get().get(Registries.BLOCK);
        public static final Registrar<Item> items = MANAGER.get().get(Registries.ITEM);
        public static final Registrar<BlockEntityType<?>> blockEntities = MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);
        #if MC_VER >= V1_20_1
        public static final Registrar<CreativeModeTab> itemGroups = MANAGER.get().get(Registries.CREATIVE_MODE_TAB);
        #endif
    #else
    public static final Supplier<Registries> MANAGER = Suppliers.memoize(() -> Registries.get(MOD_ID));
    public static final Registrar<Block> blocks = MANAGER.get().get(Registry.BLOCK_REGISTRY);
    public static final Registrar<Item> items = MANAGER.get().get(Registry.ITEM_REGISTRY);
    public static final Registrar<BlockEntityType<?>> blockEntities = MANAGER.get().get(Registry.BLOCK_ENTITY_TYPE_REGISTRY);
    #endif

    public static RegistrySupplier<LayingItem> lItemBlock;

    public static RegistrySupplier<BlockEntityType<LayingItemEntity>> lItemBlockEntity;


    public static final Random RANDOM = new Random();

    public static #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif ITEM_PLACE;
    public static #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif ITEM_ROTATE;


    public static void initializeServer() {
        #if MC_VER >= V1_21_3 ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, #if MC_VER >= V1_21 #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath #else new ResourceLocation #endif(IPLA.MOD_ID, "l_item")); #endif

        lItemBlock = blocks.register(
                #if MC_VER >= V1_21 #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif .fromNamespaceAndPath #else new ResourceLocation #endif(MOD_ID, "l_item"),
                () -> new LayingItem(BlockBehaviour.Properties.of(#if MC_VER < V1_20_1 Material.AIR #endif)
                        .instabreak()
                        .dynamicShape()
                        .noOcclusion()
                        #if MC_VER >= V1_20_4
                        .noTerrainParticles()
                        #endif
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .isRedstoneConductor((state, level, pos) -> false)
                        #if MC_VER >= V1_21_3
                        .setId(key)
                        #endif
                )
        );
        #if MC_VER < V1_21_3
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
                ItemRotatePayload.receive(context.getPlayer(), buf.y(), buf.rotationDegrees(), buf.hitResult())
        );
        #else
        ITEM_PLACE = new ResourceLocation(MOD_ID, "place_item");
        ITEM_ROTATE = new ResourceLocation(MOD_ID, "rotate_item");

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ITEM_PLACE, (buf, context) -> ItemPlacePayload.receive(context.getPlayer(), buf.readBlockPos(), buf.readBlockHitResult()));

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ITEM_ROTATE, (buf, context) -> ItemRotatePayload.receive(context.getPlayer(), buf.readInt(), buf.readFloat(), buf.readBlockHitResult()));

		#endif
    }

    public static void initializeClient() {
        #if MC_VER >= V1_21_9 KeyMapping.Category category = KeyMapping.Category.register(#if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath(MOD_ID, "ipla")); #endif
        KeyMapping PLACE_ITEM_KEY;
        KeyMapping ROTATE_ITEM_KEY;

        PLACE_ITEM_KEY = new KeyMapping(
                "key.ipla.place_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_V,
                #if MC_VER >= V1_21_9 category #else "key.categories.ipla" #endif
        );

        ROTATE_ITEM_KEY = new KeyMapping(
                "key.ipla.rotate_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LALT,
                #if MC_VER >= V1_21_9 category #else "key.categories.ipla" #endif
        );

        KeyMappingRegistry.register(PLACE_ITEM_KEY);
        KeyMappingRegistry.register(ROTATE_ITEM_KEY);

        ClientTickEvent.CLIENT_POST.register(client -> {
            if (PLACE_ITEM_KEY.consumeClick()) {
                if (client.hitResult instanceof BlockHitResult) {
                    assert Minecraft.getInstance().level != null;
                    Block hittedBlock = Minecraft.getInstance().level.getBlockState(((BlockHitResult) client.hitResult).getBlockPos()).getBlock();
                    assert client.player != null;
                    ItemStack stack = client.player.getItemInHand(InteractionHand.MAIN_HAND);

                    if (stack == ItemStack.EMPTY && (hittedBlock == Blocks.AIR || hittedBlock == Blocks.CAVE_AIR)) return;

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


            if (hitResult != null && ROTATE_ITEM_KEY.isDown()) {
                if (minecraft.level != null && minecraft.level.getBlockState(hitResult.getBlockPos()).getBlock() instanceof LayingItem) {
                    float rotationDegrees = config.getRotationDegrees();

                    #if MC_VER < V1_21
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

                    buf.writeInt((int) y);
                    buf.writeFloat(rotationDegrees);
                    buf.writeBlockHitResult(hitResult);

                    NetworkManager.sendToServer(ITEM_ROTATE, buf);

                    #else
                        ItemRotatePayload payload = new ItemRotatePayload(
                                (int) y,
                                rotationDegrees,
                                hitResult
                        );

                        NetworkManager.sendToServer(payload);
                    #endif

                    if (#if MC_VER < V1_20_4 Platform.isForge() #else Platform.isForgeLike() #endif) {
                        return EventResult.interruptFalse();
                    } else {
                        return EventResult.interruptTrue();
                    }
                }
            }
            return EventResult.interruptDefault();
        });

        ClientLifecycleEvent.CLIENT_STARTED.register((minecraft) -> {
            try {
                config.loadConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            #if LOADER == COMMON LOGGER.info("CUI loader mode: COMMON"); #endif
            #if LOADER == FABRIC LOGGER.info("CUI loader mode: FABRIC"); #endif
            #if LOADER == NEOFORGE LOGGER.info("CUI loader mode: NEOFORGE"); #endif
            #if LOADER == FORGE LOGGER.info("CUI loader mode: FORGE"); #endif
        });

        ClientLifecycleEvent.CLIENT_STOPPING.register((minecraft) -> {
            config.saveConfig();
        });
    }

    static List<AABB> boxes = new ArrayList<>(
            List.of(
                    new AABB(0.0, 1.0 - 1.0 / 16 * 4, 0.0, 1.0, 1.0, 1.0), // TOP
                    new AABB(0.0, 0.0, 0.0, 1.0, 1.0 / 16 * 4, 1.0), // DOWN
                    new AABB(0.0, 0.0, 1.0 - 1.0 / 16 * 4, 1.0, 1.0, 1.0), // SOUTH
                    new AABB(0.0, 0.0, 0, 1.0, 1.0, 1.0 / 16 * 4), // NORTH
                    new AABB(1.0 / 16 * 4, 0.0, 0.0, 1.0 / 16 * 4, 1.0f, 1.0f), // WEST
                    new AABB(1.0 - 1.0 / 16 * 4, 0.0, 0.0, 1.0 - 1.0 / 16 * 4, 1.0f, 1.0f) // EAST
        )


    );

    /*
    static List<AABB> boxes = new ArrayList<>(
            List.of(
                    new AABB(0.0, 1.0 - 1.0 / 16 * 4, 0.0, 1.0, 1.0, 1.0), // TOP
                    new AABB(0.0, 0.0, 0.0, 1.0, 1.0 / 16 * 4, 1.0), // DOWN
                    new AABB(0.0, 0.0, 1.0 - 1.0 / 16 * 4, 1.0, 1.0, 1.0), // SOUTH
                    new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0 / 16 * 4), // NORTH
                    new AABB(1.0 - 1.0 / 16 * 4, 0.0, 0.0, 1.0, 1.0, 1.0), // WEST
                    new AABB(0.0, 0.0, 0.0, 1.0 / 16 * 4, 1.0, 1.0) // EAST
            )
    );
    */

    public static BlockHitResult getBlockHitResult(HitResult hit) {
        if (hit.getType() == HitResult.Type.BLOCK) {
            return (BlockHitResult) hit;
        }
        return null;
    }

    static final double EPS = 1e-6;

    private static boolean contains(double x, double y, double z, AABB box) {
        return x >= box.minX - EPS && x <= box.maxX + EPS
                && y >= box.minY - EPS && y <= box.maxY + EPS
                && z >= box.minZ - EPS && z <= box.maxZ + EPS;
    }


    public static int getSlotFromShape(double x, double y, double z) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < boxes.size(); i++) {
            if (contains(x, y, z, boxes.get(i))) {
                return i;
            }
        }

        return -1;
    }

    private static int getSubSlotFromPos(int slot, double x, double y, double z) {
        switch (slot) {
            case 0, 1 -> {
                return (slot == 1) ? getIndexFromXY(x, 1 - z) : getIndexFromXY(x, z);
            }
            case 2, 3 -> {
                return (slot == 2) ? getIndexFromXY(1 - x, y) : getIndexFromXY(x, y);
            }
            case 4, 5 -> {
                return (slot == 5) ? getIndexFromXY(1 - z, y) : getIndexFromXY(z, y);
            }
        }

        return 0;
    }

    public static int getSlotFromHit(BlockHitResult hit, boolean empty, boolean quad) {
        BlockPos blockPos = hit.getBlockPos();
        Vec3 pos = hit.getLocation();

        double x = Math.abs(pos.x - blockPos.getX());
        double y = Math.abs(pos.y - blockPos.getY());
        double z = Math.abs(pos.z - blockPos.getZ());

        int slot;

        if (empty) {
            slot = hit.getDirection().get3DDataValue();
        } else {
            slot = getSlotFromShape(x, y, z);
        }

        return slot * 4 + ((quad) ? getSubSlotFromPos(slot, x, y, z) : 0);
    }

    public static List<Integer> getPreciseIndexFromHit(LayingItemEntity entity, BlockHitResult hit, Boolean empty) {
        List<Integer> list = new ArrayList<>(0);

        for (int i = 0; i < entity.inv.size(); i++) {
            ItemStack stack = entity.inv.get(i);
            boolean cuboid = entity.isCuboid(i);
            boolean quad = entity.quad.get((int) i / 4);

            if (!stack.isEmpty()) {
                BlockPos blockPos = hit.getBlockPos();
                Vec3 pos = hit.getLocation();

                double x = Math.abs(pos.x - blockPos.getX());
                double y = Math.abs(pos.y - blockPos.getY());
                double z = Math.abs(pos.z - blockPos.getZ());

                boolean contains = contains(x, y, z, ((quad) ? ((cuboid) ? LayingItemEntity.basicQuadShapesBlock.get(i) : LayingItemEntity.basicQuadShapesItem.get(i)) : ((cuboid) ? LayingItemEntity.basicShapesBlock.get((int) i / 4) : LayingItemEntity.basicShapesItem.get((int) i / 4))).bounds());

                if (contains) {
                    list.add(i);
                }
            }
        }

        return list;
    }

    private static int getIndexFromXY(double a, double b) {
        return ((a > 0.5) ? 1 : 0) + ((b > 0.5) ? 2 : 0);
    }
}

package com.akicater;

import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
import com.akicater.network.ItemPlacePayload;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
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
    public static KeyMapping RETRIEVE_ITEM_KEY;

    public static void initiliazeServer() {
        lItemBlock = blocks.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "l_item"), () -> new LayingItem(BlockBehaviour.Properties.of().instabreak().dynamicShape().noOcclusion()));

        lItemBlockEntity = blockEntities.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "l_item_entity"),
                () -> BlockEntityType.Builder.of(LayingItemEntity::new, lItemBlock.get()).build(null)
        );

        #if MC_VER >= V1_21
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ItemPlacePayload.TYPE, ItemPlacePayload.CODEC, (buf, context) ->
                ItemPlacePayload.receive(context.getPlayer(), buf.pos(), buf.hitResult())
        );
		#endif
    }
    public static void initiliazeClient() {
        PLACE_ITEM_KEY = new KeyMapping(
                "key.ipla.place_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_V,
                "Ipla"
        );

        ROTATE_ITEM_KEY = new KeyMapping(
                "key.ipla.rotate_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LALT,
                "Ipla"
        );

        RETRIEVE_ITEM_KEY = new KeyMapping(
                "key.ipla.retrieve_item_key",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_B,
                "Ipla"
        );

        ClientTickEvent.CLIENT_POST.register(client -> {
            if (PLACE_ITEM_KEY.consumeClick()) {
                if (client.hitResult instanceof BlockHitResult && client.player.getItemInHand(InteractionHand.MAIN_HAND) != ItemStack.EMPTY && Minecraft.getInstance().level.getBlockState(((BlockHitResult) client.hitResult).getBlockPos()).getBlock() != Blocks.AIR) {
					#if MC_VER < V1_21
                            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeBlockPos(pos.offset(side, 1));
                    buf.writeBlockHitResult((BlockHitResult) client.hitResult);
                    NetworkManager.sendToServer(ITEMPLACE, buf);
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

    static boolean contains(float x, float y, float z, AABB box) {
        return x >= box.minX
                && x <= box.maxX
                && y >= box.minY
                && y <= box.maxY
                && z >= box.minZ
                && z <= box.maxZ;
    }

    public static Pair<Integer, Integer> getIndexFromHit(BlockHitResult hit, boolean empty) {
        BlockPos blockPos = hit.getBlockPos();
        Vec3 pos = hit.getLocation();

        float x = (float) Math.abs(pos.x - blockPos.getX());
        float y = (float) Math.abs(pos.y - blockPos.getY());
        float z = (float) Math.abs(pos.z - blockPos.getZ());

        int slot = 0;
        if (empty) {
            slot = hit.getDirection().get3DDataValue();
        } else {
            for (int i = 0; i < boxes.size(); i++) {
                if (contains(x, y, z, boxes.get(i))) {
                    slot = i;
                }
            }
        }

        switch (slot) {
            case 0, 1 -> {
                return new Pair<>(slot, getIndexFromXY(x, z));
            }
            case 2, 3 -> {
                return new Pair<>(slot, getIndexFromXY(x, y));
            }
            case 4, 5 -> {
                return new Pair<>(slot, getIndexFromXY(z, y));
            }
        }
        return new Pair<>(0,0);
    }

    private static int getIndexFromXY(float a, float b) {
        return ((a > 0.5f) ? 1 : 0) + ((b > 0.5f) ? 2 : 0);
    }
}

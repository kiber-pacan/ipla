package com.akicater;

import com.akicater.blocks.LayingItem;
import com.akicater.client.EatingPlayer;
import com.akicater.client.IPLA_Config;
import com.akicater.network.ItemEatPayload;
import com.akicater.network.ItemPlacePayload;
import com.akicater.network.ItemRotatePayload;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
#if MC_VER >= V1_21
import net.minecraft.core.component.DataComponents;
#else
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
#endif

#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif


import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import java.io.IOException;

import static com.akicater.IPLA.LOGGER;

public class IPLA_Client {
    public static IPLA_Config config = new IPLA_Config();

    public static void initializeClient() {
        #if MC_VER >= V1_21_9 KeyMapping.Category category = KeyMapping.Category.register(#if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath(IPLA.MOD_ID, "ipla")); #endif
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

                    NetworkManager.sendToServer(IPLA.ITEM_PLACE, buf);
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
            BlockHitResult hitResult = IPLA_Methods.getBlockHitResult(minecraft.hitResult);


            if (hitResult != null && ROTATE_ITEM_KEY.isDown()) {
                if (minecraft.level != null && minecraft.level.getBlockState(hitResult.getBlockPos()).getBlock() instanceof LayingItem) {
                    float rotationDegrees = config.getRotationDegrees();

                    #if MC_VER < V1_21
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

                    buf.writeInt((int) y);
                    buf.writeFloat(rotationDegrees);
                    buf.writeBlockHitResult(hitResult);

                    NetworkManager.sendToServer(IPLA.ITEM_ROTATE, buf);

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

            #if LOADER == COMMON LOGGER.info("IPLA loader mode: COMMON"); #endif
            #if LOADER == FABRIC LOGGER.info("IPLA loader mode: FABRIC"); #endif
            #if LOADER == NEOFORGE LOGGER.info("IPLA loader mode: NEOFORGE"); #endif
            #if LOADER == FORGE LOGGER.info("IPLA loader mode: FORGE"); #endif
        });

        ClientLifecycleEvent.CLIENT_STOPPING.register((minecraft) -> config.saveConfig());

        ClientRawInputEvent.MOUSE_CLICKED_POST.register(
                #if MC_VER <= V1_21_8
                (Minecraft client, int button, int action, int mods)
                #else
                (client, buttonInfo, action)
                #endif -> {
            if (client.player == null) return EventResult.interruptFalse();
            boolean emptyHand = client.player.getMainHandItem().isEmpty();

            boolean rightButton = #if MC_VER <= V1_21_8 button #else buttonInfo.button() #endif == 1;
            boolean hasHitResult = client.hitResult != null;

            if (!rightButton || !hasHitResult || !emptyHand) {
                return EventResult.interruptFalse();
            }

            Player player = client.player;
            Level level = client.level;

            BlockHitResult hitResult = IPLA_Methods.getBlockHitResult(client.hitResult);

            if (action == 0 || hitResult == null) {
                IPLA_Methods.clearEating(player);

                #if MC_VER < V1_21
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeBoolean(false);

                NetworkManager.sendToServer(IPLA.ITEM_EAT, buf);
                #else

                ItemEatPayload payload = new ItemEatPayload(false);

                NetworkManager.sendToServer(payload);
                #endif

                ((EatingPlayer) player).ipla$setEating(false);

                return EventResult.interruptFalse();
            } else {
                #if MC_VER < V1_21
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeBoolean(true);

                NetworkManager.sendToServer(IPLA.ITEM_EAT, buf);
                #else

                ItemEatPayload payload = new ItemEatPayload(true);

                NetworkManager.sendToServer(payload);
                #endif

                ((EatingPlayer) player).ipla$setEating(true);

                return EventResult.interruptFalse();
            }
        });
    }
}

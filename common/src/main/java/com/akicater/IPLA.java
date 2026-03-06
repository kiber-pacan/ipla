package com.akicater;

import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;

import com.akicater.client.EatingPlayer;
import com.akicater.network.ItemEatPayload;
import com.akicater.network.ItemPlacePayload;
import com.akicater.network.ItemRotatePayload;
import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
#if MC_VER >= V1_21_11
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
#else
#if MC_VER >= V1_21
import net.minecraft.core.component.DataComponents;
#endif
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
#endif

#if MC_VER >= V1_21_3
import net.minecraft.resources.ResourceKey;
#endif

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
#if MC_VER >= V1_20_1
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
#endif
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
    public static #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif ITEM_EAT;


    public static void initializeServer() {
        #if MC_VER >= V1_21_3 ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, #if MC_VER >= V1_21 #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath #else new ResourceLocation #endif(IPLA.MOD_ID, "l_item")); #endif

        lItemBlock = blocks.register(
                #if MC_VER >= V1_21 #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif .fromNamespaceAndPath #else new ResourceLocation #endif(MOD_ID, "l_item"),
                () -> new LayingItem(BlockBehaviour.Properties.of(#if MC_VER < V1_20_1 Material.AIR #endif)
                        .instabreak()
                        .dynamicShape()
                        .noOcclusion()
                        .lightLevel(LayingItem::getLuminance)
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

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ItemEatPayload.TYPE, ItemEatPayload.CODEC, (buf, context) ->
                ItemEatPayload.receive(context.getPlayer(), buf.ipla$isEatingIpla())
        );
        #else
        ITEM_PLACE = new ResourceLocation(MOD_ID, "place_item");
        ITEM_ROTATE = new ResourceLocation(MOD_ID, "rotate_item");
        ITEM_EAT = new ResourceLocation(MOD_ID, "eat_item");

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ITEM_PLACE, (buf, context) -> ItemPlacePayload.receive(context.getPlayer(), buf.readBlockPos(), buf.readBlockHitResult()));

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ITEM_ROTATE, (buf, context) -> ItemRotatePayload.receive(context.getPlayer(), buf.readInt(), buf.readFloat(), buf.readBlockHitResult()));

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ITEM_EAT, (buf, context) -> ItemEatPayload.receive(context.getPlayer(), buf.readBoolean()));
		#endif

        TickEvent.Player.PLAYER_POST.register((player) -> {
            Vec3 pos = player.getEyePosition();
            BlockPos blockPos = ((EatingPlayer) player).ipla$getFoodPos();

            double distance = (blockPos != null) ? ((EatingPlayer) player).ipla$getFoodPos().distToCenterSqr(pos.x, pos.y, pos.z) : 0;

            ItemStack targetFood = ((EatingPlayer) player).ipla$getTargetFood();
            Level level = #if MC_VER >= V1_20_1 player.level(); #else player.level; #endif

            // Preventing eating of certain conditions
            if (player.isDiscrete() || distance >= 16 || !player.canEat(true)) {
                IPLA_Methods.clearEatingPlayer(player);
                return;
            }

            // Maybe this unfucks eating with hunger 100
            if (!player.canEat(true)) {
                IPLA_Methods.clearEating(player);
                return;
            }

            // Eat food
            if (((EatingPlayer) player).ipla$getEatingTicks() >= 32 && !player.isDiscrete()) {
                LayingItemEntity entity = ((EatingPlayer) player).ipla$getLayingItemEntity();

                if (!player.isCreative()) {
                    #if MC_VER >= V1_21_3
                    player.getFoodData().eat(targetFood.get(DataComponents.FOOD));
                    #else
                    player.eat(level, targetFood);
                    #endif

                    // Clear slot
                    entity.inv.set(((EatingPlayer) player).ipla$getHit(), ItemStack.EMPTY);
                } else {
                    level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
                }



                // Clear player eating values
                IPLA_Methods.clearEatingPlayer(player);

                entity.markDirty(player);

                if (entity.isEmpty()) {
                    level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                }
            }

            // Tick
            if (((EatingPlayer) player).ipla$isEating()) {
                double reachDistance = 5.0;
                BlockHitResult hitResult = rayTrace(#if MC_VER >= V1_20_1 player.level() #else player.level #endif, player, reachDistance);

                // If not looking at Block return
                if (hitResult == null) {
                    IPLA_Methods.clearEatingPlayer(player);
                    return;
                }

                BlockPos hitPos = hitResult.getBlockPos();
                LayingItemEntity entity = (LayingItemEntity) level.getChunk(hitPos).getBlockEntity(hitPos);

                // If not looking at LayingItemEntity return
                if (entity == null) {
                    IPLA_Methods.clearEatingPlayer(player);
                    return;
                }

                ItemStack foodStack = ItemStack.EMPTY;

                List<Integer> hits = IPLA_Methods.getPreciseIndexFromHit(entity, hitResult, true);
                Integer hit = null;

                // Get first food item
                for (int index : hits) {
                    ItemStack stack = entity.inv.get(index);
                    boolean food = #if MC_VER >= V1_21 ((FoodProperties) stack.get(DataComponents.FOOD)) != null; #else stack.getItem().isEdible(); #endif

                    if (!stack.isEmpty() && food) {
                        hit = index;
                        foodStack = stack;
                    }
                }

                if (foodStack != ItemStack.EMPTY) {
                    ((EatingPlayer) player).ipla$setEating(true);
                    ((EatingPlayer) player).ipla$setTargetFood(foodStack);
                    ((EatingPlayer) player).ipla$setFoodPos(hitPos);
                    ((EatingPlayer) player).ipla$setHit(hit);
                    ((EatingPlayer) player).ipla$setLayingItemEntity(entity);

                    ((EatingPlayer) player).ipla$tickEating();

                    if (((EatingPlayer) player).ipla$getEatingTicks() % 4 == 0 || ((EatingPlayer) player).ipla$getEatingTicks() == 0) {
                        level.playSound(player, hitPos, SoundEvents.GENERIC_EAT #if MC_VER >= V1_21_3 .value() #endif, SoundSource.PLAYERS, 1.0F, 1.0F);
                        IPLA_Methods.spawnItemParticles(player, foodStack, 1);
                    }
                }
            } else {
                IPLA_Methods.clearEating(player);
            }
        });
    }

    public static BlockHitResult rayTrace(Level level, Player player, double distance) {
        Vec3 eyePosition = player.getEyePosition();

        Vec3 lookVector = player.getViewVector(1.0F);

        Vec3 endPosition = eyePosition.add(
                lookVector.x * distance,
                lookVector.y * distance,
                lookVector.z * distance
        );

        ClipContext context = new ClipContext(
                eyePosition,
                endPosition,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        );

        return level.clip(context);
    }
}

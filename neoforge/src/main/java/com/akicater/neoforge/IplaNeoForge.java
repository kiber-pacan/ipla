package com.akicater.neoforge;

import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
import com.akicater.client.IplaConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import com.akicater.Ipla;

#if MC_VER < V1_21
import net.neoforged.neoforge.client.ConfigScreenHandler;
#else
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
#endif

#if MC_VER == V1_20_4
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
#endif

#if MC_VER >= V1_20_4
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;
#endif


@Mod(Ipla.MOD_ID)
public final class IplaNeoForge {
    #if MC_VER >= V1_21_3
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Ipla.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Ipla.MOD_ID);

    public static final ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, #if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(Ipla.MOD_ID, "l_item"));


    public static final DeferredBlock<LayingItem> layingItemBlock = BLOCKS.register(
            "l_item",
            () -> new LayingItem(BlockBehaviour.Properties.of()
                    .instabreak()
                    .dynamicShape()
                    .noOcclusion()
                    .setId(key)
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LayingItemEntity>> layingItemEntity = BLOCK_ENTITY_TYPES.register(
            "l_item_entity",
            () -> new BlockEntityType<>(
                    LayingItemEntity::new,
                    layingItemBlock.get()
            )
    );
    #endif

    public IplaNeoForge(IEventBus modBus, ModContainer container) {
        #if MC_VER >= V1_21_3
        BLOCKS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);

        modBus.addListener(this::onCommonSetup);
        #else
        Ipla.initializeServer();

        if (FMLEnvironment.dist.isClient()) {
            Ipla.initializeClient();
        }
        #endif
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        #if MC_VER >= V1_21_3
        Ipla.lItemBlock = layingItemBlock.get();
        Ipla.lItemBlockEntity = layingItemEntity.get();
        #endif

        Ipla.initializeServer();

        #if MC_VER >= V1_20_4
        if (FMLEnvironment.dist.isClient()) {
            Ipla.initializeClient();
        }

        #else
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Ipla::initializeClient);
        #endif


        #if MC_VER == V1_20_4
        if (FMLEnvironment.dist.isClient()) {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (client, parent) -> IplaConfig.HANDLER.instance().getScreen(parent)
                    )
            );
        }
        #elif MC_VER >= V1_21
        if (FMLEnvironment.dist.isClient()) {
            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> (client, parent) -> IplaConfig.HANDLER.instance().getScreen(parent));
        }
        #endif
    }
}

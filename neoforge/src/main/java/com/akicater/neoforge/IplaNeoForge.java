package com.akicater.neoforge;

import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
import com.akicater.client.screen.IPLA_ConfigScreenBase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import com.akicater.IPLA;

#if MC_VER < V1_21
import net.neoforged.neoforge.client.ConfigScreenHandler;
#else
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
#endif

#if MC_VER == V1_20_4
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
#endif

#if MC_VER >= V1_20_4
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
#endif


@Mod(IPLA.MOD_ID)
public final class IplaNeoForge {
    #if MC_VER >= V1_21_3
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IPLA.MOD_ID);

    #endif

    public IplaNeoForge(IEventBus modBus, ModContainer container) {
        IPLA.initializeServer();

        #if MC_VER >= V1_21_3
        IPLA.lItemBlockEntity = IPLA.blockEntities.register(
                #if MC_VER >= V1_21 #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath #else new ResourceLocation #endif(IPLA.MOD_ID, "l_item_entity"),
                () -> new BlockEntityType<>(LayingItemEntity::new, IPLA.lItemBlock.get())
        );

        #endif

        #if MC_VER >= V1_20_4
        if (FMLEnvironment #if MC_VER >= V1_21_9 .getDist() #else.dist #endif .isClient()) {
            IPLA.initializeClient();
        }
        #else
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> IPLA::initializeClient);
        #endif



        ModLoadingContext.get().registerExtensionPoint(
                #if MC_VER >= V1_21
                IConfigScreenFactory.class,
                () -> (client, parent) -> new IPLA_ConfigScreenBase(parent)
                #else
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                (client, parent) -> new IPLA_ConfigScreen(parent))
                #endif
        );
    }
}

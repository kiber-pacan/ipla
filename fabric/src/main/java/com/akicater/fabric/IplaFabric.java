package com.akicater.fabric;


import com.akicater.IPLA;
import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

#if MC_VER >= V1_21_3
import net.minecraft.core.registries.Registries;
#endif

public final class IplaFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        #if MC_VER >= V1_21_3
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, #if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(IPLA.MOD_ID, "l_item"));

        IPLA.lItemBlock = IPLA.blocks.register(
                #if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(IPLA.MOD_ID, "l_item"),
                () -> new LayingItem(BlockBehaviour.Properties.of(#if MC_VER < V1_20_1 Material.AIR #endif)
                        .instabreak()
                        .dynamicShape()
                        .noOcclusion()
                        #if MC_VER >= V1_21_3
                        .setId(key)
                        #endif
                )
        ).get();

        IPLA.lItemBlockEntity = IPLA.blockEntities.register(
                #if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(IPLA.MOD_ID, "l_item_entity"),
                () -> FabricBlockEntityTypeBuilder.create(LayingItemEntity::new, IPLA.lItemBlock).build(null)
        ).get();
        #endif
        IPLA.initializeServer();
    }
}

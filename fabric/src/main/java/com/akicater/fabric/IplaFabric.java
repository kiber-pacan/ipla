package com.akicater.fabric;


import com.akicater.IPLA;
import com.akicater.blocks.LayingItem;
import com.akicater.blocks.LayingItemEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

#if MC_VER >= V1_21_3
import net.minecraft.core.registries.Registries;
#endif

#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif

public final class IplaFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        IPLA.initializeServer();
        #if MC_VER >= V1_21_3
        IPLA.lItemBlockEntity = IPLA.blockEntities.register(
                #if MC_VER >= V1_21 #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif.fromNamespaceAndPath #else new ResourceLocation #endif(IPLA.MOD_ID, "l_item_entity"),
                () -> FabricBlockEntityTypeBuilder.create(LayingItemEntity::new, IPLA.lItemBlock.get()).build(null)
        );
        #endif
    }
}

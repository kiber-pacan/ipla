package com.akicater;

import com.akicater.blocks.LayingItemEntity;
import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public final class Ipla {
    public static final String MOD_ID = "ipla";

    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final Registrar<Block> blocks = MANAGER.get().get(Registries.BLOCK);
    public static final Registrar<BlockEntityType<?>> blockEntities = MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<Block> lItemBlock = blocks.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "l_item"), () -> new Block(BlockBehaviour.Properties.of().instabreak().noOcclusion()));
    public static final RegistrySupplier<BlockEntityType<BannerBlockEntity>> lItemBlockEntity = blockEntities.register(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "l_item_entity"),
            () -> BlockEntityType.Builder.of(BannerBlockEntity::new, lItemBlock.get()).build(null)
        );

    public static void initiliazeServer() {

    }
    public static void initiliazeClient() {

    }
}

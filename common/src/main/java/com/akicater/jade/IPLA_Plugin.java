package com.akicater.jade;

import com.akicater.IPLA;
import com.akicater.blocks.LayingItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class IPLA_Plugin implements IWailaPlugin {
    public static Identifier TEST = Identifier.fromNamespaceAndPath(IPLA.MOD_ID, "ipla_test");

    @Override
    public void register(IWailaCommonRegistration registration) {
        //TODO register data providers and hiding things here
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(IPLA_ComponentProvider.INSTANCE, LayingItem.class);
    }
}
package com.akicater.forge.jade;

import com.akicater.IPLA;
import com.akicater.blocks.LayingItem;

#if MC_VER <= V1_18_2
import mcp.mobius.waila.api.*;
import net.minecraft.resources. #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
#endif

#if MC_VER <= V1_18_2 @WailaPlugin #endif
public class IPLA_PluginForge #if MC_VER <= V1_18_2 implements IWailaPlugin #endif {
    #if MC_VER <= V1_18_2
    public static #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif IPLA_JADE = #if MC_VER >= V1_21_11 Identifier.fromNamespaceAndPath #else #if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif #endif(IPLA.MOD_ID, "ipla_jade");

    @Override
    public void register(IWailaCommonRegistration registration) {
        //TODO register data providers and hiding things here
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider((IComponentProvider) IPLA_ComponentProviderForge.INSTANCE, TooltipPosition.HEAD, LayingItem.class);
    }
    #endif
}
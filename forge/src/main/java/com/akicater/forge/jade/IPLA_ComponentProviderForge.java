package com.akicater.forge.jade;


import com.akicater.blocks.LayingItemEntity;


import net.minecraft.network.chat.Component;
import net.minecraft.resources. #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.shapes.Shapes;

#if MC_VER <= V1_18_2
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.addons.core.BaseBlockProvider;
#endif
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class IPLA_ComponentProviderForge #if MC_VER <= V1_18_2 extends BaseBlockProvider #endif {
    public static final IPLA_ComponentProviderForge INSTANCE = new IPLA_ComponentProviderForge();
    #if MC_VER <= V1_18_2
    @Override
    public void appendTooltip(
            ITooltip tooltip,
            BlockAccessor accessor,
            IPluginConfig config
    ) {
        LayingItemEntity entity = (LayingItemEntity) accessor.getBlockEntity();
        if (entity == null) return;


        tooltip.add(Component.nullToEmpty(""));
        for (int i = 0; i < 6; i++) {
            for (int i1 = 0; i1 < 4; i1++) {
                ItemStack stack = entity.inv.get(i * 4 + i1);
                if (!stack.isEmpty()) tooltip.append(#if MC_VER >= V1_21_6 JadeUI.item #else #if MC_VER > V1_18_2 IElementHelper.get() #else tooltip.getElementHelper() #endif .item #endif(stack, (entity.quad.get(i)) ? 0.5f : 0.8f));
            }
        }
    }
    #endif
}
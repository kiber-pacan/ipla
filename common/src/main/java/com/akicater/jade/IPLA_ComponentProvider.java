package com.akicater.jade;


import com.akicater.blocks.LayingItemEntity;


import net.minecraft.network.chat.Component;
import net.minecraft.resources. #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.shapes.Shapes;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
#if MC_VER >= V1_21_6
import snownee.jade.api.ui.JadeUI;
#else
import snownee.jade.api.ui.IElementHelper;
#endif





import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class IPLA_ComponentProvider implements IBlockComponentProvider {
    public static final IPLA_ComponentProvider INSTANCE = new IPLA_ComponentProvider();

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
                if (!stack.isEmpty()) tooltip.append(#if MC_VER >= V1_21_6 JadeUI.item #else #if MC_VER >= V1_21 IElementHelper.get() #else tooltip.getElementHelper() #endif .item #endif(stack, (entity.quad.get(i)) ? 0.5f : 0.8f));
            }
        }
    }

    @Override
    public #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif getUid() {
        return IPLA_Plugin.IPLA_JADE;
    }
}
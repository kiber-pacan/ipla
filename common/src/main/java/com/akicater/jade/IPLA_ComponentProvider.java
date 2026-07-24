package com.akicater.jade;


import com.akicater.blocks.LayingItemEntity;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.shapes.Shapes;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class IPLA_ComponentProvider implements IBlockComponentProvider {
    public static final IPLA_ComponentProvider INSTANCE = new IPLA_ComponentProvider();

    @Override
    public void appendTooltip(
            @NonNull ITooltip tooltip,
            BlockAccessor accessor,
            @NonNull IPluginConfig config
    ) {
        LayingItemEntity entity = (LayingItemEntity) accessor.getBlockEntity();
        if (entity == null) return;


        tooltip.add(Component.nullToEmpty(""));
        for (int i = 0; i < 6; i++) {
            for (int i1 = 0; i1 < 4; i1++) {
                ItemStack stack = entity.inv.get(i * 4 + i1);
                if (!stack.isEmpty()) tooltip.append(JadeUI.item(stack, (entity.quad.get(i)) ? 0.5f : 0.8f).offset(0,(entity.quad.get(i)) ? 4 : 0));
            }
        }
    }

    @Override
    public @NonNull Identifier getUid() {
        return IPLA_Plugin.TEST;
    }
}
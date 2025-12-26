package com.akicater.client.screen;

import com.akicater.IPLA;
#if MC_VER >= V1_20_1
import net.minecraft.client.gui.GuiGraphics;
#endif
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;

import net.minecraft.client.gui.screens.Screen;

#if MC_VER < V1_20_1
import com.mojang.blaze3d.vertex.PoseStack;
#endif


import static com.akicater.IPLA.config;

#if MC_VER >= V1_19_4
#else

#endif

#if MC_VER >= V1_19_2
import net.minecraft.network.chat.Component;
#else
import net.minecraft.network.chat.TextComponent;
#endif

public class IPLA_ConfigScreenScale extends Screen {
    private final Screen parent;

    private static final #if MC_VER >= V1_19_2 Component #else TextComponent #endif TITLE = #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("IPLA config");

    private float blockScale;
    private float itemScale;

    private static final float STEP = 0.125f;

    private float snap(float v) {
        return Math.round(v / STEP) * STEP;
    }


    public IPLA_ConfigScreenScale(Screen parent) {
        super(TITLE);

        this.parent = parent;

        this.blockScale = config.blockScale;
        this.itemScale = config.itemScale;
    }

    @Override
    public void render(#if MC_VER >= V1_20_1 GuiGraphics x1 #else PoseStack x1 #endif, int mouseX, int mouseY, float delta) {
        #if MC_VER <= V1_20_4
        this.renderDirtBackground(#if MC_VER >= V1_19_4 x1 #else 0 #endif);
        #endif
        super.render(x1, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();

        int widthButton = 200;
        int heightButton = 20;
        int x = (this.width - widthButton) / 2;
        int y = 30;
        int gap = 4;


        // Block scale
        this.addRenderableWidget(new AbstractSliderButton(
                x, y, widthButton, heightButton,
        #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Block scale"),
                normalize(blockScale)
        ) {
            @Override
            protected void updateMessage() {
                setMessage(#if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif
                        (String.format("Block scale: %.3f", blockScale)));
            }

            @Override
            protected void applyValue() {
                blockScale = snap(denormalize((float) value));
                config.blockScale = blockScale;
            }
        });
        y += heightButton + gap;


        // Item scale
        this.addRenderableWidget(new AbstractSliderButton(
                x, y, widthButton, heightButton,
        #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Item scale"),
                normalize(itemScale)
        ) {
            @Override
            protected void updateMessage() {
                setMessage(#if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif
                        (String.format("Item scale: %.3f", itemScale)));
            }

            @Override
            protected void applyValue() {
                itemScale = snap(denormalize((float) value));
                config.itemScale = itemScale;
            }
        });
        y += heightButton + gap * 4;

        // Reset
        #if MC_VER >= V1_19_4
        this.addRenderableWidget(
                Button.builder(
                                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Reset"),
                                b -> reload()
                        )
                        .bounds(x, y, widthButton, heightButton)
                        .build()
        );
        y += heightButton + gap;

        // Done button
        this.addRenderableWidget(
                Button.builder(
                                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Done"),
                                b -> onClose()
                        )
                        .bounds(x, y, widthButton, heightButton)
                        .build()
        );
        #else
        this.addRenderableWidget(new Button(x, y, widthButton, heightButton, #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Reset"), b -> {
            reload();
        }));
        y += heightButton + gap;

        // Done button
        this.addRenderableWidget(new Button(x, y, widthButton, heightButton, #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Done"), b -> onClose()));
        #endif
    }

    private float normalize(float v) {
        return (v - 0.05f) / (2.0f - 0.05f);
    }
    private float denormalize(float v) {
        return 0.05f + v * (2.0f - 0.05f);
    }

    @Override
    public void onClose() {
        IPLA.config.saveConfig();
        this.minecraft.setScreen(parent);
    }

    public void reload() {
        clearWidgets();
        config.defaultScaleConfig();
        config.saveConfig();

        this.blockScale = config.blockScale;
        this.itemScale = config.itemScale;

        init();
    }
}

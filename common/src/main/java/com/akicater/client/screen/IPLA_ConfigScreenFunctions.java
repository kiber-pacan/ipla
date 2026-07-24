package com.akicater.client.screen;

import com.akicater.IPLA;
#if MC_VER >= V1_20_1
import com.akicater.IPLA_Client;
import net.minecraft.client.Minecraft;
#if MC_VER < V26_1
import net.minecraft.client.gui.GuiGraphics;
#endif
#endif

import com.akicater.IPLA_Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;

import net.minecraft.client.gui.screens.Screen;

#if MC_VER < V1_20_1
import com.mojang.blaze3d.vertex.PoseStack;
#endif



import static com.akicater.IPLA_Client.config;

#if MC_VER >= V1_19_4
#else

#endif

#if MC_VER >= V1_19_2
import net.minecraft.network.chat.Component;
#else
import net.minecraft.network.chat.TextComponent;
#endif

public class IPLA_ConfigScreenFunctions extends Screen {
    private final Screen parent;

    private static final #if MC_VER >= V1_19_2 Component #else TextComponent #endif TITLE = #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("IPLA config");


    private boolean oldRendering;
    private boolean eatFood;

    public IPLA_ConfigScreenFunctions(Screen parent) {
        super(TITLE);

        this.parent = parent;

        this.oldRendering = config.oldRendering;
        this.eatFood = config.eatFood;
    }

    #if MC_VER < V26_1
    @Override
    public void render(#if MC_VER >= V1_20_1 GuiGraphics x1 #else PoseStack x1 #endif, int mouseX, int mouseY, float delta) {
        #if MC_VER <= V1_20_4
        this.renderDirtBackground(#if MC_VER >= V1_19_4 x1 #else 0 #endif);
        #endif
        super.render(x1, mouseX, mouseY, delta);
    }
    #endif

    @Override
    protected void init() {
        super.init();

        int widthButton = 200;
        int heightButton = 20;
        int x = (this.width - widthButton) / 2;
        int y = 30;
        int gap = 4;


        // Old rendering checkbox
        #if MC_VER >= V1_20_4
        this.addRenderableWidget(Checkbox.builder(Component.literal("Old rendering"), this.font)
                #if MC_VER >= V1_21 .maxWidth(width) #endif
                .selected(oldRendering)
                .onValueChange((cb, state) -> {
                    oldRendering = state;
                    config.saveConfig();
                })
                .pos(x, y)
                .build());
        #else
        this.addRenderableWidget(new Checkbox(x, y, widthButton, heightButton,
                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Old rendering"), oldRendering) {
            @Override
            public void onPress() {
                super.onPress();
                oldRendering = this.selected();
                config.oldRendering = oldRendering;
            }
        });
        #endif
        y += heightButton + gap;

        // Eat food checkbox
        #if MC_VER >= V1_20_4
        this.addRenderableWidget(Checkbox.builder(Component.literal("Old rendering"), this.font)
                #if MC_VER >= V1_21 .maxWidth(width) #endif
                .selected(eatFood)
                .onValueChange((cb, state) -> {
                    eatFood = state;
                    config.saveConfig();
                })
                .pos(x, y)
                .build());
        #else
        this.addRenderableWidget(new Checkbox(x, y, widthButton, heightButton,
                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Eat food"), eatFood) {
            @Override
            public void onPress() {
                super.onPress();
                eatFood = this.selected();
                config.eatFood = eatFood;
            }
        });
        #endif
        y += (heightButton + gap) * 4;

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
        IPLA_Client.config.saveConfig();
        this.minecraft #if MC_VER >= V26_2 .gui #endif .setScreen(parent);
    }

    public void reload() {
        clearWidgets();
        config.defaultFunctionsConfig();
        config.saveConfig();

        this.oldRendering = config.oldRendering;
        this.eatFood = config.eatFood;

        init();
    }
}

package com.akicater.client.screen;

import com.akicater.IPLA;
#if MC_VER >= V1_20_1
import com.akicater.IPLA_Client;
import net.minecraft.client.Minecraft;
#if MC_VER < V26_1
import net.minecraft.client.gui.GuiGraphics;
#else
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

public class IPLA_ConfigScreenBase extends Screen {
    private final Screen parent;

    private static final #if MC_VER >= V1_19_2 Component #else TextComponent #endif TITLE = #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("IPLA config");

    private float absSize;
    private float iSize;
    private float bSize;
    private int rotationPower;

    public IPLA_ConfigScreenBase(Screen parent) {
        super(TITLE);
        this.parent = parent;

        this.absSize = config.absoluteSize;
        this.iSize = config.itemSize;
        this.bSize = config.blockSize;
        this.rotationPower = config.rotationPower;
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
        int widthButton = 200;
        int heightButton = 20;
        int x = (this.width - widthButton) / 2;
        int y = 30;
        int gap = 4;

        // Absolute size
        this.addRenderableWidget(new AbstractSliderButton(x, y, widthButton, heightButton,
                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Absolute size"), normalize(absSize)) {
            @Override
            protected void updateMessage() {
                setMessage(#if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif (String.format("Absolute size: %.2f", absSize)));
            }

            @Override
            protected void applyValue() {
                absSize = denormalize((float) value);
                config.absoluteSize = absSize;
            }
        });
        y += heightButton + gap;

        // Item size
        this.addRenderableWidget(new AbstractSliderButton(x, y, widthButton, heightButton,
                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Item size"), normalize(iSize)) {
            @Override
            protected void updateMessage() {
                setMessage(#if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif (String.format("Item size: %.2f", iSize)));
            }

            @Override
            protected void applyValue() {
                iSize = denormalize((float) value);
                config.itemSize = iSize;
            }
        });
        y += heightButton + gap;

        // Block size
        this.addRenderableWidget(new AbstractSliderButton(x, y, widthButton, heightButton,
                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Block size"), normalize(bSize)) {
            @Override
            protected void updateMessage() {
                setMessage(#if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif (String.format("Block size: %.2f", bSize)));
            }

            @Override
            protected void applyValue() {
                bSize = denormalize((float) value);
                config.blockSize = bSize;
            }
        });
        y += heightButton + gap;

        // Rotation power 0..4
        int max = 4;
        this.addRenderableWidget(new AbstractSliderButton(x, y, widthButton, heightButton,
                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Rotation power"), rotationPower / (double) max) {
            @Override
            protected void updateMessage() {
                int display = (int) Math.round(value * max);
                setMessage(#if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Rotation power size: " + display));
            }

            @Override
            protected void applyValue() {
                rotationPower = (int) Math.round(value * max);
                config.rotationPower = rotationPower;
            }
        });
        y += heightButton + gap;

        // Scale screen
        #if MC_VER >= V1_19_4
        this.addRenderableWidget(
                Button.builder(
                                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Scale settings"),

                                b -> Minecraft.getInstance() #if MC_VER >= V26_2 .gui #endif .setScreen(new IPLA_ConfigScreenScale(this))
                        )
                        .bounds(x, y, widthButton, heightButton)
                        .build()
        );
        #else
        this.addRenderableWidget(new Button(x, y, widthButton, heightButton, #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Scale settings"),
            b -> {
                Minecraft.getInstance().setScreen(new IPLA_ConfigScreenScale(this));
            }
        ));
        #endif

        y += heightButton + gap;

        // Scale screen
        #if MC_VER >= V1_19_4
        this.addRenderableWidget(
                Button.builder(
                                #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Functions settings"),

                                b -> Minecraft.getInstance() #if MC_VER >= V26_2 .gui #endif .setScreen(new IPLA_ConfigScreenFunctions(this))
                        )
                        .bounds(x, y, widthButton, heightButton)
                        .build()
        );
        #else
        this.addRenderableWidget(new Button(x, y, widthButton, heightButton, #if MC_VER >= V1_19_2 Component.literal #else new TextComponent #endif ("Functions settings"),
            b -> {
                Minecraft.getInstance().setScreen(new IPLA_ConfigScreenFunctions(this));
            }
        ));
        #endif

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
        config.defaultBaseConfig();
        config.defaultScaleConfig();
        config.defaultFunctionsConfig();
        config.saveConfig();

        this.absSize = config.absoluteSize;
        this.iSize = config.itemSize;
        this.bSize = config.blockSize;
        this.rotationPower = config.rotationPower;

        init();
    }
}

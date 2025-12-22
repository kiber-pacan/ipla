package com.akicater.client;

import com.akicater.IPLA;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class IPLA_ConfigScreen extends Screen {
    private final Screen parent;

    private static final Component TITLE = Component.literal("IPLA config");

    private float absSize;
    private float iSize;
    private float bSize;
    private boolean oldRendering;
    private int rotationPower;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 61, 33);

    public IPLA_ConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;


        this.absSize = IPLA.config.absoluteSize;
        this.iSize = IPLA.config.itemSize;
        this.bSize = IPLA.config.blockSize;
        this.oldRendering = IPLA.config.oldRendering;
        this.rotationPower = IPLA.config.rotationPower;
    }

    @Override
    protected void init() {
        LinearLayout header = this.layout.addToHeader(
                LinearLayout.vertical().spacing(8)
        );

        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting()
                .paddingHorizontal(4)
                .paddingBottom(4)
                .alignHorizontallyCenter();

        GridLayout.RowHelper row = gridLayout.createRowHelper(1);

        int width = 200;
        int height = 20;

        // Absolute size
        row.addChild(new AbstractSliderButton(0, 0, width, height,
                Component.literal("Absolute size"), normalize(absSize)) {

            @Override
            protected void updateMessage() {
                setMessage(Component.literal(
                        String.format("Absolute size: %.2f", absSize)
                ));
            }

            @Override
            protected void applyValue() {
                absSize = denormalize((float) value);
                IPLA.config.absoluteSize = absSize;
            }
        });

        // Item size
        row.addChild(new AbstractSliderButton(0, 0, width, height,
                Component.literal("Item size"), normalize(iSize)) {

            @Override
            protected void updateMessage() {
                setMessage(Component.literal(
                        String.format("Item size: %.2f", iSize)
                ));
            }

            @Override
            protected void applyValue() {
                iSize = denormalize((float) value);
                IPLA.config.itemSize = iSize;
            }
        });

        // Block size
        row.addChild(new AbstractSliderButton(0, 0, width, height,
                Component.literal("Block size"), normalize(bSize)) {

            @Override
            protected void updateMessage() {
                setMessage(Component.literal(
                        String.format("Block size: %.2f", bSize)
                ));
            }

            @Override
            protected void applyValue() {
                bSize = denormalize((float) value);
                IPLA.config.blockSize = bSize;
            }
        });

        // Degrees
        int max = 4;
        row.addChild(new AbstractSliderButton(0, 0, width, height,
                Component.literal("Rotation power"), rotationPower / (double) max) {

            @Override
            protected void updateMessage() {
                int display = (int) Math.round(value * max); // округляем к int
                setMessage(Component.literal(
                        "Rotation power size: " + display
                ));
            }

            @Override
            protected void applyValue() {
                rotationPower = (int) Math.round(value * max); // сохраняем int
                IPLA.config.rotationPower = rotationPower;   // в конфиг
            }
        });

        // Old rendering checkbox
        row.addChild(
                Checkbox.builder(Component.literal("Old rendering"), this.font)
                        .selected(oldRendering)
                        .onValueChange((cb, value) -> {
                            oldRendering = value;
                            IPLA.config.oldRendering = value;
                        })
                        .build()
        );

        this.layout.addToContents(gridLayout);
        this.layout.addToFooter(
                Button.builder(Component.literal("Done"), b -> this.onClose())
                        .width(200)
                        .build()
        );

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    public void onClose() {
        IPLA.config.saveConfig();
        this.minecraft.setScreen(parent);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    // диапазон 0.05..2.0 -> 0..1
    private float normalize(float v) {
        return (v - 0.05f) / (2.0f - 0.05f);
    }

    private float denormalize(float v) {
        return 0.05f + v * (2.0f - 0.05f);
    }
}

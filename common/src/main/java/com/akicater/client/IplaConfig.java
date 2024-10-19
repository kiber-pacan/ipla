package com.akicater.client;


import com.akicater.Ipla;
#if MC_VER >= V1_20_4
import com.google.gson.GsonBuilder;
import com.google.gson.internal.GsonBuildConfig;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.config.v2.impl.serializer.GsonConfigSerializer;
import dev.isxander.yacl3.impl.controller.FloatSliderControllerBuilderImpl;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dev. isxander. yacl3.config. v2.api. ConfigClassHandler;
#elif MC_VER > V1_18_2
import eu.midnightdust.lib.config.MidnightConfig;
#else
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
#endif

#if MC_VER <= V1_18_2 @Config(name = Ipla.MOD_ID) #endif
public class IplaConfig #if  MC_VER > V1_18_2 && MC_VER < V1_20_4 extends MidnightConfig #elif MC_VER <= V1_18_2 implements ConfigData #endif{
    #if MC_VER >= V1_20_4
    public static ConfigClassHandler<IplaConfig> HANDLER = ConfigClassHandler.createBuilder(IplaConfig.class)
            .id(#if MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(Ipla.MOD_ID, "ipla_config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("ipla_config.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public boolean oldRendering;
    @SerialEntry
    public float absSize;
    @SerialEntry
    public float iSize;
    @SerialEntry
    public float bSize;

    public Screen getScreen(Screen parentScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Ipla config."))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Ipla config"))
                        .tooltip(Component.literal("Main Ipla tab."))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Main settings"))
                                .description(OptionDescription.of(Component.literal("Main Ipla settings.")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Old rendering"))
                                        .description(OptionDescription.of(Component.literal("Old block rendering style (half of block in block face).")))
                                        .binding(false, () -> this.oldRendering, newVal -> this.oldRendering = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Component.literal("Absolute size"))
                                        .description(OptionDescription.of(Component.literal("Number that item size and block size will be multiplied by \n (like this: absSize * bSize = 1.0f * 0.75f = 0.75f).")))
                                        .binding(1.0f, () -> this.absSize, newVal -> this.absSize = newVal)
                                        .controller((option) -> new FloatSliderControllerBuilderImpl(option).range(0.05f, 2.0f).step(0.05f))
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Component.literal("Item size"))
                                        .description(OptionDescription.of(Component.literal("Basically item rendering size.")))
                                        .binding(1.0f, () -> this.iSize, newVal -> this.iSize = newVal)
                                        .controller((option) -> new  FloatSliderControllerBuilderImpl(option).range(0.05f, 2.0f).step(0.05f))
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Component.literal("Block size"))
                                        .description(OptionDescription.of(Component.literal("Basically block rendering size.")))
                                        .binding(0.75f, () -> this.bSize, newVal -> this.bSize = newVal)
                                        .controller((option) -> new  FloatSliderControllerBuilderImpl(option).range(0.05f, 2.0f).step(0.05f))
                                        .build())
                                .build())
                        .build())
                .build().generateScreen(parentScreen);
    }
    #elif MC_VER > V1_18_2
    #if MC_VER > V1_19_2 public static final String MAIN_CATEGORY = "text"; #endif

    @MidnightConfig.Entry(#if MC_VER > V1_19_2 category = MAIN_CATEGORY #endif)
    public static boolean oldRendering = false;

    @MidnightConfig.Entry(#if MC_VER > V1_19_2 category = MAIN_CATEGORY, #endif isSlider = true, precision = 1000, min = 0.05f, max = 2.0f)
    public static float absSize = 1f;

    @MidnightConfig.Entry(#if MC_VER > V1_19_2 category = MAIN_CATEGORY, #endif isSlider = true, precision = 1000, min = 0.05f, max = 2.0f)
    public static float iSize = 1f;

    @MidnightConfig.Entry(#if MC_VER > V1_19_2 category = MAIN_CATEGORY, #endif isSlider = true, precision = 1000, min = 0.05f, max = 2.0f)
    public static float bSize = 0.75f;
    #else
    public boolean oldRendering = false;
    public float absSize = 1f;
    public float iSize = 1f;
    public float bSize = 0.75f;
    #endif
}

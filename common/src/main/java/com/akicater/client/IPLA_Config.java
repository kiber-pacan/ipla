package com.akicater.client;

import com.akicater.IPLA;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.architectury.platform.Platform;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

#if MC_VER >= V1_20_1
#endif



#if MC_VER >= V1_19_4
#else

#endif

#if MC_VER >= V1_19_2
#else
import net.minecraft.network.chat.TextComponent;
#endif

public class IPLA_Config {


    // Rotation
    public int rotationPower;
    protected float baseDegrees = 90.0f;

    // Size
    public float absoluteSize;
    public float itemSize;
    public float blockSize;

    // Resize
    public float itemScale;
    public float blockScale;

    // Functions
    public boolean eatFood;
    public boolean oldRendering;

    public IPLA_Config(){}

    public void loadConfig() throws IOException {
        Path cp = Platform.getConfigFolder();
        Path c = cp.resolve("ipla_config.toml");

        if (Files.exists(c)) {
            CommentedFileConfig fileConfig = CommentedFileConfig.builder(c)
                    .concurrent()
                    .build();

            fileConfig.load();


            // Rotation
            Optional<Integer> rotPower = fileConfig.getOptional("rotPower");

            // Size
            Optional<Float> absSize = fileConfig.getOptional("absSize");
            Optional<Float> iSize = fileConfig.getOptional("iSize");
            Optional<Float> bSize = fileConfig.getOptional("bSize");

            // Resize
            Optional<Float> itemScale = fileConfig.getOptional("itemScale");
            Optional<Float> blockScale = fileConfig.getOptional("blockScale");

            // Functions
            Optional<Boolean> oldRendering = fileConfig.getOptional("oldRendering");
            Optional<Boolean> eatFood = fileConfig.getOptional("eatFood");

            boolean broken = false;

            try {
                this.absoluteSize = absSize.orElse(1.0f);
            } catch (Exception e) {
                this.absoluteSize = 1.0f;
                broken = true;
            }

            try {
                this.itemSize = iSize.orElse(1.0f);
            } catch (Exception e) {
                this.itemSize = 1.0f;
                broken = true;
            }

            try {
                this.blockSize = bSize.orElse(1.0f);
            } catch (Exception e) {
                this.blockSize = 1.0f;
                broken = true;
            }

            try {
                this.rotationPower = rotPower.orElse(1);
            } catch (Exception e) {
                this.baseDegrees = 0;
                broken = true;
            }

            try {
                this.blockScale = blockScale.orElse(2.0f);
            } catch (Exception e) {
                this.blockScale = 2.0f;
                broken = true;
            }

            try {
                this.itemScale = itemScale.orElse(2.0f);
            } catch (Exception e) {
                this.itemScale = 2.0f;
                broken = true;
            }

            try {
                this.eatFood = eatFood.orElse(false);
            } catch (Exception e) {
                this.eatFood = false;
                broken = true;
            }

            if (broken) {
                defaultBaseConfig();
                saveConfig();
            }

        } else {
            defaultBaseConfig();
            saveConfig();
        }
    }

    public void defaultBaseConfig() {

        this.rotationPower = 1;

        this.absoluteSize = 1.0f;
        this.itemSize = 1.0f;
        this.blockSize = 1.0f;
    }

    public void defaultScaleConfig() {
        this.blockScale = 2.0f;
        this.itemScale = 2.0f;
    }

    public void defaultFunctionsConfig() {
        this.oldRendering = false;
        this.eatFood = true;
    }

    public void saveConfig() {
        Path folder = Platform.getConfigFolder();
        Path c = folder.resolve("ipla_config.toml");

        try {
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(c)) {
                writer.write("# IPLA config\n");
                writer.write("absSize = " + absoluteSize + "\n");
                writer.write("iSize = " + itemSize + "\n");
                writer.write("bSize = " + blockSize + "\n");

                writer.write("rotPower = " + rotationPower + "\n");
                writer.write("blockScale = " + blockScale + "\n");
                writer.write("itemScale = " + itemScale + "\n");

                writer.write("oldRendering = " + oldRendering + "\n");
                writer.write("eatFood = " + eatFood + "\n");
            }
        } catch (IOException e) {
            IPLA.LOGGER.info("IPLA: failed to save config to " + c.toAbsolutePath());
        }
    }

    public float getRotationDegrees() {
        float degrees = this.baseDegrees;

        for (int i = 0; i < rotationPower; i++) {
            degrees /= 2;
        }

        return degrees;
    }
}
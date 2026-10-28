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
            System.out.println("EXIST");
            CommentedFileConfig fileConfig = CommentedFileConfig.builder(c)
                    .concurrent()
                    .build();

            fileConfig.load();


            // Rotation
            Optional<Integer> rotPower = fileConfig.getOptional("rotPower");

            // Size
            Optional<Double> absSize = fileConfig.getOptional("absSize");
            Optional<Double> iSize = fileConfig.getOptional("iSize");
            Optional<Double> bSize = fileConfig.getOptional("bSize");

            // Resize
            Optional<Double> itemScale = fileConfig.getOptional("itemScale");
            Optional<Double> blockScale = fileConfig.getOptional("blockScale");

            // Functions
            Optional<Boolean> oldRendering = fileConfig.getOptional("oldRendering");
            Optional<Boolean> eatFood = fileConfig.getOptional("eatFood");

            boolean broken = false;

            if (absSize.isPresent()) {
                this.absoluteSize = absSize.get().floatValue();
            } else {
                this.absoluteSize = 1.0f;
                broken = true;
                System.out.println("BROKEM absSize");
            }

            if (iSize.isPresent()) {
                this.itemSize = iSize.get().floatValue();
            } else {
                this.itemSize = 1.0f;
                broken = true;
                System.out.println("BROKEM iSize");
            }

            if (bSize.isPresent()) {
                this.blockSize = bSize.get().floatValue();
            } else {
                this.blockSize = 1.0f;
                broken = true;
                System.out.println("BROKEM bSize");
            }

            if (rotPower.isPresent()) {
                this.rotationPower = rotPower.get();
            } else {
                this.baseDegrees = 0;
                broken = true;
                System.out.println("BROKEM rotPower");
            }

            if (blockScale.isPresent()) {
                this.blockScale = blockScale.get().floatValue();
            } else {
                this.blockScale = 2.0f;
                broken = true;
                System.out.println("BROKEM blockScale");
            }

            if (itemScale.isPresent()) {
                this.itemScale = itemScale.get().floatValue();
            } else {
                this.itemScale = 2.0f;
                broken = true;
                System.out.println("BROKEM itemScale");
            }

            if (eatFood.isPresent()) {
                this.eatFood = eatFood.get();
            } else {
                this.eatFood = false;
                broken = true;
                System.out.println("BROKEM eatFood");
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
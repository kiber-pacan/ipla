package com.akicater.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.architectury.platform.Platform;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;


public class IPLA_Config {
    public boolean oldRendering;
    public float absoluteSize;
    public float itemSize;
    public float blockSize;
    public int rotationPower;

    protected float baseDegrees = 90.0f;

    public IPLA_Config(){}

    public void loadConfig() throws IOException {
        Path cp = Platform.getConfigFolder();
        Path c = cp.resolve("ipla_config.toml");

        if (Files.exists(c)) {
            CommentedFileConfig fileConfig = CommentedFileConfig.builder(c)
                    .concurrent()
                    .build();

            fileConfig.load();

            Optional<Boolean> oldRendering = fileConfig.getOptional("oldRendering");
            Optional<Float> absSize = fileConfig.getOptional("absSize");
            Optional<Float> iSize = fileConfig.getOptional("iSize");
            Optional<Float> bSize = fileConfig.getOptional("bSize");
            Optional<Integer> rotPower = fileConfig.getOptional("rotPower");

            boolean broken = false;

            try {
                this.oldRendering = oldRendering.orElse(false);
            } catch (Exception e) {
                this.oldRendering = false;
                broken = true;
            }

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
                this.blockSize = bSize.orElse(0.75f);
            } catch (Exception e) {
                this.blockSize = 0.75f;
                broken = true;
            }

            try {
                this.rotationPower = rotPower.orElse(1);
            } catch (Exception e) {
                this.baseDegrees = 0;
                broken = true;
            }

            if (broken) saveConfig();

        } else {
            // дефолты
            this.oldRendering = false;
            this.absoluteSize = 1.0f;
            this.itemSize = 1.0f;
            this.blockSize = 0.75f;
            this.rotationPower = 1;

            saveConfig();
        }
    }

    public void saveConfig() {
        Path c = Platform.getConfigFolder().resolve("ipla_config.toml");

        try (BufferedWriter writer = Files.newBufferedWriter(c)) {
            writer.write("# IPLA config\n");
            writer.write("oldRendering = " + oldRendering + "\n");
            writer.write("absSize = " + absoluteSize + "\n");
            writer.write("iSize = " + itemSize + "\n");
            writer.write("bSize = " + blockSize + "\n");
            writer.write("rotPower = " + rotationPower + "\n");
        } catch (IOException e) {
            e.printStackTrace();
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
package com.akicater.mixin;

import com.akicater.IPLA_Client;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;


@Mixin(Minecraft.class)
public class ConfigMixin {
    @Inject(method = "run", at = @At("HEAD"))
    private void run(CallbackInfo ci) {
        System.out.println("LOADED CONIFG");
        try {
            IPLA_Client.config.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void stop(CallbackInfo ci) {
        System.out.println("SAVES CONIFG");
        IPLA_Client.config.saveConfig();
    }
}

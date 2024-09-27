package com.akicater.neoforge;

import net.neoforged.fml.common.Mod;

import com.akicater.Ipla;

@Mod(Ipla.MOD_ID)
public final class IplaNeoForge {
    public IplaNeoForge() {
        Ipla.initiliazeServer();
    }
}

package com.akicater.fabric;


import com.akicater.Ipla;
import net.fabricmc.api.ModInitializer;

public final class IplaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Ipla.initializeServer();
    }
}

package com.akicater.abs;

#if MC_VER >= V1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif

public class Resource {
    public static #if MC_VER >= V1_21_11 Identifier #else ResourceLocation #endif fromNamespaceAndPath(String namespace, String path) {
        return #if MC_VER >= V1_21_11 Identifier.fromNamespaceAndPath #elif MC_VER >= V1_21 ResourceLocation.fromNamespaceAndPath #else new ResourceLocation #endif(namespace, path);
    }
}

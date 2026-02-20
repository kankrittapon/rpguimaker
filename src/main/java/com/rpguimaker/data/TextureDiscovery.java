package com.rpguimaker.data;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility to discover available textures in the game resources.
 */
public class TextureDiscovery {

    public static List<ResourceLocation> findAllTextures() {
        List<ResourceLocation> textures = new ArrayList<>();
        // Add external textures already loaded
        textures.addAll(DynamicTextureRegistry.getExternalTextures());

        ResourceManager rm = Minecraft.getInstance().getResourceManager();

        // Scan for .png files in the textures directory
        Map<ResourceLocation, net.minecraft.server.packs.resources.Resource> resources = rm.listResources("textures",
                (rl) -> rl.getPath().endsWith(".png"));

        for (ResourceLocation rl : resources.keySet()) {
            // Filter some unwanted internal ones if needed
            if (!rl.getPath().contains("debug") && !rl.getPath().contains("missing")) {
                textures.add(rl);
            }
        }

        return textures;
    }
}

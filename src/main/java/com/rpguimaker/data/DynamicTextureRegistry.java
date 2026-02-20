package com.rpguimaker.data;

import com.mojang.blaze3d.platform.NativeImage;
import com.rpguimaker.RPGUIManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for textures loaded dynamically from the game directory.
 */
public class DynamicTextureRegistry {
    private static final Map<ResourceLocation, DynamicTexture> EXTERNAL_TEXTURES = new HashMap<>();
    private static final String NAMESPACE = "rpgui_imported";

    public static void scanAndLoad() {
        File dir = new File(Minecraft.getInstance().gameDirectory, "rpguimaker/textures");
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null)
            return;

        for (File file : files) {
            String id = file.getName().toLowerCase().replace(".png", "").replaceAll("[^a-z0-9_.-]", "_");
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(NAMESPACE, id);

            if (!EXTERNAL_TEXTURES.containsKey(rl)) {
                loadTexture(file, rl);
            }
        }
    }

    private static void loadTexture(File file, ResourceLocation rl) {
        try (FileInputStream fis = new FileInputStream(file)) {
            NativeImage image = NativeImage.read(fis);
            DynamicTexture texture = new DynamicTexture(image);
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().getTextureManager().register(rl, texture);
                EXTERNAL_TEXTURES.put(rl, texture);
                RPGUIManager.LOGGER.info("Registered external texture: {}", rl);
            });
        } catch (IOException e) {
            RPGUIManager.LOGGER.error("Failed to load external texture: {}", file.getName(), e);
        }
    }

    public static List<ResourceLocation> getExternalTextures() {
        return new ArrayList<>(EXTERNAL_TEXTURES.keySet());
    }

    public static void clear() {
        Minecraft.getInstance().execute(() -> {
            for (ResourceLocation rl : EXTERNAL_TEXTURES.keySet()) {
                Minecraft.getInstance().getTextureManager().release(rl);
            }
            EXTERNAL_TEXTURES.clear();
        });
    }
}

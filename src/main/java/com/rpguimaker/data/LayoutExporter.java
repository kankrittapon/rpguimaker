package com.rpguimaker.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpguimaker.RPGUIManager;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility to export UI designs to JSON files.
 */
public class LayoutExporter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveToFile(UILayout layout) {
        File dir = new File(Minecraft.getInstance().gameDirectory, "rpguimaker/exports");
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, layout.guiName + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(layout, writer);
            RPGUIManager.LOGGER.info("Successfully exported UI layout to: {}", file.getAbsolutePath());
        } catch (IOException e) {
            RPGUIManager.LOGGER.error("Failed to export UI layout", e);
        }
    }
}

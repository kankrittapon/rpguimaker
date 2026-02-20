package com.rpguimaker.data;

import com.google.gson.Gson;
import com.rpguimaker.RPGUIManager;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to load UI designs from JSON files.
 */
public class LayoutLoader {
    private static final Gson GSON = new Gson();

    public static UILayout loadFromFile(String guiName) {
        File file = new File(Minecraft.getInstance().gameDirectory, "rpguimaker/exports/" + guiName + ".json");
        if (!file.exists())
            return null;

        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, UILayout.class);
        } catch (IOException e) {
            RPGUIManager.LOGGER.error("Failed to load UI layout", e);
            return null;
        }
    }

    public static List<String> listLayouts() {
        File dir = new File(Minecraft.getInstance().gameDirectory, "rpguimaker/exports");
        List<String> layouts = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    layouts.add(f.getName().replace(".json", ""));
                }
            }
        }
        return layouts;
    }
}

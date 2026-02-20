package com.rpguimaker.engine;

import com.rpguimaker.data.UILayout;
import com.rpguimaker.data.LayoutLoader;
import com.rpguimaker.client.screen.DynamicRuntimeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * The Runtime Engine that other mods can use to display RPG UI Maker layouts.
 */
public class UIRuntime {

    /**
     * Opens a UI based on the exported JSON filename.
     * 
     * @param layoutName The name of the layout (without .json)
     */
    public static void openUI(String layoutName) {
        UILayout layout = LayoutLoader.loadFromFile(layoutName);
        if (layout != null) {
            Minecraft.getInstance().setScreen(new DynamicRuntimeScreen(layout));
        }
    }

    /**
     * Logic to handle element clicks and linking.
     */
    public static void handleElementClick(UILayout.UIElementData element) {
        if (element.linkTo != null && !element.linkTo.isEmpty()) {
            openUI(element.linkTo);
        }
    }
}

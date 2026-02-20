package com.rpguimaker.engine.rendering;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Utility for Nine-Slice (9-Patch) rendering to prevent texture stretching at
 * corners.
 */
public class NineSliceRenderer {

    /**
     * Draws a nine-sliced texture.
     * 
     * @param gui       The GuiGraphics instance.
     * @param texture   The texture ResourceLocation.
     * @param x         Screen X.
     * @param y         Screen Y.
     * @param width     Total width of the element.
     * @param height    Total height of the element.
     * @param sliceSize Size of the corners/edges (in pixels on the texture).
     * @param texW      Total width of the texture file.
     * @param texH      Total height of the texture file.
     */
    public static void draw(GuiGraphics gui, ResourceLocation texture, int x, int y, int width, int height,
            int sliceSize, int texW, int texH) {
        // Corners
        gui.blit(texture, x, y, 0, 0, sliceSize, sliceSize, texW, texH); // Top-Left
        gui.blit(texture, x + width - sliceSize, y, texW - sliceSize, 0, sliceSize, sliceSize, texW, texH); // Top-Right
        gui.blit(texture, x, y + height - sliceSize, 0, texH - sliceSize, sliceSize, sliceSize, texW, texH); // Bottom-Left
        gui.blit(texture, x + width - sliceSize, y + height - sliceSize, texW - sliceSize, texH - sliceSize, sliceSize,
                sliceSize, texW, texH); // Bottom-Right

        // Edges
        gui.blit(texture, x + sliceSize, y, sliceSize, 0, width - 2 * sliceSize, sliceSize, texW, texH); // Top-Edge
        gui.blit(texture, x + sliceSize, y + height - sliceSize, sliceSize, texH - sliceSize, width - 2 * sliceSize,
                sliceSize, texW, texH); // Bottom-Edge
        gui.blit(texture, x, y + sliceSize, 0, sliceSize, sliceSize, height - 2 * sliceSize, texW, texH); // Left-Edge
        gui.blit(texture, x + width - sliceSize, y + sliceSize, texW - sliceSize, sliceSize, sliceSize,
                height - 2 * sliceSize, texW, texH); // Right-Edge

        // Center
        gui.blit(texture, x + sliceSize, y + sliceSize, sliceSize, sliceSize, width - 2 * sliceSize,
                height - 2 * sliceSize, texW, texH);
    }
}

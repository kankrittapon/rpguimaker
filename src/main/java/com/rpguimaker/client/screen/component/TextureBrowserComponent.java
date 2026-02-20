package com.rpguimaker.client.screen.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/**
 * A UI component for browsing and selecting textures.
 */
public class TextureBrowserComponent {
    private final List<ResourceLocation> textures;
    private int scrollOffset = 0;
    private final int x, y, width, height;
    private ResourceLocation selectedTexture = null;

    public TextureBrowserComponent(List<ResourceLocation> textures, int x, int y, int width, int height) {
        this.textures = textures;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        gui.fill(x, y, x + width, y + height, 0xEE0F172A);
        gui.renderOutline(x, y, width, height, 0xFF00E5FF);

        int itemHeight = 20;
        int maxItems = height / itemHeight;

        for (int i = 0; i < maxItems && (i + scrollOffset) < textures.size(); i++) {
            ResourceLocation rl = textures.get(i + scrollOffset);
            int itemY = y + (i * itemHeight);
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight;

            if (hovered || rl.equals(selectedTexture)) {
                gui.fill(x + 2, itemY + 1, x + width - 2, itemY + itemHeight - 1, 0x4400E5FF);
            }

            String label = rl.getNamespace() + ":" + rl.getPath().substring(rl.getPath().lastIndexOf('/') + 1);
            gui.drawString(font, label, x + 5, itemY + 6, 0xFFFFFFFF, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            int itemHeight = 20;
            int index = (int) ((mouseY - y) / itemHeight) + scrollOffset;
            if (index >= 0 && index < textures.size()) {
                selectedTexture = textures.get(index);
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            scrollOffset = (int) Math.max(0, Math.min(textures.size() - 5, scrollOffset - scrollY));
            return true;
        }
        return false;
    }

    public ResourceLocation getSelectedTexture() {
        return selectedTexture;
    }
}

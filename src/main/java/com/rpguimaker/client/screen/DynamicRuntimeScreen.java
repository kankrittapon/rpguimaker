package com.rpguimaker.client.screen;

import com.rpguimaker.data.UILayout;
import com.rpguimaker.engine.UIRuntime;
import com.rpguimaker.engine.rendering.NineSliceRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A generic screen that renders a UILayout JSON at runtime.
 */
public class DynamicRuntimeScreen extends Screen {
    private final UILayout layout;
    private static final ResourceLocation DEFAULT_UI = ResourceLocation.fromNamespaceAndPath("rpguimaker",
            "textures/gui/default_panel.png");

    public DynamicRuntimeScreen(UILayout layout) {
        super(Component.literal(layout.guiName != null ? layout.guiName : "RPG UI"));
        this.layout = layout;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui, mouseX, mouseY, partialTick);
        for (UILayout.UIElementData el : layout.elements) {
            renderElement(gui, el);
        }
        super.render(gui, mouseX, mouseY, partialTick);
    }

    private void renderElement(GuiGraphics gui, UILayout.UIElementData el) {
        ResourceLocation rl = DEFAULT_UI;
        if (el.texture != null && !el.texture.isEmpty()) {
            rl = ResourceLocation.parse(el.texture);
        }

        if (el.type.equals("panel")) {
            NineSliceRenderer.draw(gui, rl, el.x, el.y, el.w, el.h, el.sliceSize, 256, 256);
        } else if (el.type.equals("text")) {
            gui.drawString(this.font, el.id, el.x, el.y, el.color, false);
        } else if (el.type.equals("image")) {
            gui.blit(rl, el.x, el.y, 0, 0, el.w, el.h, el.w, el.h);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = layout.elements.size() - 1; i >= 0; i--) {
            UILayout.UIElementData el = layout.elements.get(i);
            if (mouseX >= el.x && mouseX <= el.x + el.w && mouseY >= el.y && mouseY <= el.y + el.h) {
                UIRuntime.handleElementClick(el);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

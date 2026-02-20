package com.rpguimaker.client.screen;

import com.rpguimaker.RPGUIManager;
import com.rpguimaker.data.LayoutExporter;
import com.rpguimaker.data.LayoutLoader;
import com.rpguimaker.data.UILayout;
import com.rpguimaker.data.TextureDiscovery;
import com.rpguimaker.client.screen.component.TextureBrowserComponent;
import com.rpguimaker.client.screen.component.ProjectManagerComponent;
import com.rpguimaker.engine.rendering.NineSliceRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * The Enhanced In-Game UI Studio Editor.
 */
public class StudioScreen extends Screen {

    private final UILayout layout = new UILayout();
    private UILayout.UIElementData selectedElement = null;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private boolean showTextureBrowser = false;
    private boolean showProjectManager = false;
    private double dragStartX, dragStartY;
    private TextureBrowserComponent textureBrowser;
    private ProjectManagerComponent projectManager;

    private EditBox idInput;
    private EditBox linkInput;

    private static final ResourceLocation GRID_TEXTURE = ResourceLocation.fromNamespaceAndPath("rpguimaker",
            "textures/gui/grid.png");
    private static final ResourceLocation DEFAULT_UI_TEXTURE = ResourceLocation.fromNamespaceAndPath("rpguimaker",
            "textures/gui/default_panel.png");

    public StudioScreen() {
        super(Component.literal("RPG UI Studio"));
        layout.guiName = "new_rpg_gui";
    }

    @Override
    protected void init() {
        super.init();

        // Scan for external textures when opening the studio
        com.rpguimaker.data.DynamicTextureRegistry.scanAndLoad();
        if (layout.elements.isEmpty()) {
            addElement("panel");
        }

        // Initialize Texture Browser
        List<ResourceLocation> allTextures = TextureDiscovery.findAllTextures();
        this.textureBrowser = new TextureBrowserComponent(allTextures, 10, 30, 150, this.height - 40);

        int panelW = 140;
        this.addRenderableWidget(Button.builder(Component.literal("SAVE"), b -> LayoutExporter.saveToFile(layout))
                .bounds(this.width - panelW + 5, this.height - 50, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("LOAD"), b -> {
            UILayout loaded = LayoutLoader.loadFromFile(layout.guiName);
            if (loaded != null) {
                layout.elements.clear();
                layout.elements.addAll(loaded.elements);
            }
        }).bounds(this.width - panelW + 70, this.height - 50, 60, 20).build());

        // Texture Browser Toggle
        this.addRenderableWidget(
                Button.builder(Component.literal("TEXTURES"), b -> showTextureBrowser = !showTextureBrowser)
                        .bounds(10, 5, 70, 20).build());

        // Project Manager Toggle
        this.addRenderableWidget(
                Button.builder(Component.literal("PROJECTS"), b -> {
                    showProjectManager = !showProjectManager;
                    if (showProjectManager) {
                        this.projectManager = new ProjectManagerComponent(LayoutLoader.listLayouts(), 10, 30, 150,
                                this.height - 40);
                    }
                }).bounds(170, 5, 70, 20).build());

        // Preview Button
        this.addRenderableWidget(Button.builder(Component.literal("PREVIEW"), b -> {
            LayoutExporter.saveToFile(layout); // Auto save before preview
            this.minecraft.setScreen(new DynamicRuntimeScreen(layout));
        }).bounds(90, 5, 70, 20).build());

        // Creation Toolbar
        int tbX = this.width - 280;
        this.addRenderableWidget(Button.builder(Component.literal("+ Panel"), b -> addElement("panel"))
                .bounds(tbX, 5, 60, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+ Text"), b -> addElement("text"))
                .bounds(tbX + 65, 5, 60, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+ Image"), b -> addElement("image"))
                .bounds(tbX + 130, 5, 60, 20).build());

        // Property Inputs (Hidden initially, placed in property panel area)
        idInput = new EditBox(this.font, this.width - panelW + 10, 50, 120, 12, Component.literal("ID"));
        idInput.setResponder(s -> {
            if (selectedElement != null)
                selectedElement.id = s;
        });
        this.addRenderableWidget(idInput);

        linkInput = new EditBox(this.font, this.width - panelW + 10, 180, 120, 12, Component.literal("LinkTo"));
        linkInput.setResponder(s -> {
            if (selectedElement != null)
                selectedElement.linkTo = s;
        });
        this.addRenderableWidget(linkInput);

        idInput.visible = false;
        linkInput.visible = false;
    }

    private void addElement(String type) {
        UILayout.UIElementData el = new UILayout.UIElementData();
        el.id = "element_" + layout.elements.size();
        el.type = type;
        el.x = 50;
        el.y = 50;
        el.w = 80;
        el.h = 80;
        el.color = 0xAA00D2FF;
        el.sliceSize = 4; // Default slice
        layout.elements.add(el);
        selectedElement = el;
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // HUD & Grid Info
        guiGraphics.drawCenteredString(this.font, "RPG UI STUDIO v1.0", this.width / 2, 8, 0x00D2FF);

        // Render Project elements
        for (UILayout.UIElementData el : layout.elements) {
            renderElement(guiGraphics, el, selectedElement == el);
        }

        if (showTextureBrowser) {
            textureBrowser.render(guiGraphics, this.font, mouseX, mouseY);
        }

        if (showProjectManager && projectManager != null) {
            projectManager.render(guiGraphics, this.font, mouseX, mouseY);
        }

        renderPropertiesPanel(guiGraphics);
    }

    private void renderElement(GuiGraphics gui, UILayout.UIElementData el, boolean selected) {
        ResourceLocation rl = DEFAULT_UI_TEXTURE;
        if (el.texture != null && !el.texture.isEmpty()) {
            rl = ResourceLocation.parse(el.texture);
        }

        if (el.type.equals("panel")) {
            NineSliceRenderer.draw(gui, rl, el.x, el.y, el.w, el.h, el.sliceSize, 256, 256);
        } else if (el.type.equals("text")) {
            gui.drawString(this.font, "TEXT ELEMENT", el.x, el.y, el.color, false);
        } else if (el.type.equals("image")) {
            gui.blit(rl, el.x, el.y, 0, 0, el.w, el.h, el.w, el.h);
        } else {
            gui.fill(el.x, el.y, el.x + el.w, el.y + el.h, el.color);
        }

        if (selected) {
            // Neon Cyan selection border
            int borderColor = 0xFF00E5FF;
            gui.renderOutline(el.x - 1, el.y - 1, el.w + 2, el.h + 2, borderColor);
            // Resize handle with glow effect
            gui.fill(el.x + el.w - 6, el.y + el.h - 6, el.x + el.w + 1, el.y + el.h + 1, borderColor);
            gui.fill(el.x + el.w - 4, el.y + el.h - 4, el.x + el.w - 1, el.y + el.h - 1, 0xFFFFFFFF);
        }

        // Element Label
        int labelColor = selected ? 0xFF00E5FF : 0xAAFFFFFF;
        gui.drawString(this.font, "[" + el.id + "]", el.x + 2, el.y - 10, labelColor, false);
    }

    private void renderPropertiesPanel(GuiGraphics gui) {
        int panelW = 140; // Slightly wider for better readability
        // Deep Slate background with slight transparency
        gui.fill(this.width - panelW, 0, this.width, this.height, 0xEE0F172A);

        // Panel Header
        gui.drawString(this.font, "PROPERTY EDITOR", this.width - panelW + 8, 10, 0x00E5FF, false);
        gui.fill(this.width - panelW + 8, 22, this.width - 8, 23, 0x22FFFFFF); // Divider

        if (selectedElement != null) {
            int y = 35;
            // ID Input Label
            gui.drawString(this.font, "ID:", this.width - panelW + 8, y, 0x88FFFFFF, false);
            idInput.visible = true;
            idInput.setX(this.width - panelW + 8);
            idInput.setY(y + 12);

            y += 35;
            drawProperty(gui, "POS X", String.valueOf(selectedElement.x), y);
            drawProperty(gui, "POS Y", String.valueOf(selectedElement.y), y += 15);
            drawProperty(gui, "WIDTH", String.valueOf(selectedElement.w), y += 15);
            drawProperty(gui, "HEIGHT", String.valueOf(selectedElement.h), y += 15);
            drawProperty(gui, "SLICE", String.valueOf(selectedElement.sliceSize), y += 15);

            String texName = selectedElement.texture != null
                    ? selectedElement.texture.substring(selectedElement.texture.lastIndexOf('/') + 1)
                    : "None";
            drawProperty(gui, "TEXTURE", texName, y += 15);

            y += 20;
            // LinkTo Input Label
            gui.drawString(this.font, "LINK TO:", this.width - panelW + 8, y, 0x88FFFFFF, false);
            linkInput.visible = true;
            linkInput.setX(this.width - panelW + 8);
            linkInput.setY(y + 12);

            // Neon accent for active selection info
            gui.fill(this.width - panelW, y + 25, this.width - panelW + 2, y + 40, 0xFF00E5FF);
        } else {
            idInput.visible = false;
            linkInput.visible = false;
            gui.drawString(this.font, "Select an element", this.width - panelW + 10, 40, 0x44FFFFFF, false);
        }
    }

    private void drawProperty(GuiGraphics gui, String label, String value, int y) {
        int panelW = 140;
        gui.drawString(this.font, label, this.width - panelW + 8, y, 0x88FFFFFF, false);
        gui.drawString(this.font, value, this.width - 10 - this.font.width(value), y, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showTextureBrowser && textureBrowser.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        if (showProjectManager && projectManager != null
                && projectManager.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showTextureBrowser && textureBrowser.mouseClicked(mouseX, mouseY, button)) {
            if (selectedElement != null && textureBrowser.getSelectedTexture() != null) {
                // Apply texture path to element
                selectedElement.texture = textureBrowser.getSelectedTexture().toString();
                RPGUIManager.LOGGER.info("Applied texture {} to {}", selectedElement.texture, selectedElement.id);
            }
            return true;
        }

        if (showProjectManager && projectManager != null && projectManager.mouseClicked(mouseX, mouseY, button)) {
            String selected = projectManager.getSelectedProject();
            if (selected != null) {
                UILayout loaded = LayoutLoader.loadFromFile(selected);
                if (loaded != null) {
                    this.layout.guiName = selected;
                    this.layout.elements.clear();
                    this.layout.elements.addAll(loaded.elements);
                    this.selectedElement = null;
                }
            }
            return true;
        }

        selectedElement = null;
        for (int i = layout.elements.size() - 1; i >= 0; i--) {
            UILayout.UIElementData el = layout.elements.get(i);
            if (isOverResizeHandle(el, mouseX, mouseY)) {
                selectedElement = el;
                isResizing = true;
                idInput.setValue(el.id);
                linkInput.setValue(el.linkTo != null ? el.linkTo : "");
                return true;
            }
            if (isMouseOverElement(el, mouseX, mouseY)) {
                selectedElement = el;
                isDragging = true;
                dragStartX = mouseX - el.x;
                dragStartY = mouseY - el.y;
                idInput.setValue(el.id);
                linkInput.setValue(el.linkTo != null ? el.linkTo : "");
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverElement(UILayout.UIElementData el, double mx, double my) {
        return mx >= el.x && mx <= el.x + el.w && my >= el.y && my <= el.y + el.h;
    }

    private boolean isOverResizeHandle(UILayout.UIElementData el, double mx, double my) {
        return mx >= el.x + el.w - 5 && mx <= el.x + el.w && my >= el.y + el.h - 5 && my <= el.y + el.h;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (selectedElement != null) {
            if (isResizing) {
                selectedElement.w = (int) Math.max(10, mouseX - selectedElement.x);
                selectedElement.h = (int) Math.max(10, mouseY - selectedElement.y);
            } else if (isDragging) {
                selectedElement.x = (int) (mouseX - dragStartX);
                selectedElement.y = (int) (mouseY - dragStartY);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        isResizing = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Helper for debugging/future use
    public ResourceLocation getGridTexture() {
        return GRID_TEXTURE;
    }
}

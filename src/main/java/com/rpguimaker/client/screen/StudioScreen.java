package com.rpguimaker.client.screen;

import com.rpguimaker.RPGUIManager;
import com.rpguimaker.data.LayoutExporter;
import com.rpguimaker.data.LayoutLoader;
import com.rpguimaker.data.UILayout;
import com.rpguimaker.data.TextureDiscovery;
import com.rpguimaker.client.screen.component.TextureBrowserComponent;
import com.rpguimaker.client.screen.component.ProjectManagerComponent;
import com.rpguimaker.client.screen.component.FlatButton;
import com.rpguimaker.engine.rendering.NineSliceRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Stack;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.lwjgl.glfw.GLFW;
import javax.annotation.Nonnull;

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
    private EditBox linkInput; // Action/Binding

    // Canvas Properties
    private EditBox canvasWInput, canvasHInput;

    // Gecko Properties
    private EditBox modelPathInput, animNameInput;
    private FlatButton canvasApplyBtn;

    // Undo/Redo & Clipboard
    private final Stack<String> undoStack = new Stack<>();
    private final Gson gson = new Gson();
    private String systemClipboard = "";

    // Viewport State
    private float zoom = 1.0f;
    private double panX = 0;
    private double panY = 0;
    private boolean isPanning = false;
    private double lastMouseX, lastMouseY;

    // Context Menu State
    private boolean showContextMenu = false;
    private int contextMenuX, contextMenuY;

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
            // Keep empty to show helper text
        }

        // Initialize Components
        List<ResourceLocation> allTextures = TextureDiscovery.findAllTextures();
        this.textureBrowser = new TextureBrowserComponent(allTextures, 40, 30, 200, this.height - 40);
        this.projectManager = new ProjectManagerComponent(LayoutLoader.listLayouts(), 40, 30, 200, this.height - 40);

        // --- TOP BAR (File & View) ---
        int topBarY = 2;
        int btnW = 30; // Icon-width
        int btnH = 20;
        int x = 5;

        // Projects
        this.addRenderableWidget(new FlatButton(x, topBarY, 60, btnH, Component.literal("Projects"),
                b -> showProjectManager = !showProjectManager, () -> "Manage Visual Projects"));
        x += 65;

        // Textures
        this.addRenderableWidget(new FlatButton(x, topBarY, 60, btnH, Component.literal("Textures"),
                b -> showTextureBrowser = !showTextureBrowser, () -> "Browse Loaded Textures"));
        x += 65;

        // Save
        this.addRenderableWidget(new FlatButton(x, topBarY, 40, btnH, Component.literal("Save"),
                b -> {
                    LayoutExporter.saveToFile(layout);
                }, () -> "Save current layout"));
        x += 45;

        // Load (Reload)
        this.addRenderableWidget(new FlatButton(x, topBarY, 40, btnH, Component.literal("Load"),
                b -> {
                    UILayout loaded = LayoutLoader.loadFromFile(layout.guiName);
                    if (loaded != null) {
                        layout.elements.clear();
                        layout.elements.addAll(loaded.elements);
                    }
                }, () -> "Reload from file"));
        x += 45;

        // Preview
        this.addRenderableWidget(new FlatButton(x, topBarY, 50, btnH, Component.literal("Preview"),
                b -> {
                    LayoutExporter.saveToFile(layout);
                    this.minecraft.setScreen(new DynamicRuntimeScreen(layout));
                }, () -> "Test Logic & Visuals"));

        // --- LEFT TOOLBAR (Tools) ---
        int toolY = 30;
        int toolW = 30;
        int toolH = 30;

        this.addRenderableWidget(new FlatButton(5, toolY, toolW, toolH, Component.literal("[P]"),
                b -> addElement("panel"), () -> "Add Panel (Container)"));
        toolY += 35;

        this.addRenderableWidget(new FlatButton(5, toolY, toolW, toolH, Component.literal("[T]"),
                b -> addElement("text"), () -> "Add Text Label"));
        toolY += 35;

        this.addRenderableWidget(new FlatButton(5, toolY, toolW, toolH, Component.literal("[I]"),
                b -> addElement("image"), () -> "Add Image / Icon"));
        toolY += 35;

        this.addRenderableWidget(new FlatButton(5, toolY, toolW, toolH, Component.literal("[A]"),
                b -> addElement("gecko"), () -> "Add Animated Model"));

        // --- PROPERTY PANEL ---
        int panelW = 140;
        int inputsX = this.width - panelW + 10;

        idInput = new EditBox(this.font, inputsX, 70, 120, 12, Component.literal("ID"));
        idInput.setResponder(s -> {
            if (selectedElement != null)
                selectedElement.id = s;
        });
        this.addRenderableWidget(idInput);

        linkInput = new EditBox(this.font, inputsX, 220, 120, 12, Component.literal("Action"));
        linkInput.setResponder(s -> {
            if (selectedElement != null)
                selectedElement.linkTo = s;
        });
        this.addRenderableWidget(linkInput);

        // Canvas Inputs
        canvasWInput = new EditBox(this.font, inputsX, 70, 50, 12, Component.literal("CW"));
        canvasWInput.setResponder(s -> {
            try {
                layout.canvas.w = Integer.parseInt(s);
            } catch (Exception e) {
            }
        });
        this.addRenderableWidget(canvasWInput);

        canvasHInput = new EditBox(this.font, inputsX + 60, 70, 50, 12, Component.literal("CH"));
        canvasHInput.setResponder(s -> {
            try {
                layout.canvas.h = Integer.parseInt(s);
            } catch (Exception e) {
            }
        });
        this.addRenderableWidget(canvasHInput);

        // Gecko Inputs
        modelPathInput = new EditBox(this.font, inputsX, 150, 120, 12, Component.literal("Model"));
        modelPathInput.setResponder(s -> {
            if (selectedElement != null)
                selectedElement.model = s;
        });
        this.addRenderableWidget(modelPathInput);

        animNameInput = new EditBox(this.font, inputsX, 180, 120, 12, Component.literal("Anim"));
        animNameInput.setResponder(s -> {
            if (selectedElement != null)
                selectedElement.animation = s;
        });
        this.addRenderableWidget(animNameInput);

        this.addRenderableWidget(animNameInput);

        canvasApplyBtn = new FlatButton(0, 0, 110, 16, Component.literal("Apply Resize"), b -> {
            try {
                layout.canvas.w = Integer.parseInt(canvasWInput.getValue());
                layout.canvas.h = Integer.parseInt(canvasHInput.getValue());
            } catch (Exception e) {
            }
        }, () -> "Confirm Canvas Size Changes");
        this.addRenderableWidget(canvasApplyBtn);

        hideAllInputs();
    }

    private void hideAllInputs() {
        idInput.visible = false;
        linkInput.visible = false;
        canvasWInput.visible = false;
        canvasHInput.visible = false;
        modelPathInput.visible = false;
        animNameInput.visible = false;
        canvasApplyBtn.visible = false;
    }

    // Coordinate conversion
    private double toScreenX(double canvasX) {
        return 40 + panX + (canvasX * zoom);
    }

    private double toScreenY(double canvasY) {
        return 25 + panY + (canvasY * zoom);
    }

    private double toCanvasX(double screenX) {
        return (screenX - 40 - panX) / zoom;
    }

    private double toCanvasY(double screenY) {
        return (screenY - 25 - panY) / zoom;
    }

    private void addElement(String type) {
        UILayout.UIElementData el = new UILayout.UIElementData();
        el.id = "el_" + layout.elements.size();
        el.type = type;
        // Center of current view
        el.x = (int) toCanvasX(this.width / 2);
        el.y = (int) toCanvasY(this.height / 2);
        el.w = 60;
        el.h = 60;
        el.color = 0xFFCCCCCC; // Light grey default
        el.sliceSize = 4;
        saveUndo(); // Save state before adding
        layout.elements.add(el);
        selectedElement = el;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Override to prevent default level blur or background rendering
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Fix: Use solid background instead of scene background to prevent Z-order/blur
        // issues
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF101010);

        // --- CANVAS AREA ---
        // Draw Grid Background (Infinite)
        guiGraphics.fill(40, 25, this.width - 140, this.height, 0xFF1e1e1e);

        // Draw Canvas Boundary
        double cx = toScreenX(0);
        double cy = toScreenY(0);
        double rw = layout.canvas.w * zoom;
        double rh = layout.canvas.h * zoom;

        // Fill canvas area
        guiGraphics.fill((int) cx, (int) cy, (int) (cx + rw), (int) (cy + rh), 0xFF252525);
        // Outline
        guiGraphics.renderOutline((int) cx - 1, (int) cy - 1, (int) rw + 2, (int) rh + 2, 0xFFFFFFFF);

        // Zoom Info
        guiGraphics.drawString(this.font, String.format("Zoom: %.0f%%", zoom * 100), this.width - 200, 5, 0xFF888888,
                false);

        // Render Elements
        for (UILayout.UIElementData el : layout.elements) {
            renderElement(guiGraphics, el, selectedElement == el);
        }

        // Helper Text if Empty
        if (layout.elements.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "Welcome to RPG UI Maker", (this.width - 140 + 40) / 2,
                    this.height / 2 - 20, 0xFFFFFFFF);
            guiGraphics.drawCenteredString(this.font, "Right-click or use Left Toolbar to add elements.",
                    (this.width - 140 + 40) / 2,
                    this.height / 2, 0xFFAAAAAA);
        }

        // --- OVERLAYS (Backgrounds) ---
        // Top Bar Background (Opaque Black)
        guiGraphics.fill(0, 0, this.width, 25, 0xFF111111);
        guiGraphics.hLine(0, this.width, 25, 0xFF555555);

        // Left Toolbar Background (Opaque Black)
        guiGraphics.fill(0, 25, 40, this.height, 0xFF111111);
        guiGraphics.vLine(40, 25, this.height, 0xFF555555);

        // Render Widgets (Buttons)
        renderPropertiesPanel(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Windows
        if (showTextureBrowser)
            textureBrowser.render(guiGraphics, this.font, mouseX, mouseY);
        if (showProjectManager && projectManager != null)
            projectManager.render(guiGraphics, this.font, mouseX, mouseY);

        // Context Menu
        if (showContextMenu) {
            renderContextMenu(guiGraphics, mouseX, mouseY);
        }

        // Tooltips
        if (this.getChildAt(mouseX, mouseY).isPresent()
                && this.getChildAt(mouseX, mouseY).get() instanceof FlatButton fb) {
            String tt = fb.getTooltipText();
            if (tt != null)
                guiGraphics.renderTooltip(this.font, Component.literal(tt), mouseX, mouseY);
        }

        // HUD Overlay (Bottom Left)
        // renderHUD(guiGraphics);
    }

    private void renderHUD(GuiGraphics gui) {
        int x = 45;
        int y = this.height - 85;
        int w = 130;
        int h = 80;

        gui.fill(x, y, x + w, y + h, 0xAA000000); // Semi-transparent black box
        gui.renderOutline(x - 1, y - 1, w + 2, h + 2, 0xFF555555);

        int ty = y + 5;
        gui.drawString(this.font, "[ Controls ]", x + 5, ty, 0xFFFFFF55, false);
        ty += 15;
        gui.drawString(this.font, "Right Click: Menu", x + 5, ty, 0xFFDDDDDD, false);
        ty += 15;
        gui.drawString(this.font, "Scroll: Zoom", x + 5, ty, 0xFFDDDDDD, false);
        ty += 15;
        gui.drawString(this.font, "Middle Click: Pan", x + 5, ty, 0xFFDDDDDD, false);
        ty += 15;
        gui.drawString(this.font, "Drag Left: Add", x + 5, ty, 0xFFDDDDDD, false);
    }

    private void renderElement(GuiGraphics gui, UILayout.UIElementData el, boolean selected) {
        ResourceLocation rl = DEFAULT_UI_TEXTURE;
        if (el.texture != null && !el.texture.isEmpty()) {
            rl = ResourceLocation.parse(el.texture);
        }

        // Transform coords
        int rx = (int) toScreenX(el.x);
        int ry = (int) toScreenY(el.y);
        int rw = (int) (el.w * zoom);
        int rh = (int) (el.h * zoom);

        if (el.type.equals("panel")) {
            NineSliceRenderer.draw(gui, rl, rx, ry, rw, rh, el.sliceSize, 256, 256);
        } else if (el.type.equals("text")) {
            // Scale text? For now just draw at pos
            gui.pose().pushPose();
            gui.pose().translate(rx, ry, 0);
            gui.pose().scale(zoom, zoom, 1);
            gui.drawString(this.font, "TEXT", 0, 0, el.color, false);
            gui.pose().popPose();
        } else if (el.type.equals("image")) {
            gui.blit(rl, rx, ry, 0, 0, rw, rh, rw, rh);
        } else if (el.type.equals("gecko")) {
            gui.fill(rx, ry, rx + rw, ry + rh, 0xFF550055);
            // Scaled text
            gui.pose().pushPose();
            gui.pose().translate(rx + rw / 2, ry + rh / 2, 0);
            gui.pose().scale(zoom, zoom, 1);
            gui.drawCenteredString(this.font, "GECKO", 0, -4, 0xFFEEAAAA);
            gui.pose().popPose();
        }

        if (selected) {
            gui.renderOutline(rx - 1, ry - 1, rw + 2, rh + 2, 0xFF00FF00);
            gui.fill(rx + rw - 5, ry + rh - 5, rx + rw, ry + rh, 0xFFFFFFFF);
            gui.drawCenteredString(this.font, el.x + "," + el.y, rx + rw / 2, ry - 10, 0xFFFFFF00);
        }
    }

    private void renderPropertiesPanel(GuiGraphics gui) {
        int panelW = 140;
        int x = this.width - panelW;

        // Background - Solid Gray
        gui.fill(x, 25, this.width, this.height, 0xFF353535);
        // Divider Border
        gui.vLine(x, 25, this.height, 0xFF888888);

        gui.drawCenteredString(this.font, "PROPERTIES", x + panelW / 2, 35, 0xFFFFFFFF);

        if (selectedElement != null) {
            setElementInputs(gui, x, 60);

        } else {
            hideAllInputs();
            // Show Canvas Properties
            gui.drawCenteredString(this.font, "-- CANVAS --", x + panelW / 2, 60, 0xFFFFFFAA);

            int y = 85;
            gui.drawString(this.font, "Size (W / H):", x + 10, y - 12, 0xFFFFFFFF, false);

            canvasWInput.visible = true;
            canvasWInput.setX(x + 10);
            canvasWInput.setY(y); // Explicit Y for input
            if (!canvasWInput.isFocused())
                canvasWInput.setValue(String.valueOf(layout.canvas.w));

            canvasHInput.visible = true;
            canvasHInput.setX(x + 70);
            canvasHInput.setY(y); // Explicit Y for input
            if (!canvasHInput.isFocused())
                canvasHInput.setValue(String.valueOf(layout.canvas.h));

            canvasApplyBtn.visible = true;
            canvasApplyBtn.setX(x + 10);
            canvasApplyBtn.setY(y + 18);
        }
    }

    private void renderContextMenu(GuiGraphics gui, int mouseX, int mouseY) {
        int w = 100;
        int h = 85; // 4 items * 20 + padding
        int x = contextMenuX;
        int y = contextMenuY;

        gui.fill(x, y, x + w, y + h, 0xFF353535);
        gui.renderOutline(x - 1, y - 1, w + 2, h + 2, 0xFFFFFFFF);

        String[] options = { "Add Panel", "Add Text", "Handle Image", "Add Model" };
        for (int i = 0; i < options.length; i++) {
            int iy = y + 5 + (i * 20);
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= iy && mouseY < iy + 20;
            if (hovered) {
                gui.fill(x + 2, iy, x + w - 2, iy + 20, 0xFF555555);
            }
            gui.drawString(this.font, options[i], x + 10, iy + 6, 0xFFFFFFFF, false);
        }
    }

    private void setElementInputs(GuiGraphics gui, int x, int y) {
        // ID & Common
        gui.drawString(this.font, "ID:", x + 10, y, 0xFFAAAAAA, false);
        idInput.visible = true;
        idInput.setX(x + 10);
        idInput.setY(y + 12);
        if (!idInput.isFocused())
            idInput.setValue(selectedElement.id);

        y += 40;
        // Pos
        gui.drawString(this.font, "X: " + selectedElement.x + "  Y: " + selectedElement.y, x + 10, y, 0xFFAAAAAA,
                false);
        y += 15;
        gui.drawString(this.font, "W: " + selectedElement.w + "  H: " + selectedElement.h, x + 10, y, 0xFFAAAAAA,
                false);

        y += 30;
        if (selectedElement.type.equals("gecko")) {
            gui.drawString(this.font, "Model Path:", x + 10, y, 0xFFAAAAAA, false);
            modelPathInput.visible = true;
            modelPathInput.setX(x + 10);
            modelPathInput.setY(y + 12);
            if (!modelPathInput.isFocused())
                modelPathInput.setValue(selectedElement.model == null ? "" : selectedElement.model);

            y += 30;
            gui.drawString(this.font, "Animation:", x + 10, y, 0xFFAAAAAA, false);
            animNameInput.visible = true;
            animNameInput.setX(x + 10);
            animNameInput.setY(y + 12);
            if (!animNameInput.isFocused())
                animNameInput.setValue(selectedElement.animation == null ? "" : selectedElement.animation);
        } else {
            // Normal link logic
            gui.drawString(this.font, "Action / Binding:", x + 10, y, 0xFFAAAAAA, false);
            linkInput.visible = true;
            linkInput.setX(x + 10);
            linkInput.setY(y + 22);
            if (!linkInput.isFocused())
                linkInput.setValue(selectedElement.linkTo == null ? "" : selectedElement.linkTo);
        }
    }

    // --- SHORTCUTS & EDITING ---

    private void saveUndo() {
        // Serialize current elements state
        String json = gson.toJson(layout.elements);
        undoStack.push(json);
        // Limit stack size? For now infinite
    }

    private void restoreUndo() {
        if (undoStack.isEmpty())
            return;
        String json = undoStack.pop();
        try {
            List<UILayout.UIElementData> oldElements = gson.fromJson(json,
                    new TypeToken<List<UILayout.UIElementData>>() {
                    }.getType());
            layout.elements.clear();
            layout.elements.addAll(oldElements);
            selectedElement = null; // Deselect to avoid ghost selection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyValues() {
        if (selectedElement == null)
            return;
        systemClipboard = gson.toJson(selectedElement);
    }

    private void pasteValues() {
        if (systemClipboard == null || systemClipboard.isEmpty())
            return;
        try {
            saveUndo(); // Save before paste
            UILayout.UIElementData newEl = gson.fromJson(systemClipboard, UILayout.UIElementData.class);
            newEl.id = newEl.id + "_copy";
            newEl.x += 10;
            newEl.y += 10;
            layout.elements.add(newEl);
            selectedElement = newEl;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cutValues() {
        if (selectedElement == null)
            return;
        copyValues();
        deleteSelected();
    }

    private void deleteSelected() {
        if (selectedElement == null)
            return;
        saveUndo();
        layout.elements.remove(selectedElement);
        selectedElement = null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle EditBoxes first
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        // Ctrl Shortcuts
        if (hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_Z) {
                restoreUndo();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_C) {
                copyValues();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_V) {
                pasteValues();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_X) {
                cutValues();
                return true;
            }
        }

        // Delete
        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            // Only if not editing text (already handled by super)
            if (getFocused() == null) {
                deleteSelected();
                return true;
            }
        }

        return false;
    }

    // Input Handling (Mouse Click/Drag)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showTextureBrowser && textureBrowser.mouseClicked(mouseX, mouseY, button)) {
            if (selectedElement != null && textureBrowser.getSelectedTexture() != null) {
                selectedElement.texture = textureBrowser.getSelectedTexture().toString();
            }
            return true;
        }
        if (showProjectManager && projectManager != null && projectManager.mouseClicked(mouseX, mouseY, button)) {
            // Load project logic
            String selected = projectManager.getSelectedProject();
            if (selected != null) {
                UILayout loaded = LayoutLoader.loadFromFile(selected);
                if (loaded != null) {
                    this.layout.elements.clear();
                    this.layout.elements.addAll(loaded.elements);
                    this.selectedElement = null;
                }
            }
            return true;
        }

        // Overlay blocking
        if (showTextureBrowser || showProjectManager)
            return super.mouseClicked(mouseX, mouseY, button);

        // Context Menu Click
        if (showContextMenu) {
            if (mouseX >= contextMenuX && mouseX <= contextMenuX + 100 &&
                    mouseY >= contextMenuY && mouseY <= contextMenuY + 85) {
                int index = (int) ((mouseY - (contextMenuY + 5)) / 20);
                if (index == 0)
                    addElement("panel");
                else if (index == 1)
                    addElement("text");
                else if (index == 2)
                    addElement("image");
                else if (index == 3)
                    addElement("gecko");
                showContextMenu = false;
                return true;
            } else {
                showContextMenu = false; // Close if clicked outside
                return true;
            }
        }

        // Context Menu Open (Right Click)
        if (button == 1) { // Right Click
            showContextMenu = true;
            contextMenuX = (int) mouseX;
            contextMenuY = (int) mouseY;
            return true;
        }

        // Unfocus if clicking background
        setFocused(null);

        selectedElement = null;

        // Handle Pan Start (Middle Click or Space+Left)
        if (button == 2 || (button == 0 && hasShiftDown())) { // Using Shift instead of Space for easier drag
            isPanning = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        double cx = toCanvasX(mouseX);
        double cy = toCanvasY(mouseY);

        // Search reverse for top-most element
        for (int i = layout.elements.size() - 1; i >= 0; i--) {
            UILayout.UIElementData el = layout.elements.get(i);

            // Check Resize Handle (transformed)
            // Logic: Is mouse near the bottom-right corner in CANVAS space?
            if (cx >= el.x + el.w - 5.0 / zoom && cx <= el.x + el.w && cy >= el.y + el.h - 5.0 / zoom
                    && cy <= el.y + el.h) {
                selectedElement = el;
                isResizing = true;
                idInput.setValue(el.id);
                linkInput.setValue(el.linkTo != null ? el.linkTo : "");
                modelPathInput.setValue(el.model != null ? el.model : "");
                animNameInput.setValue(el.animation != null ? el.animation : "");
                return true;
            }
            // Check Body
            if (cx >= el.x && cx <= el.x + el.w && cy >= el.y && cy <= el.y + el.h) {
                selectedElement = el;
                isDragging = true;
                dragStartX = cx - el.x;
                dragStartY = cy - el.y; // Capture offset in canvas space
                idInput.setValue(el.id);
                linkInput.setValue(el.linkTo != null ? el.linkTo : "");
                modelPathInput.setValue(el.model != null ? el.model : "");
                animNameInput.setValue(el.animation != null ? el.animation : "");
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isPanning) {
            panX += (mouseX - lastMouseX);
            panY += (mouseY - lastMouseY);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        if (selectedElement != null) {
            double cx = toCanvasX(mouseX);
            double cy = toCanvasY(mouseY);

            if (isResizing) {
                selectedElement.w = (int) Math.max(10, cx - selectedElement.x);
                selectedElement.h = (int) Math.max(10, cy - selectedElement.y);
            } else if (isDragging) {
                selectedElement.x = (int) (cx - dragStartX);
                selectedElement.y = (int) (cy - dragStartY);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        isResizing = false;
        isPanning = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showTextureBrowser && textureBrowser.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;
        if (showProjectManager && projectManager != null
                && projectManager.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        // Zoom
        if (scrollY != 0) {
            float zoomSpeed = 0.1f;
            float newZoom = zoom + (float) (scrollY * zoomSpeed);
            newZoom = Math.max(0.25f, Math.min(newZoom, 4.0f));

            // Optional: Zoom towards mouse pointer
            // For simple implementation, just zoom
            zoom = newZoom;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

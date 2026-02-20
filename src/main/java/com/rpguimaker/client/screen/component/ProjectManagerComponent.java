package com.rpguimaker.client.screen.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.util.List;

/**
 * UI Component for managing and selecting different layout projects.
 */
public class ProjectManagerComponent {
    private final List<String> projectNames;
    private final int x, y, w, h;
    private int scrollOffset = 0;
    private String selectedProject = null;

    public ProjectManagerComponent(List<String> projectNames, int x, int y, int w, int h) {
        this.projectNames = projectNames;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
        // Dark Gray panel background
        gui.fill(x, y, x + w, y + h, 0xFF353535);
        gui.renderOutline(x - 1, y - 1, w + 2, h + 2, 0xFF888888);

        gui.drawString(font, "PROJECTS", x + 5, y + 5, 0xFFFFFFFF, false);
        gui.fill(x + 5, y + 15, x + w - 5, y + 16, 0xFF888888);

        int itemY = y + 20;
        for (int i = scrollOffset; i < projectNames.size(); i++) {
            if (itemY + 15 > y + h)
                break;

            String name = projectNames.get(i);
            boolean isHovered = mouseX >= x + 5 && mouseX <= x + w - 5 && mouseY >= itemY && mouseY <= itemY + 12;
            int color = 0xFFFFFFFF; // Always white text

            if (name.equals(selectedProject)) {
                gui.fill(x + 2, itemY, x + w - 2, itemY + 12, 0xFF555555); // Selected Gray
            } else if (isHovered) {
                gui.fill(x + 2, itemY, x + w - 2, itemY + 12, 0xFF444444); // Hover Gray
            }

            gui.drawString(font, name, x + 8, itemY + 2, color, false);
            itemY += 15;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX > x + w || mouseY < y || mouseY > y + h)
            return false;

        int itemY = y + 20;
        for (int i = scrollOffset; i < projectNames.size(); i++) {
            if (itemY + 15 > y + h)
                break;

            if (mouseX >= x + 5 && mouseX <= x + w - 5 && mouseY >= itemY && mouseY <= itemY + 12) {
                selectedProject = projectNames.get(i);
                return true;
            }
            itemY += 15;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            scrollOffset = Math.max(0, Math.min(projectNames.size() - 1, scrollOffset - (int) scrollY));
            return true;
        }
        return false;
    }

    public String getSelectedProject() {
        return selectedProject;
    }
}

package com.rpguimaker.client.screen.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.util.function.Supplier;

public class FlatButton extends Button {

    private final Supplier<String> tooltipSupplier;
    private final int hoverColor = 0x44FFFFFF;
    private final int pressedColor = 0x66FFFFFF;

    public FlatButton(int x, int y, int width, int height, Component message, OnPress onPress,
            Supplier<String> tooltip) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.tooltipSupplier = tooltip;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // High Visibility Background
        int bgColor = isHovered ? 0xFF555555 : 0xFF333333; // Lighter gray on hover, Dark gray default
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

        // Solid Border
        guiGraphics.renderOutline(getX(), getY(), width, height, 0xFF777777);

        // Always White Text
        int color = 0xFFFFFFFF;
        int textWidth = net.minecraft.client.Minecraft.getInstance().font.width(getMessage());
        int textX = getX() + (width - textWidth) / 2;
        int textY = getY() + (height - 8) / 2;

        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, getMessage(), textX, textY, color,
                false);
    }

    public String getTooltipText() {
        return tooltipSupplier != null ? tooltipSupplier.get() : null;
    }
}

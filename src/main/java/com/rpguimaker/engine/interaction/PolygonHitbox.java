package com.rpguimaker.engine.interaction;

import net.minecraft.client.gui.components.events.GuiEventListener;
import java.util.List;

/**
 * Point-in-Polygon detection for UI interaction.
 */
public class PolygonHitbox {
    private final List<Point> vertices;

    public PolygonHitbox(List<Point> vertices) {
        this.vertices = vertices;
    }

    /**
     * Ray Casting Algorithm
     */
    public boolean isInside(double x, double y) {
        int n = vertices.size();
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if (((vertices.get(i).y > y) != (vertices.get(j).y > y)) &&
                    (x < (vertices.get(j).x - vertices.get(i).x) * (y - vertices.get(i).y)
                            / (vertices.get(j).y - vertices.get(i).y) + vertices.get(i).x)) {
                inside = !inside;
            }
        }
        return inside;
    }

    public record Point(double x, double y) {
    }
}

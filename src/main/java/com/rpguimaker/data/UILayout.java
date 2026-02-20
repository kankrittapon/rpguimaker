package com.rpguimaker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure for the UI Layout, serializable to/from JSON.
 */
public class UILayout {
    public String guiName;
    public CanvasInfo canvas;
    public List<UIElementData> elements = new ArrayList<>();

    public static class CanvasInfo {
        public int w, h;
    }

    public static class UIElementData {
        public String id;
        public String type;
        public int x, y, w, h;
        public int color;
        public float rotation;
        public String texture;
        public int sliceSize; // For Nine-Slice
        public List<PolygonHitboxData> hitbox;
        public String linkTo; // GUI Link logic
    }

    public static class PolygonHitboxData {
        public double x, y;
    }
}

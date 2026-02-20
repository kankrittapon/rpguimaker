package com.rpguimaker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure for the UI Layout, serializable to/from JSON.
 */
public class UILayout {
    public String guiName;
    public CanvasInfo canvas = new CanvasInfo();
    public List<UIElementData> elements = new ArrayList<>();

    public static class CanvasInfo {
        public int w = 400; // Default Width
        public int h = 300; // Default Height
    }

    public static class UIElementData {
        public String id;
        public String type; // panel, text, image, gecko
        public int x, y, w, h;
        public int color;
        public float rotation;
        public String texture;
        public String model; // Path to geo.json
        public String animation; // Animation name
        public int sliceSize; // For Nine-Slice
        public List<PolygonHitboxData> hitbox;
        public String linkTo; // GUI Link / Logic Binding
    }

    public static class PolygonHitboxData {
        public double x, y;
    }
}

package com.rpguimaker.engine.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/**
 * Custom Vertex Rendering for Non-Rectangular UI Elements.
 */
public class VertexMapper {

    public static void drawPolygon(Matrix4f matrix, Tesselator tesselator, float r, float g, float b, float a,
            float... vertices) {
        if (vertices.length % 2 != 0)
            return;

        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN,
                DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < vertices.length; i += 2) {
            bufferBuilder.addVertex(matrix, vertices[i], vertices[i + 1], 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}

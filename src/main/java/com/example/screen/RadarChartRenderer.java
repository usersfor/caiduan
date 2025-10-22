package com.example.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * 极简雷达图渲染器（只支持6边形）
 */
public class SimpleRadarChartRenderer {

    private final TextRenderer textRenderer;
    private final String[] labels;
    private final int[] colors;
    private final int centerX, centerY, radius;

    public SimpleRadarChartRenderer(TextRenderer textRenderer,
                                    String[] labels,
                                    int[] colors,
                                    int centerX, int centerY, int radius) {
        this.textRenderer = textRenderer;
        this.labels = labels;
        this.colors = colors;
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public void render(DrawContext ctx, float[] values) {
        if (values.length != 6) return;

        // 1. 画3个同心圆
        for (int i = 1; i <= 3; i++) {
            drawHexagon(ctx, centerX, centerY, radius * i / 3, 0x44FFFFFF);
        }

        // 2. 画6条轴线
        for (int j = 0; j < 6; j++) {
            int ax = (int) (centerX + radius * Math.cos(Math.PI * 2 * j / 6));
            int ay = (int) (centerY + radius * Math.sin(Math.PI * 2 * j / 6));
            drawLine(ctx, centerX, centerY, ax, ay, 0x44FFFFFF);
        }

        // 3. 画数据多边形（只描边，不填充）
        int[] px = new int[6];
        int[] py = new int[6];
        for (int j = 0; j < 6; j++) {
            float v = Math.min(values[j], 1f);
            px[j] = (int) (centerX + radius * v * Math.cos(Math.PI * 2 * j / 6));
            py[j] = (int) (centerY + radius * v * Math.sin(Math.PI * 2 * j / 6));
        }
        for (int j = 0; j < 6; j++) {
            int k = (j + 1) % 6;
            drawLine(ctx, px[j], py[j], px[k], py[k], 0xFF00FF00);
        }

        // 4. 画顶点小圆点 + 文字
        for (int j = 0; j < 6; j++) {
            fillCircle(ctx, px[j], py[j], 3, colors[j]);
            String txt = labels[j] + ":" + (int) (values[j] * 100);
            int tw = textRenderer.getWidth(txt);
            ctx.drawText(textRenderer, txt, px[j] - tw / 2, py[j] - 10, 0xFFFFFFFF, true);
        }
    }

    /* ============ 极简工具 ============= */

    private static void drawLine(DrawContext ctx, int x0, int y0, int x1, int y1, int color) {
        // 只支持水平/垂直/45°，够用
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            ctx.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    private static void fillCircle(DrawContext ctx, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++)
                if (dx * dx + dy * dy <= r * r)
                    ctx.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
    }

    private static void drawHexagon(DrawContext ctx, int cx, int cy, int r, int color) {
        for (int i = 0; i < 6; i++) {
            int x0 = (int) (cx + r * Math.cos(Math.PI * 2 * i / 6));
            int y0 = (int) (cy + r * Math.sin(Math.PI * 2 * i / 6));
            int x1 = (int) (cx + r * Math.cos(Math.PI * 2 * (i + 1) / 6));
            int y1 = (int) (cy + r * Math.sin(Math.PI * 2 * (i + 1) / 6));
            drawLine(ctx, x0, y0, x1, y1, color);
        }
    }
}
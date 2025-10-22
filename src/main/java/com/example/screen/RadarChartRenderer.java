package com.example.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.*;

/**
 * 1.20.1 Fabric 雷达图渲染器
 * 修复：数据点位置正确对应数值，100%值显示在最外圈
 */
public class RadarChartRenderer {

    /* ====================== 字段 ====================== */

    private final TextRenderer textRenderer;
    private final String[] talentNames;
    private final int[] talentColors;
    private final float[] talentValues;
    private final float[] displayValues;
    private final float[] targetValues;

    /* 位置与尺寸 */
    private int centerX, centerY, maxRadius, segments = 32;
    private int leftBound, rightBound, topBound, bottomBound;

    /* 显示曲线 */
    private float displayScale = 1.0f, softCap = 1.0f;

    /* 动画 */
    private long lastAnimationUpdateTime = System.currentTimeMillis();
    private float animationProgress = 0f;
    private boolean isAnimating = false;
    private float gridRotation = 0f;
    private final Map<Integer, Float> labelAlpha = new HashMap<>();
    private boolean needsAnimationUpdate = false;

    /* 缓存 */
    private float[] cachedXPointsF = null, cachedYPointsF = null;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 16; // 60 FPS

    /* 性能 */
    private long lastFrameTime = System.currentTimeMillis();
    private float currentFPS = 60f;

    private final RadarChartConfig config;

    /* ====================== 构造 ====================== */
    public RadarChartRenderer(TextRenderer textRenderer,
                              String[] talentNames,
                              int[] talentColors) {
        this(textRenderer, talentNames, talentColors, new RadarChartConfig());
    }
    public RadarChartRenderer(TextRenderer textRenderer,
                              String[] talentNames,
                              int[] talentColors,
                              RadarChartConfig config) {
        this.textRenderer = textRenderer;
        this.talentNames = talentNames;
        this.talentColors = talentColors;
        int n = talentNames.length;
        this.talentValues = new float[n];
        this.displayValues = new float[n];
        this.targetValues = new float[n];
        this.config = config;
        for (int i = 0; i < n; i++) labelAlpha.put(i, 0f);
    }

    /* ====================== 对外 API ====================== */

    public void setPosition(int centerX, int centerY, int maxRadius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.maxRadius = maxRadius;
        clearCache();
    }

    public void setBounds(int left, int right, int top, int bottom) {
        this.leftBound = left;
        this.rightBound = right;
        this.topBound = top;
        this.bottomBound = bottom;
    }

    public void adjustLayout(int screenWidth, int screenHeight) {
        int chartSize = Math.min(screenWidth - 100, screenHeight - 150);
        this.maxRadius = chartSize / 2;
        this.centerX = screenWidth / 2;
        this.centerY = screenHeight / 2 - 20;
        setBounds(50, screenWidth - 50, 50, screenHeight - 100);
        clearCache();
    }

    public void setValues(float[] values) {
        if (values.length == talentValues.length) {
            // ✅ 用当前显示值作为动画起点
            System.arraycopy(displayValues, 0, talentValues, 0, values.length);
            System.arraycopy(values, 0, targetValues, 0, values.length);
            startValueAnimation();
            forceAnimationUpdate();
        }
    }

    public void setValuesImmediate(float[] values) {
        if (values.length == talentValues.length) {
            System.arraycopy(values, 0, talentValues, 0, values.length);
            System.arraycopy(values, 0, displayValues, 0, values.length);
            System.arraycopy(values, 0, targetValues, 0, values.length);
            animationProgress = 1f;
            isAnimating = false;
            needsAnimationUpdate = true;
            clearCache();
        }
    }

    public void setSegments(int segments) {
        this.segments = Math.max(8, segments);
        clearCache();
    }

    public void setDisplayParams(float scale, float softCap) {
        this.displayScale = MathHelper.clamp(scale, 0.1f, 1.0f);
        this.softCap = MathHelper.clamp(softCap, 0.1f, 1.0f);
        clearCache();
    }

    public float getCurrentFPS() {
        return currentFPS;
    }

    public void forceAnimationUpdate() {
        needsAnimationUpdate = true;
        updateAnimations();
    }

    /* ====================== 动画 ====================== */

    public void updateAnimations() {
        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastAnimationUpdateTime) / 1000f, 0.05f);
        lastAnimationUpdateTime = now;

        gridRotation += dt * config.gridRotationSpeed;
        if (gridRotation > Math.PI * 2) gridRotation -= Math.PI * 2;

        if (isAnimating) {
            animationProgress += dt * config.animationSpeed;
            if (animationProgress >= 1f) {
                animationProgress = 1f;
                isAnimating = false;
                System.arraycopy(targetValues, 0, displayValues, 0, displayValues.length);
            } else {
                float t = easeOutCubic(animationProgress);
                for (int i = 0; i < displayValues.length; i++)
                    displayValues[i] = talentValues[i] + (targetValues[i] - talentValues[i]) * t;
            }
            needsAnimationUpdate = true;
        }

        boolean labelUpdated = false;
        for (int i = 0; i < talentNames.length; i++) {
            float alpha = labelAlpha.get(i);
            float desired = displayValues[i] > 0.01f ? 1f : 0f;
            float speed = config.labelFadeSpeed * dt;
            float newAlpha = MathHelper.clamp(alpha + (desired - alpha) * speed, 0f, 1f);
            if (Math.abs(newAlpha - alpha) > 0.001f) labelUpdated = true;
            labelAlpha.put(i, newAlpha);
        }
        if (labelUpdated) needsAnimationUpdate = true;
    }

    private void startValueAnimation() {
        System.arraycopy(displayValues, 0, talentValues, 0, displayValues.length);
        animationProgress = 0f;
        isAnimating = true;
        needsAnimationUpdate = true;
        clearCache();
    }

    /* ====================== 渲染入口 ====================== */

    public void render(DrawContext context, int mouseX, int mouseY) {
        updatePerformanceMetrics();
        if (needsAnimationUpdate || isAnimating) updateAnimations();

        drawGrid(context);
        drawTalentPolygonOptimized(context);   // 生成/更新浮点缓存 + 填充
        drawTalentPointsOptimized(context);    // 使用浮点缓存
        drawCombinedLabels(context);
    }

    /* ====================== 网格 ====================== */

    private void drawGrid(DrawContext context) {
        for (int i = 1; i <= config.gridCircles; i++) {
            int r = maxRadius * i / config.gridCircles;
            drawCircle(context, centerX, centerY, r, config.gridColor, segments);
        }
        int n = talentNames.length;
        for (int i = 0; i < n; i++) {
            // 修复：使用统一的角度计算，移除旋转偏移
            double angle = 2 * Math.PI * i / n;
            int ex = centerX + (int) (maxRadius * Math.cos(angle));
            int ey = centerY + (int) (maxRadius * Math.sin(angle));
            drawLine(context, centerX, centerY, ex, ey, config.gridColor);
        }
    }

    /* ====================== 浮点缓存 ====================== */

    private void clearCache() {
        cachedXPointsF = null;
        cachedYPointsF = null;
    }

    private void ensureCache() {
        long now = System.currentTimeMillis();
        if (cachedXPointsF != null && now - lastCacheTime <= CACHE_DURATION && !needsAnimationUpdate)
            return;

        int n = talentNames.length;
        cachedXPointsF = new float[n];
        cachedYPointsF = new float[n];

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            float curved = applyDisplayCurve(displayValues[i]);   // ← 就是这行
            float radius = maxRadius * curved;                    // ← 半径
            cachedXPointsF[i] = centerX + radius * (float) Math.cos(angle);
            cachedYPointsF[i] = centerY + radius * (float) Math.sin(angle);


        }
        lastCacheTime = now;
    }

    /* ====================== 多边形填充（浮点） ====================== */

    private void drawTalentPolygonOptimized(DrawContext context) {
        ensureCache();
        if (config.enablePolygonFill)
            fillPolygonWithScanlineFloat(context, cachedXPointsF, cachedYPointsF, config.fillColor);
    }

    private void fillPolygonWithScanlineFloat(DrawContext context,
                                              float[] xPts, float[] yPts, int color) {
        int n = xPts.length;
        if (n < 3) return;
        float minY = yPts[0], maxY = yPts[0];
        for (int i = 1; i < n; i++) {
            minY = Math.min(minY, yPts[i]);
            maxY = Math.max(maxY, yPts[i]);
        }
        for (int y = (int) Math.floor(minY); y <= (int) Math.ceil(maxY); y++) {
            List<Float> xs = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                float y1 = yPts[i], y2 = yPts[j];
                if ((y >= y1 && y <= y2) || (y >= y2 && y <= y1)) {
                    if (y1 == y2) continue;
                    float x1 = xPts[i], x2 = xPts[j];
                    float x = x1 + (x2 - x1) * (y - y1) / (y2 - y1);
                    xs.add(x);
                }
            }
            xs.sort(Float::compareTo);
            for (int i = 0; i < xs.size(); i += 2) {
                if (i + 1 < xs.size()) {
                    int x0 = (int) Math.floor(xs.get(i));
                    int x1 = (int) Math.ceil(xs.get(i + 1));
                    context.fill(x0, y, x1, y + 1, color);
                }
            }
        }
    }

    /* ====================== 数据点 ====================== */

    private void drawTalentPointsOptimized(DrawContext context) {
        ensureCache();
        int n = talentNames.length;
        for (int i = 0; i < n; i++) {
            if (displayValues[i] < 0.01f) continue;
            int x = (int) cachedXPointsF[i];
            int y = (int) cachedYPointsF[i];
            float scale = 0.5f + displayValues[i] * 0.5f;
            int size = Math.max(2, (int) (4 * scale));
            drawFilledCircle(context, x, y, size, talentColors[i]);
            if (size >= 3) {
                int h = Math.max(1, size / 2);
                context.fill(x - h + 1, y - h + 1, x - h + 2, y - h + 2, 0x80FFFFFF);
            }
        }
    }

    private void drawFilledCircle(DrawContext context, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++)
                if (dx * dx + dy * dy <= r * r)
                    context.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
    }

    /* ====================== 标签 ====================== */

    private void drawCombinedLabels(DrawContext context) {
        int n = talentNames.length;
        for (int i = 0; i < n; i++) {
            // 修复：使用统一的角度计算
            double angle = 2 * Math.PI * i / n;
            int labelDist = maxRadius + 25;
            int x = centerX + (int) (labelDist * Math.cos(angle));
            int y = centerY + (int) (labelDist * Math.sin(angle));
            String text = talentNames[i] + ": " + (int) (displayValues[i] * 100) + "%";
            int w = textRenderer.getWidth(text);
            int h = 8;
            x = MathHelper.clamp(x, leftBound + w / 2, rightBound - w / 2);
            y = MathHelper.clamp(y, topBound + h / 2, bottomBound - h / 2);
            float alpha = labelAlpha.get(i);
            if (alpha <= 0.01f) continue;
            int bg = withAlpha(0x80000000, alpha);
            int fg = withAlpha(config.labelColor, alpha);
            if (config.enableLabelBackground) {
                int padX = 2, padY = 1;
                context.fill(x - w / 2 - padX, y - h / 2 - padY,
                        x + w / 2 + padX, y + h / 2 + padY, bg);
            }
            context.drawText(textRenderer, text, x - w / 2, y - h / 2, fg, true);
        }
    }

    /* ====================== 工具 ====================== */

    private void updatePerformanceMetrics() {
        long now = System.currentTimeMillis();
        long dt = now - lastFrameTime;
        if (dt > 0) currentFPS = 1000f / dt;
        lastFrameTime = now;
    }

    private float easeOutCubic(float x) {
        return (float) (1 - Math.pow(1 - x, 3));
    }

    private int withAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        drawBresenhamLine(context, x1, y1, x2, y2, color);
    }

    private void drawBresenhamLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        int dy = Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            context.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }

    private void drawCircle(DrawContext context, int cx, int cy, int radius, int color, int segments) {
        for (int i = 0; i < segments; i++) {
            double a1 = 2 * Math.PI * i / segments;
            double a2 = 2 * Math.PI * (i + 1) / segments;
            int x1 = cx + (int) (radius * Math.cos(a1));
            int y1 = cy + (int) (radius * Math.sin(a1));
            int x2 = cx + (int) (radius * Math.cos(a2));
            int y2 = cy + (int) (radius * Math.sin(a2));
            drawLine(context, x1, y1, x2, y2, color);
        }
    }

    public float applyDisplayCurve(float raw) {
        return raw; // 临时绕过压缩
    }

    /* ====================== 配置 ====================== */

    public static class RadarChartConfig {
        public int gridColor = 0x6644AACC;
        public int fillColor = 0x553366AA;
        public int labelColor = 0xFFFFFFFF;
        public int valueColor = 0xFFFFFFAA;
        public int gridCircles = 3;
        public boolean enableLabelBackground = true;
        public float animationSpeed = 2.0f;
        public float labelFadeSpeed = 3.0f;
        public float gridRotationSpeed = 0.1f;
        public boolean enablePolygonFill = true;
    }
}
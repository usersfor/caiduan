package com.example.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * 完全优化版雷达图渲染器
 * 修复了数据点动画延迟问题并优化了性能
 */
public class RadarChartRenderer {

    private final TextRenderer textRenderer;
    private final String[] talentNames;
    private final int[] talentColors;
    private final float[] talentValues;
    private final float[] displayValues; // 用于动画的当前显示值
    private final float[] targetValues;  // 目标值

    /* 位置与尺寸 */
    private int centerX;
    private int centerY;
    private int maxRadius;
    private int segments = 32;

    /* 边界约束 */
    private int leftBound;
    private int rightBound;
    private int topBound;
    private int bottomBound;

    /* 显示层曲线参数 */
    private float displayScale = 1.0f;   // 整体缩放，0.5f 表示压到 50%
    private float softCap      = 1.0f;   // 软上限，超过部分开根号衰减

    /* 动画参数 */
    private long lastAnimationUpdateTime = System.currentTimeMillis();
    private float animationProgress = 0f; // 0-1 的动画进度
    private boolean isAnimating = false;
    private float gridRotation = 0f; // 网格旋转角度
    private final Map<Integer, Float> labelAlpha = new HashMap<>(); // 标签透明度动画
    private boolean needsAnimationUpdate = false;

    /* 性能优化缓存 */
    private int[] cachedXPoints = null;
    private int[] cachedYPoints = null;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 16; // 约60FPS的缓存时间

    /* 简化绘制标志 */
    private boolean simplifiedRendering = false;

    /* 性能监控 */
    private long lastFrameTime = System.currentTimeMillis();
    private float currentFPS = 60f;

    private final RadarChartConfig config;

    public RadarChartRenderer(TextRenderer textRenderer, String[] talentNames, int[] talentColors) {
        this(textRenderer, talentNames, talentColors, new RadarChartConfig());
    }

    public RadarChartRenderer(TextRenderer textRenderer,
                              String[] talentNames,
                              int[] talentColors,
                              RadarChartConfig config) {
        this.textRenderer  = textRenderer;
        this.talentNames   = talentNames;
        this.talentColors  = talentColors;
        this.talentValues  = new float[talentNames.length];
        this.displayValues = new float[talentNames.length];
        this.targetValues  = new float[talentNames.length];
        this.config        = config;

        // 初始化标签透明度
        for (int i = 0; i < talentNames.length; i++) {
            labelAlpha.put(i, 0f);
        }
    }

    /* ========== 外部调用 ========== */

    public void setPosition(int centerX, int centerY, int maxRadius) {
        this.centerX  = centerX;
        this.centerY  = centerY;
        this.maxRadius = maxRadius;
        // 清除缓存，因为位置改变了
        cachedXPoints = null;
        cachedYPoints = null;
    }

    public void setBounds(int left, int right, int top, int bottom) {
        this.leftBound  = left;
        this.rightBound = right;
        this.topBound   = top;
        this.bottomBound= bottom;
    }

    /**
     * 自适应不同屏幕尺寸
     */
    public void adjustLayout(int screenWidth, int screenHeight) {
        int chartSize = Math.min(screenWidth - 100, screenHeight - 150);
        this.maxRadius = chartSize / 2;
        this.centerX = screenWidth / 2;
        this.centerY = screenHeight / 2 - 20;

        // 动态调整边界
        setBounds(50, screenWidth - 50, 50, screenHeight - 100);

        // 清除缓存，因为尺寸改变了
        cachedXPoints = null;
        cachedYPoints = null;
    }

    public void setValues(float[] values) {
        if (values.length == talentValues.length) {
            System.arraycopy(values, 0, talentValues, 0, values.length);
            System.arraycopy(values, 0, targetValues, 0, values.length);

            // 开始数值动画并强制立即更新
            startValueAnimation();
            forceAnimationUpdate();
        }
    }

    /**
     * 设置数值并立即应用（无动画）
     */
    public void setValuesImmediate(float[] values) {
        if (values.length == talentValues.length) {
            System.arraycopy(values, 0, talentValues, 0, values.length);
            System.arraycopy(values, 0, displayValues, 0, values.length);
            System.arraycopy(values, 0, targetValues, 0, values.length);
            animationProgress = 1f;
            isAnimating = false;
            needsAnimationUpdate = true;

            // 清除缓存，因为数值改变了
            cachedXPoints = null;
            cachedYPoints = null;
        }
    }

    public void setSegments(int segments) {
        this.segments = Math.max(8, segments);
        // 清除缓存，因为分段数改变了
        cachedXPoints = null;
        cachedYPoints = null;
    }

    /**
     * 动态调整显示曲线，无需重建渲染器
     * @param scale   整体缩放 0.1~1.0
     * @param softCap 软上限  0.1~1.0
     */
    public void setDisplayParams(float scale, float softCap) {
        this.displayScale = MathHelper.clamp(scale,   0.1f, 1.0f);
        this.softCap      = MathHelper.clamp(softCap, 0.1f, 1.0f);
        // 清除缓存，因为显示参数改变了
        cachedXPoints = null;
        cachedYPoints = null;
    }

    /**
     * 设置简化渲染模式以提高性能
     */
    public void setSimplifiedRendering(boolean simplified) {
        this.simplifiedRendering = simplified;
        if (simplified) {
            // 简化模式下减少网格复杂度
            this.segments = Math.max(16, talentNames.length * 2);
        }
    }

    /**
     * 获取当前估计的FPS，可用于动态调整渲染质量
     */
    public float getCurrentFPS() {
        return currentFPS;
    }

    /**
     * 强制立即更新动画状态（用于确保数据点大小立即响应）
     */
    public void forceAnimationUpdate() {
        needsAnimationUpdate = true;
        updateAnimations();
    }

    /**
     * 更新动画状态，应该在每帧渲染前调用
     */
    public void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastAnimationUpdateTime) / 1000f; // 转换为秒

        // 限制deltaTime避免卡顿导致的超大时间步
        deltaTime = Math.min(deltaTime, 0.05f); // 最大50ms

        lastAnimationUpdateTime = currentTime;

        // 更新网格旋转动画
        gridRotation += deltaTime * config.gridRotationSpeed;
        if (gridRotation > Math.PI * 2) {
            gridRotation -= Math.PI * 2;
        }

        // 更新数值动画
        if (isAnimating) {
            animationProgress += deltaTime * config.animationSpeed;
            if (animationProgress >= 1f) {
                animationProgress = 1f;
                isAnimating = false;
                // 动画完成，确保显示值与目标值一致
                System.arraycopy(targetValues, 0, displayValues, 0, displayValues.length);
            } else {
                // 使用缓动函数使动画更自然
                float easedProgress = easeOutCubic(animationProgress);
                for (int i = 0; i < displayValues.length; i++) {
                    displayValues[i] = talentValues[i] + (targetValues[i] - talentValues[i]) * easedProgress;
                }
            }
            needsAnimationUpdate = true;
        }

        // 更新标签淡入动画
        boolean labelUpdated = false;
        for (int i = 0; i < talentNames.length; i++) {
            float currentAlpha = labelAlpha.get(i);
            if (displayValues[i] > 0.01f && currentAlpha < 1f) {
                float newAlpha = Math.min(1f, currentAlpha + deltaTime * config.labelFadeSpeed);
                labelAlpha.put(i, newAlpha);
                labelUpdated = true;
            } else if (displayValues[i] <= 0.01f && currentAlpha > 0f) {
                float newAlpha = Math.max(0f, currentAlpha - deltaTime * config.labelFadeSpeed);
                labelAlpha.put(i, newAlpha);
                labelUpdated = true;
            }
        }

        if (labelUpdated) {
            needsAnimationUpdate = true;
        }
    }

    private void updatePerformanceMetrics() {
        long currentTime = System.currentTimeMillis();
        long frameTime = currentTime - lastFrameTime;
        if (frameTime > 0) {
            currentFPS = 1000f / frameTime;

            // 根据FPS动态调整渲染质量
            if (currentFPS < 30f && !simplifiedRendering) {
                simplifiedRendering = true;
            } else if (currentFPS > 50f && simplifiedRendering) {
                simplifiedRendering = false;
            }
        }
        lastFrameTime = currentTime;
    }

    /* ========== 绘制入口 ========== */

    public void render(DrawContext context, int mouseX, int mouseY) {
        updatePerformanceMetrics();

        // 在渲染前确保动画状态是最新的
        if (needsAnimationUpdate || isAnimating) {
            updateAnimations();
            needsAnimationUpdate = false;
        }

        drawGrid(context);
        drawTalentPolygonOptimized(context);
        drawTalentPointsOptimized(context);
        drawCombinedLabels(context);
    }

    /* ========== 网格绘制 ========== */

    private void drawGrid(DrawContext context) {
        if (simplifiedRendering) {
            drawSimplifiedGrid(context);
        } else {
            drawFullGrid(context);
        }
    }

    private void drawSimplifiedGrid(DrawContext context) {
        // 简化网格：只绘制最外圈和必要的辐射线
        drawCircle(context, centerX, centerY, maxRadius, config.gridColor, segments);

        // 只绘制主要辐射线，跳过一些细节
        for (int i = 0; i < talentNames.length; i += 2) { // 每隔一个绘制一条线
            double angle = 2 * Math.PI * i / talentNames.length + gridRotation;
            int endX = centerX + (int)(maxRadius * Math.cos(angle));
            int endY = centerY + (int)(maxRadius * Math.sin(angle));
            drawLine(context, centerX, centerY, endX, endY, config.gridColor);
        }
    }

    private void drawFullGrid(DrawContext context) {
        // 绘制同心圆网格
        for (int i = 1; i <= config.gridCircles; i++) {
            int radius = maxRadius * i / config.gridCircles;
            drawCircle(context, centerX, centerY, radius, config.gridColor, segments);
        }

        // 绘制辐射线网格（带旋转动画）
        for (int i = 0; i < talentNames.length; i++) {
            double angle = 2 * Math.PI * i / talentNames.length + gridRotation;
            int endX = centerX + (int)(maxRadius * Math.cos(angle));
            int endY = centerY + (int)(maxRadius * Math.sin(angle));
            drawLine(context, centerX, centerY, endX, endY, config.gridColor);
        }
    }

    /* ========== 优化多边形绘制 ========== */

    private void drawTalentPolygonOptimized(DrawContext context) {
        int pointCount = talentNames.length;

        // 使用缓存避免重复计算
        long currentTime = System.currentTimeMillis();
        if (cachedXPoints == null || cachedYPoints == null ||
                currentTime - lastCacheTime > CACHE_DURATION ||
                needsAnimationUpdate) {

            cachedXPoints = new int[pointCount];
            cachedYPoints = new int[pointCount];

            for (int i = 0; i < pointCount; i++) {
                double angle = 2 * Math.PI * i / pointCount - Math.PI / 2;
                float v = applyDisplayCurve(displayValues[i]); // 使用动画中的当前值
                int r = (int)(maxRadius * v);
                cachedXPoints[i] = centerX + (int)(r * Math.cos(angle));
                cachedYPoints[i] = centerY + (int)(r * Math.sin(angle));
            }
            lastCacheTime = currentTime;
        }

        // 使用更高效的多边形填充方法
        if (simplifiedRendering) {
            fillPolygonOutline(context, cachedXPoints, cachedYPoints, config.fillColor);
        } else {
            fillPolygonOptimized(context, cachedXPoints, cachedYPoints, config.fillColor);
        }
    }

    /* ========== 优化的多边形填充方法 ========== */

    private void fillPolygonOutline(DrawContext context, int[] xPoints, int[] yPoints, int color) {
        // 简化模式：只绘制多边形轮廓而不是填充
        int n = xPoints.length;
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            drawLine(context, xPoints[i], yPoints[i], xPoints[next], yPoints[next], config.polygonColor);
        }

        // 绘制到中心的连线
        for (int i = 0; i < n; i++) {
            drawLine(context, centerX, centerY, xPoints[i], yPoints[i], config.polygonColor);
        }
    }

    private void fillPolygonOptimized(DrawContext context, int[] xPoints, int[] yPoints, int color) {
        int n = xPoints.length;

        // 使用更高效的三角形扇形填充
        for (int i = 1; i < n - 1; i++) {
            fillTriangleOptimized(context,
                    centerX, centerY,
                    xPoints[i], yPoints[i],
                    xPoints[i + 1], yPoints[i + 1],
                    color);
        }

        // 填充第一个和最后一个三角形
        if (n > 2) {
            fillTriangleOptimized(context,
                    centerX, centerY,
                    xPoints[0], yPoints[0],
                    xPoints[n - 1], yPoints[n - 1],
                    color);
        }
    }

    private void fillTriangleOptimized(DrawContext context, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        // 优化的三角形填充：使用更粗的线条近似填充
        int avgX = (x1 + x2 + x3) / 3;
        int avgY = (y1 + y2 + y3) / 3;

        // 绘制从中心到各顶点的粗线来近似填充
        drawThickLine(context, x1, y1, x2, y2, color, 2);
        drawThickLine(context, x2, y2, x3, y3, color, 2);
        drawThickLine(context, x3, y3, x1, y1, color, 2);

        // 填充中心区域
        context.fill(avgX - 1, avgY - 1, avgX + 1, avgY + 1, color);
    }

    private void drawThickLine(DrawContext context, int x1, int y1, int x2, int y2, int color, int thickness) {
        // 绘制粗线来近似填充
        for (int i = 0; i < thickness; i++) {
            for (int j = 0; j < thickness; j++) {
                drawLine(context, x1 + i, y1 + j, x2 + i, y2 + j, color);
            }
        }
    }

    /* ========== 优化数据点绘制 ========== */

    private void drawTalentPointsOptimized(DrawContext context) {
        for (int i = 0; i < talentNames.length; i++) {
            if (displayValues[i] < 0.01f) continue;

            // 使用缓存的位置数据
            int x = cachedXPoints[i];
            int y = cachedYPoints[i];

            // 修复：使用当前帧的displayValues而不是targetValues
            // 数据点大小根据当前动画值动态变化，确保与多边形动画同步
            float currentAnimatedValue = displayValues[i];
            float pointScale = 0.5f + currentAnimatedValue * 0.5f;
            int pointSize = Math.max(2, (int)(4 * pointScale)); // 最小2像素确保可见

            // 单次绘制而不是分两次
            int size = pointSize;
            context.fill(x - size, y - size, x + size + 1, y + size + 1, talentColors[i]);

            // 只在较大点时添加高光
            if (pointSize >= 4) {
                context.fill(x - size + 1, y - size + 1, x - size + 2, y - size + 2, 0x80FFFFFF);
            }
        }
    }

    /* ========== 合并的标签绘制 ========== */

    private void drawCombinedLabels(DrawContext context) {
        for (int i = 0; i < talentNames.length; i++) {
            double angle = 2 * Math.PI * i / talentNames.length - Math.PI / 2;

            // 标签放在雷达图外侧固定位置
            int labelDistance = maxRadius + 25;
            int labelX = centerX + (int)(labelDistance * Math.cos(angle));
            int labelY = centerY + (int)(labelDistance * Math.sin(angle));

            // 创建合并的标签文本：名称 + 数值
            String nameText = talentNames[i];
            String valueText = String.format("%.0f%%", displayValues[i] * 100);
            String combinedText = nameText + ": " + valueText;

            int textWidth = textRenderer.getWidth(combinedText);
            int textHeight = 8; // 单行文本高度

            // 调整标签位置，确保在边界内
            labelX = MathHelper.clamp(labelX, leftBound + textWidth / 2, rightBound - textWidth / 2);
            labelY = MathHelper.clamp(labelY, topBound + textHeight / 2, bottomBound - textHeight / 2);

            // 获取标签透明度
            float alpha = labelAlpha.get(i);
            if (alpha <= 0.01f) continue; // 完全透明时不绘制

            // 计算带透明度的颜色
            int backgroundColor = withAlpha(0x80000000, alpha);
            int textColor = withAlpha(config.labelColor, alpha);

            // 绘制标签背景
            if (config.enableLabelBackground) {
                int padX = 2;
                int padY = 1;
                context.fill(labelX - textWidth / 2 - padX, labelY - textHeight / 2 - padY,
                        labelX + textWidth / 2 + padX, labelY + textHeight / 2 + padY,
                        backgroundColor);
            }

            // 绘制合并的标签文本
            context.drawText(textRenderer, combinedText,
                    labelX - textWidth / 2, labelY - textHeight / 2,
                    textColor, true);
        }
    }

    /* ========== 动画控制 ========== */

    private void startValueAnimation() {
        // 保存当前值为起始值
        System.arraycopy(displayValues, 0, talentValues, 0, displayValues.length);
        animationProgress = 0f;
        isAnimating = true;
        needsAnimationUpdate = true; // 标记需要动画更新

        // 清除缓存，因为数值动画开始了
        cachedXPoints = null;
        cachedYPoints = null;
    }

    /* ========== 曲线核心 ========== */

    public float applyDisplayCurve(float raw) {
        raw = MathHelper.clamp(raw, 0f, 2f);
        if (raw <= softCap) return raw * displayScale;
        float excess = raw - softCap;
        return (softCap + (float)Math.sqrt(excess)) * displayScale;
    }
    /* ========== 工具函数 ========== */

    /**
     * 缓动函数 - 三次缓出
     */
    private float easeOutCubic(float x) {
        return (float)(1 - Math.pow(1 - x, 3));
    }

    /**
     * 为颜色添加透明度
     */
    private int withAlpha(int color, float alpha) {
        int a = (int)(((color >> 24) & 0xFF) * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /* ========== 基础绘图工具 ========== */

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
            if (e2 < dx)  { err += dx; y1 += sy; }
        }
    }

    private void drawCircle(DrawContext context, int cx, int cy, int radius, int color, int segments) {
        for (int i = 0; i < segments; i++) {
            double a1 = 2 * Math.PI * i / segments;
            double a2 = 2 * Math.PI * (i + 1) / segments;
            int x1 = cx + (int)(radius * Math.cos(a1));
            int y1 = cy + (int)(radius * Math.sin(a1));
            int x2 = cx + (int)(radius * Math.cos(a2));
            int y2 = cy + (int)(radius * Math.sin(a2));
            drawLine(context, x1, y1, x2, y2, color);
        }
    }

    /* ========== 配置 ========== */

    public static class RadarChartConfig {
        public int gridColor       = 0x6644AACC;
        public int polygonColor    = 0xAAFFFFFF;
        public int fillColor       = 0x333366AA;
        public int labelColor      = 0xFFFFFFFF;
        public int valueColor      = 0xFFFFFFAA;
        public int gridCircles     = 3;
        public boolean enableLabelBackground = true;

        // 动画参数
        public float animationSpeed = 2.0f;        // 数值动画速度
        public float labelFadeSpeed = 3.0f;        // 标签淡入淡出速度
        public float gridRotationSpeed = 0.1f;     // 网格旋转速度（弧度/秒）
    }
}
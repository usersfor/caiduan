package com.example.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class TetraUtil {

    // Tetra 武器属性权重配置（基于模块化特性）
    private static final float[][] WEAPON_WEIGHTS = {
            {0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.3f}, // 元素掌控权重
            {0.6f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f}, // 锋锐精通权重
            {0.0f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f}, // 流光身法权重
            {0.33f, 0.0f, 0.0f, 0.0f, 0.33f, 0.33f}, // 秘源流转权重
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}, // 不朽壁垒权重
            {0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.4f}  // 命运交织权重
    };

    /**
     * 检查是否为 Tetra 工具
     */
    public static boolean isTetraTool(NbtCompound nbt) {
        if (nbt == null) return false;

        // 检查 Tetra 特有的标识
        return nbt.contains("id") && nbt.getString("id").startsWith("tetra:") ||
                nbt.contains("sword/blade") || // 剑刃模块
                nbt.contains("sword/hilt") ||  // 剑柄模块
                nbt.contains("sword/guard") || // 护手模块
                nbt.contains("sword/pommel") || // 配重球模块
                nbt.contains("honing_progress"); // 打磨进度
    }

    /**
     * 计算 Tetra 工具的天赋值
     */
    public static float[] calculateTetraTalentValues(NbtCompound nbt) {
        if (!isTetraTool(nbt)) {
            return new float[6]; // 返回空数组
        }

        return calculateTetraWeaponTalentValues(nbt);
    }

    /**
     * 计算 Tetra 武器的天赋值（基于模块化系统）
     */
    private static float[] calculateTetraWeaponTalentValues(NbtCompound nbt) {
        float[] baseValues = new float[6];
        float[] compositeValues = new float[6];

        // 分析模块质量
        ModuleQuality moduleQuality = analyzeModuleQuality(nbt);

        // 元素掌控 (索引0) - 模块特殊效果和精准度
        baseValues[0] = calculateElementalMastery(moduleQuality);

        // 锋锐精通 (索引1) - 攻击伤害相关
        baseValues[1] = calculateSharpnessMastery(moduleQuality);

        // 流光身法 (索引2) - 攻击速度和移动性
        baseValues[2] = calculateAgilityMastery(moduleQuality);

        // 秘源流转 (索引3) - 使用效率和能量流动
        baseValues[3] = calculateEfficiencyMastery(moduleQuality, nbt);

        // 不朽壁垒 (索引4) - 耐久度和防御
        baseValues[4] = calculateDurabilityMastery(moduleQuality, nbt);

        // 命运交织 (索引5) - 综合稀有度和模块协调
        baseValues[5] = calculateFateIntertwined(moduleQuality, nbt);

        // 使用武器权重计算复合属性
        compositeValues = calculateCompositeValues(baseValues, WEAPON_WEIGHTS);

        return compositeValues;
    }

    /**
     * 分析模块质量
     */
    private static ModuleQuality analyzeModuleQuality(NbtCompound nbt) {
        ModuleQuality quality = new ModuleQuality();

        // 分析各个模块的材料等级
        quality.bladeQuality = getMaterialQuality(getModuleMaterial(nbt, "sword/blade"));
        quality.hiltQuality = getMaterialQuality(getModuleMaterial(nbt, "sword/hilt"));
        quality.guardQuality = getMaterialQuality(getModuleMaterial(nbt, "sword/guard"));
        quality.pommelQuality = getMaterialQuality(getModuleMaterial(nbt, "sword/pommel"));

        // 检查特殊模块
        quality.hasSpecialModules = checkSpecialModules(nbt);
        quality.honingProgress = nbt.contains("honing_progress") ? nbt.getInt("honing_progress") : 0;

        return quality;
    }

    /**
     * 获取模块材料
     */
    private static String getModuleMaterial(NbtCompound nbt, String moduleKey) {
        String materialKey = moduleKey + "_material";
        if (nbt.contains(materialKey)) {
            return nbt.getString(materialKey);
        }
        return "unknown";
    }

    /**
     * 材料质量评估
     */
    private static float getMaterialQuality(String material) {
        if (material.contains("netherite")) return 1.0f;
        if (material.contains("diamond")) return 0.8f;
        if (material.contains("arcane") || material.contains("lightning")) return 0.9f;
        if (material.contains("iron")) return 0.6f;
        if (material.contains("stone")) return 0.4f;
        if (material.contains("wood")) return 0.2f;
        if (material.contains("gold")) return 0.3f;
        return 0.1f;
    }

    /**
     * 检查特殊模块
     */
    private static boolean checkSpecialModules(NbtCompound nbt) {
        // 检查是否有特殊命名的模块（如 more_mod_tetra 等）
        for (String key : nbt.getKeys()) {
            if (key.contains("more_mod_tetra") || key.contains("mmt_")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算元素掌控天赋
     */
    private static float calculateElementalMastery(ModuleQuality quality) {
        float score = 0f;

        // 特殊模块加分
        if (quality.hasSpecialModules) {
            score += 0.4f;
        }

        // 刀刃质量影响精准度
        score += quality.bladeQuality * 0.3f;

        // 打磨进度影响元素亲和
        score += Math.min(quality.honingProgress / 500.0f, 0.3f);

        return Math.min(score, 1.0f);
    }

    /**
     * 计算锋锐精通天赋
     */
    private static float calculateSharpnessMastery(ModuleQuality quality) {
        // 主要受刀刃质量影响
        float score = quality.bladeQuality * 0.7f;

        // 护手质量也有一定影响
        score += quality.guardQuality * 0.2f;

        // 特殊模块提供额外锋锐
        if (quality.hasSpecialModules) {
            score += 0.2f;
        }

        return Math.min(score, 1.0f);
    }

    /**
     * 计算流光身法天赋
     */
    private static float calculateAgilityMastery(ModuleQuality quality) {
        float score = 0f;

        // 刀柄质量主要影响攻击速度
        score += quality.hiltQuality * 0.4f;

        // 配重球质量影响平衡性
        score += quality.pommelQuality * 0.3f;

        // 模块协调性影响流畅度
        float coordination = calculateModuleCoordination(quality);
        score += coordination * 0.3f;

        return Math.min(score, 1.0f);
    }

    /**
     * 计算秘源流转天赋
     */
    private static float calculateEfficiencyMastery(ModuleQuality quality, NbtCompound nbt) {
        float score = 0f;

        // 所有模块的平均质量影响效率
        float avgQuality = (quality.bladeQuality + quality.hiltQuality +
                quality.guardQuality + quality.pommelQuality) / 4.0f;
        score += avgQuality * 0.5f;

        // 打磨进度反映使用流畅度
        score += Math.min(quality.honingProgress / 400.0f, 0.3f);

        // 特殊模块提供能量效率
        if (quality.hasSpecialModules) {
            score += 0.2f;
        }

        return Math.min(score, 1.0f);
    }

    /**
     * 计算不朽壁垒天赋
     */
    private static float calculateDurabilityMastery(ModuleQuality quality, NbtCompound nbt) {
        float score = 0f;

        // 护手和配重球质量影响防御
        score += quality.guardQuality * 0.4f;
        score += quality.pommelQuality * 0.3f;

        // 模块协调性影响整体坚固度
        float coordination = calculateModuleCoordination(quality);
        score += coordination * 0.3f;

        return Math.min(score, 1.0f);
    }

    /**
     * 计算命运交织天赋
     */
    private static float calculateFateIntertwined(ModuleQuality quality, NbtCompound nbt) {
        float score = 0f;

        // 模块质量均衡性
        float minQuality = Math.min(Math.min(quality.bladeQuality, quality.hiltQuality),
                Math.min(quality.guardQuality, quality.pommelQuality));
        float maxQuality = Math.max(Math.max(quality.bladeQuality, quality.hiltQuality),
                Math.max(quality.guardQuality, quality.pommelQuality));
        float balance = 1.0f - (maxQuality - minQuality);
        score += balance * 0.4f;

        // 特殊模块稀有度
        if (quality.hasSpecialModules) {
            score += 0.3f;
        }

        // 高打磨进度代表精心制作
        score += Math.min(quality.honingProgress / 600.0f, 0.3f);

        return Math.min(score, 1.0f);
    }

    /**
     * 计算模块协调性（质量差异越小，协调性越好）
     */
    private static float calculateModuleCoordination(ModuleQuality quality) {
        float[] qualities = {quality.bladeQuality, quality.hiltQuality,
                quality.guardQuality, quality.pommelQuality};

        float sum = 0f;
        for (float q : qualities) {
            sum += q;
        }
        float average = sum / qualities.length;

        float variance = 0f;
        for (float q : qualities) {
            variance += Math.pow(q - average, 2);
        }
        variance /= qualities.length;

        // 方差越小，协调性越好
        return Math.max(0f, 1.0f - variance * 5f);
    }

    /**
     * 计算复合属性值（与TicUtil相同）
     */
    private static float[] calculateCompositeValues(float[] baseValues, float[][] weights) {
        float[] composite = new float[6];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                composite[i] += baseValues[j] * weights[i][j];
            }
            composite[i] = Math.min(Math.max(composite[i], 0f), 1f);
        }
        return composite;
    }

    /**
     * 模块质量内部类
     */
    private static class ModuleQuality {
        public float bladeQuality = 0f;    // 刀刃质量
        public float hiltQuality = 0f;     // 刀柄质量
        public float guardQuality = 0f;    // 护手质量
        public float pommelQuality = 0f;   // 配重球质量
        public boolean hasSpecialModules = false; // 是否有特殊模块
        public int honingProgress = 0;     // 打磨进度
    }
}
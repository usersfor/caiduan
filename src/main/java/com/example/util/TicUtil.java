package com.example.util;

import net.minecraft.nbt.NbtCompound;

public class TicUtil {

    // TiC 工具属性权重配置（更新为新的复合元素顺序）
    private static final float[][] WEAPON_WEIGHTS = {
            {0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.3f}, // 土权重
            {0.6f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f}, // 金权重
            {0.0f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f}, // 木权重
            {0.33f, 0.0f, 0.0f, 0.0f, 0.33f, 0.33f}, // 火权重
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}, // 水权重
            {0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.4f}  // 星权重
    };

    // TiC 护甲属性权重配置（更新为新的复合元素顺序）
    private static final float[][] ARMOR_WEIGHTS = {
            {0.3f, 0.2f, 0.0f, 0.0f, 0.1f, 0.2f}, // 土权重 - 精准度、投射物伤害
            {0.2f, 0.4f, 0.0f, 0.0f, 0.1f, 0.1f}, // 金权重 - 攻击伤害、投射物伤害
            {0.0f, 0.0f, 0.5f, 0.2f, 0.1f, 0.1f}, // 木权重 - 速度、攻击速度
            {0.1f, 0.0f, 0.2f, 0.4f, 0.1f, 0.1f}, // 火权重 - 使用速度、挖掘速度
            {0.1f, 0.1f, 0.0f, 0.0f, 0.6f, 0.2f}, // 水权重 - 护甲、韧性、格挡、耐久
            {0.3f, 0.3f, 0.3f, 0.4f, 0.1f, 0.4f}  // 星权重 - 综合稀有度
    };

    /**
     * 检查是否为 TiC 工具或护甲
     */
    public static boolean isTicTool(NbtCompound nbt) {
        return nbt != null && nbt.contains("tic_stats");
    }

    /**
     * 判断是否为护甲（根据是否有护甲相关属性）
     */
    private static boolean isTicArmor(NbtCompound ticStats) {
        return ticStats.contains("tconstruct:armor") ||
                ticStats.contains("tconstruct:armor_toughness") ||
                ticStats.contains("tconstruct:knockback_resistance");
    }

    /**
     * 计算 TiC 工具/护甲的天赋值（新增玩家属性参数）
     */
    public static float[] calculateTicTalentValues(NbtCompound nbt, float[] playerAttributes) {
        NbtCompound ticStats = nbt.getCompound("tic_stats");

        if (isTicArmor(ticStats)) {
            // 处理护甲属性（支持武器转换的特殊护甲）
            return calculateArmorTalentValues(ticStats, playerAttributes);
        } else {
            // 处理工具属性
            return calculateToolTalentValues(ticStats, playerAttributes);
        }
    }

    /**
     * 计算 TiC 护甲的天赋值（支持武器转换的特殊护甲，新增玩家属性）
     */
    private static float[] calculateArmorTalentValues(NbtCompound ticStats, float[] playerAttributes) {
        float[] baseValues = new float[6];

        // 土 (索引0) - 精准度、特殊效果
        if (ticStats.contains("tconstruct:accuracy")) {
            float accuracy = ticStats.getFloat("tconstruct:accuracy");
            baseValues[0] += Math.min(accuracy / 3.0f, 0.4f);
        }
        if (ticStats.contains("tconstruct:projectile_damage")) {
            float projectileDamage = ticStats.getFloat("tconstruct:projectile_damage");
            baseValues[0] += Math.min(projectileDamage / 8.0f, 0.3f);
        }
        // TiC 扩展属性：保护帽、负面效果持续时间
        if (ticStats.contains("tconstruct:generic.protection_cap")) {
            float protectionCap = ticStats.getFloat("tconstruct:generic.protection_cap");
            baseValues[0] += Math.min(protectionCap / 25.0f, 0.2f);
        }
        if (ticStats.contains("tconstruct:generic.bad_effect_duration_multiplier")) {
            float badEffectMultiplier = ticStats.getFloat("tconstruct:generic.bad_effect_duration_multiplier");
            baseValues[0] += Math.min((1.0f - badEffectMultiplier) * 2.0f, 0.2f); // 值越小越好
        }
        baseValues[0] = Math.min(baseValues[0], 1.0f);

        // 金 (索引1) - 攻击相关属性
        if (ticStats.contains("tconstruct:projectile_damage")) {
            float projectileDamage = ticStats.getFloat("tconstruct:projectile_damage");
            baseValues[1] += Math.min(projectileDamage / 10.0f, 0.5f);
        }
        // 如果有攻击伤害属性也加入
        if (ticStats.contains("tconstruct:attack_damage")) {
            float attackDamage = ticStats.getFloat("tconstruct:attack_damage");
            baseValues[1] += Math.min(attackDamage / 25.0f, 0.5f);
        }
        // TiC 扩展属性：暴击伤害、潜行伤害、击退倍数
        if (ticStats.contains("tconstruct:player.critical_damage")) {
            float criticalDamage = ticStats.getFloat("tconstruct:player.critical_damage");
            baseValues[1] += Math.min(criticalDamage / 3.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:generic.crouch_damage_multiplier")) {
            float crouchDamage = ticStats.getFloat("tconstruct:generic.crouch_damage_multiplier");
            baseValues[1] += Math.min(crouchDamage / 2.0f, 0.2f);
        }
        if (ticStats.contains("tconstruct:generic.knockback_multiplier")) {
            float knockbackMultiplier = ticStats.getFloat("tconstruct:generic.knockback_multiplier");
            baseValues[1] += Math.min(knockbackMultiplier / 3.0f, 0.2f);
        }
        baseValues[1] = Math.min(baseValues[1], 1.0f);

        // 木 (索引2) - 速度相关
        float speedValue = 0f;
        if (ticStats.contains("tconstruct:attack_speed")) {
            speedValue += ticStats.getFloat("tconstruct:attack_speed") / 3.0f;
        }
        if (ticStats.contains("tconstruct:velocity")) {
            speedValue += ticStats.getFloat("tconstruct:velocity") / 2.5f;
        }
        // TiC 扩展属性：跳跃相关、使用物品速度
        if (ticStats.contains("tconstruct:player.jump_count")) {
            float jumpCount = ticStats.getFloat("tconstruct:player.jump_count");
            speedValue += Math.min(jumpCount / 3.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:generic.jump_boost")) {
            float jumpBoost = ticStats.getFloat("tconstruct:generic.jump_boost");
            speedValue += Math.min(jumpBoost / 2.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:player.use_item_speed")) {
            float useItemSpeed = ticStats.getFloat("tconstruct:player.use_item_speed");
            speedValue += Math.min(useItemSpeed / 2.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:generic.bouncy")) {
            float bouncy = ticStats.getFloat("tconstruct:generic.bouncy");
            speedValue += Math.min(bouncy / 1.5f, 0.2f);
        }
        baseValues[2] = Math.min(speedValue / 2.0f, 1.0f);

        // 火 (索引3) - 使用速度、能量流动
        if (ticStats.contains("tconstruct:draw_speed")) {
            float drawSpeed = ticStats.getFloat("tconstruct:draw_speed");
            baseValues[3] += Math.min(drawSpeed / 2.5f, 0.4f);
        }
        if (ticStats.contains("tconstruct:mining_speed")) {
            float miningSpeed = ticStats.getFloat("tconstruct:mining_speed");
            baseValues[3] += Math.min(miningSpeed / 8.0f, 0.4f);
        }
        // TiC 扩展属性：挖掘速度倍数、正面效果持续时间
        if (ticStats.contains("tconstruct:player.mining_speed_multiplier")) {
            float miningMultiplier = ticStats.getFloat("tconstruct:player.mining_speed_multiplier");
            baseValues[3] += Math.min(miningMultiplier / 3.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:generic.good_effect_duration_multiplier")) {
            float goodEffectMultiplier = ticStats.getFloat("tconstruct:generic.good_effect_duration_multiplier");
            baseValues[3] += Math.min(goodEffectMultiplier / 2.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:player.experience_multiplier")) {
            float experienceMultiplier = ticStats.getFloat("tconstruct:player.experience_multiplier");
            baseValues[3] += Math.min(experienceMultiplier / 2.0f, 0.3f);
        }
        baseValues[3] = Math.min(baseValues[3], 1.0f);

        // 水 (索引4) - 防御属性
        if (ticStats.contains("tconstruct:armor")) {
            float armor = ticStats.getFloat("tconstruct:armor");
            baseValues[4] += Math.min(armor / 25.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:armor_toughness")) {
            float toughness = ticStats.getFloat("tconstruct:armor_toughness");
            baseValues[4] += Math.min(toughness / 20.0f, 0.2f);
        }
        if (ticStats.contains("tconstruct:knockback_resistance")) {
            float knockbackResistance = ticStats.getFloat("tconstruct:knockback_resistance");
            baseValues[4] += Math.min(knockbackResistance / 1.0f, 0.2f);
        }
        if (ticStats.contains("tconstruct:block_angle")) {
            float blockAngle = ticStats.getFloat("tconstruct:block_angle");
            baseValues[4] += Math.min(blockAngle / 360.0f, 0.2f);
        }
        if (ticStats.contains("tconstruct:block_amount")) {
            float blockAmount = ticStats.getFloat("tconstruct:block_amount");
            baseValues[4] += Math.min(blockAmount / 15.0f, 0.1f);
        }
        if (ticStats.contains("tconstruct:durability")) {
            float durability = ticStats.getFloat("tconstruct:durability");
            baseValues[4] += Math.min(durability / 8000.0f, 0.2f);
        }
        // TiC 扩展属性：安全坠落距离
        if (ticStats.contains("tconstruct:generic.safe_fall_distance")) {
            float safeFallDistance = ticStats.getFloat("tconstruct:generic.safe_fall_distance");
            baseValues[4] += Math.min(safeFallDistance / 10.0f, 0.2f);
        }
        baseValues[4] = Math.min(baseValues[4], 1.0f);

        // 星 (索引5) - 综合稀有度
        int attributeCount = 0;
        float fateScore = 0f;

        // 统计所有属性的综合评分（包括扩展属性）
        String[] attributes = {
                "tconstruct:armor", "tconstruct:armor_toughness", "tconstruct:knockback_resistance",
                "tconstruct:block_angle", "tconstruct:block_amount", "tconstruct:durability",
                "tconstruct:projectile_damage", "tconstruct:draw_speed", "tconstruct:mining_speed",
                "tconstruct:accuracy", "tconstruct:attack_speed", "tconstruct:velocity",
                // TiC 扩展属性
                "tconstruct:player.jump_count", "tconstruct:player.mining_speed_multiplier",
                "tconstruct:player.use_item_speed", "tconstruct:generic.bad_effect_duration_multiplier",
                "tconstruct:generic.bouncy", "tconstruct:generic.crouch_damage_multiplier",
                "tconstruct:generic.good_effect_duration_multiplier", "tconstruct:generic.jump_boost",
                "tconstruct:generic.knockback_multiplier", "tconstruct:generic.protection_cap",
                "tconstruct:generic.safe_fall_distance", "tconstruct:player.critical_damage",
                "tconstruct:player.experience_multiplier"
        };

        for (String attr : attributes) {
            if (ticStats.contains(attr)) {
                float value = ticStats.getFloat(attr);
                // 简单归一化
                fateScore += Math.min(value / getAttributeMax(attr), 1.0f);
                attributeCount++;
            }
        }

        baseValues[5] = attributeCount > 0 ? Math.min(fateScore / attributeCount, 1.0f) : 0f;

        // 使用更新后的护甲权重计算复合属性，并加入玩家属性
        return calculateCompositeValuesWithPlayerAttributes(baseValues, playerAttributes, ARMOR_WEIGHTS, true);
    }

    /**
     * 计算 TiC 工具的天赋值（新增玩家属性参数）
     */
    private static float[] calculateToolTalentValues(NbtCompound ticStats, float[] playerAttributes) {
        float[] baseValues = new float[6];

        // 土 (索引0) - 精准度、特殊效果相关
        if (ticStats.contains("tconstruct:accuracy")) {
            float accuracy = ticStats.getFloat("tconstruct:accuracy");
            baseValues[0] += Math.min(accuracy / 2.0f, 0.5f);
        }
        if (ticStats.contains("tconstruct:harvest_tier")) {
            String harvestTier = ticStats.getString("tconstruct:harvest_tier");
            baseValues[0] += getHarvestTierValue(harvestTier) * 0.5f;
        }
        // TiC 扩展属性：保护帽、负面效果持续时间
        if (ticStats.contains("tconstruct:generic.protection_cap")) {
            float protectionCap = ticStats.getFloat("tconstruct:generic.protection_cap");
            baseValues[0] += Math.min(protectionCap / 25.0f, 0.2f);
        }
        if (ticStats.contains("tconstruct:generic.bad_effect_duration_multiplier")) {
            float badEffectMultiplier = ticStats.getFloat("tconstruct:generic.bad_effect_duration_multiplier");
            baseValues[0] += Math.min((1.0f - badEffectMultiplier) * 2.0f, 0.2f); // 值越小越好
        }
        baseValues[0] = Math.min(baseValues[0], 1.0f);

        // 金 (索引1) - 攻击伤害相关
        if (ticStats.contains("tconstruct:attack_damage")) {
            float attackDamage = ticStats.getFloat("tconstruct:attack_damage");
            baseValues[1] = Math.min(attackDamage / 25.0f, 1.0f);
        }
        // TiC 扩展属性：暴击伤害、潜行伤害、击退倍数
        if (ticStats.contains("tconstruct:player.critical_damage")) {
            float criticalDamage = ticStats.getFloat("tconstruct:player.critical_damage");
            baseValues[1] += Math.min(criticalDamage / 3.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:generic.crouch_damage_multiplier")) {
            float crouchDamage = ticStats.getFloat("tconstruct:generic.crouch_damage_multiplier");
            baseValues[1] += Math.min(crouchDamage / 2.0f, 0.2f);
        }
        if (ticStats.contains("tconstruct:generic.knockback_multiplier")) {
            float knockbackMultiplier = ticStats.getFloat("tconstruct:generic.knockback_multiplier");
            baseValues[1] += Math.min(knockbackMultiplier / 3.0f, 0.2f);
        }
        baseValues[1] = Math.min(baseValues[1], 1.0f);

        // 木 (索引2) - 攻击速度、移动速度相关
        float speedValue = 0f;
        if (ticStats.contains("tconstruct:attack_speed")) {
            speedValue += ticStats.getFloat("tconstruct:attack_speed") / 4.0f;
        }
        if (ticStats.contains("tconstruct:velocity")) {
            speedValue += ticStats.getFloat("tconstruct:velocity") / 3.0f;
        }
        // TiC 扩展属性：跳跃相关、使用物品速度
        if (ticStats.contains("tconstruct:player.jump_count")) {
            float jumpCount = ticStats.getFloat("tconstruct:player.jump_count");
            speedValue += Math.min(jumpCount / 3.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:generic.jump_boost")) {
            float jumpBoost = ticStats.getFloat("tconstruct:generic.jump_boost");
            speedValue += Math.min(jumpBoost / 2.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:player.use_item_speed")) {
            float useItemSpeed = ticStats.getFloat("tconstruct:player.use_item_speed");
            speedValue += Math.min(useItemSpeed / 2.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:generic.bouncy")) {
            float bouncy = ticStats.getFloat("tconstruct:generic.bouncy");
            speedValue += Math.min(bouncy / 1.5f, 0.2f);
        }
        baseValues[2] = Math.min(speedValue / 2.0f, 1.0f);

        // 火 (索引3) - 使用速度、能量流动相关
        if (ticStats.contains("tconstruct:draw_speed")) {
            float drawSpeed = ticStats.getFloat("tconstruct:draw_speed");
            baseValues[3] += Math.min(drawSpeed / 3.0f, 0.5f);
        }
        if (ticStats.contains("tconstruct:mining_speed")) {
            float miningSpeed = ticStats.getFloat("tconstruct:mining_speed");
            baseValues[3] += Math.min(miningSpeed / 10.0f, 0.5f);
        }
        // TiC 扩展属性：挖掘速度倍数、正面效果持续时间
        if (ticStats.contains("tconstruct:player.mining_speed_multiplier")) {
            float miningMultiplier = ticStats.getFloat("tconstruct:player.mining_speed_multiplier");
            baseValues[3] += Math.min(miningMultiplier / 3.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:generic.good_effect_duration_multiplier")) {
            float goodEffectMultiplier = ticStats.getFloat("tconstruct:generic.good_effect_duration_multiplier");
            baseValues[3] += Math.min(goodEffectMultiplier / 2.0f, 0.3f);
        }
        if (ticStats.contains("tconstruct:player.experience_multiplier")) {
            float experienceMultiplier = ticStats.getFloat("tconstruct:player.experience_multiplier");
            baseValues[3] += Math.min(experienceMultiplier / 2.0f, 0.3f);
        }
        baseValues[3] = Math.min(baseValues[3], 1.0f);

        // 水 (索引4) - 耐久度、防御相关
        if (ticStats.contains("tconstruct:durability")) {
            float durability = ticStats.getFloat("tconstruct:durability");
            baseValues[4] = Math.min(durability / 10000.0f, 1.0f);
        }
        // TiC 扩展属性：安全坠落距离
        if (ticStats.contains("tconstruct:generic.safe_fall_distance")) {
            float safeFallDistance = ticStats.getFloat("tconstruct:generic.safe_fall_distance");
            baseValues[4] += Math.min(safeFallDistance / 10.0f, 0.3f);
        }
        baseValues[4] = Math.min(baseValues[4], 1.0f);

        // 星 (索引5) - 综合属性、稀有度相关
        if (ticStats.contains("tconstruct:harvest_tier")) {
            String harvestTier = ticStats.getString("tconstruct:harvest_tier");
            baseValues[5] = getHarvestTierValue(harvestTier) * 0.7f;
        }
        if (ticStats.contains("tconstruct:durability")) {
            float durability = ticStats.getFloat("tconstruct:durability");
            baseValues[5] += Math.min(durability / 50000.0f, 0.3f);
        }
        // TiC 扩展属性：统计扩展属性的综合评分
        int extendedAttrCount = 0;
        float extendedScore = 0f;
        String[] extendedAttrs = {
                "tconstruct:player.jump_count", "tconstruct:player.mining_speed_multiplier",
                "tconstruct:player.use_item_speed", "tconstruct:generic.bad_effect_duration_multiplier",
                "tconstruct:generic.bouncy", "tconstruct:generic.crouch_damage_multiplier",
                "tconstruct:generic.good_effect_duration_multiplier", "tconstruct:generic.jump_boost",
                "tconstruct:generic.knockback_multiplier", "tconstruct:generic.protection_cap",
                "tconstruct:generic.safe_fall_distance", "tconstruct:player.critical_damage",
                "tconstruct:player.experience_multiplier"
        };

        for (String attr : extendedAttrs) {
            if (ticStats.contains(attr)) {
                float value = ticStats.getFloat(attr);
                extendedScore += Math.min(value / getAttributeMax(attr), 1.0f);
                extendedAttrCount++;
            }
        }

        if (extendedAttrCount > 0) {
            baseValues[5] += Math.min(extendedScore / extendedAttrCount * 0.5f, 0.5f);
        }

        baseValues[5] = Math.min(baseValues[5], 1.0f);

        // 使用武器权重计算复合属性，并加入玩家属性
        return calculateCompositeValuesWithPlayerAttributes(baseValues, playerAttributes, WEAPON_WEIGHTS, false);
    }

    /**
     * 结合玩家属性的复合属性计算
     */
    private static float[] calculateCompositeValuesWithPlayerAttributes(float[] baseValues, float[] playerAttributes, float[][] weights, boolean isArmor) {
        float[] composite = new float[6];

        for (int i = 0; i < 6; i++) {
            float equipmentContribution = 0f;

            // 计算装备贡献
            for (int j = 0; j < 6; j++) {
                equipmentContribution += baseValues[j] * weights[i][j];
            }

            // 获取对应的玩家属性贡献
            float playerContribution = playerAttributes[i];

            // 根据复合元素类型调整权重
            float equipmentWeight = getTicEquipmentWeight(i, isArmor);
            float playerWeight = 1f - equipmentWeight;

            // 合并贡献
            composite[i] = equipmentContribution * equipmentWeight + playerContribution * playerWeight;
            composite[i] = Math.min(Math.max(composite[i], 0f), 1f);
        }

        return composite;
    }

    /**
     * 获取TiC装备权重（根据复合元素类型和物品类型）
     */
    private static float getTicEquipmentWeight(int talentIndex, boolean isArmor) {
        if (isArmor) {
            // 护甲权重
            switch (talentIndex) {
                case 0: // 土 - 护甲主导
                    return 0.7f;
                case 1: // 金 - 玩家主导（护甲不直接贡献金）
                    return 0.3f;
                case 2: // 木 - 平衡
                    return 0.5f;
                case 3: // 火 - 玩家主导
                    return 0.3f;
                case 4: // 水 - 装备主导
                    return 0.7f;
                case 5: // 星 - 玩家主导
                    return 0.2f;
                default:
                    return 0.5f;
            }
        } else {
            // 武器权重
            switch (talentIndex) {
                case 0: // 土 - 玩家主导（武器不直接贡献土）
                    return 0.3f;
                case 1: // 金 - 装备主导
                    return 0.7f;
                case 2: // 木 - 平衡
                    return 0.5f;
                case 3: // 火 - 玩家主导
                    return 0.3f;
                case 4: // 水 - 装备主导
                    return 0.7f;
                case 5: // 星 - 玩家主导
                    return 0.2f;
                default:
                    return 0.5f;
            }
        }
    }

    /**
     * 获取属性最大值用于归一化（新增TiC扩展属性）
     */
    private static float getAttributeMax(String attribute) {
        switch (attribute) {
            case "tconstruct:armor": return 30.0f;
            case "tconstruct:armor_toughness": return 20.0f;
            case "tconstruct:knockback_resistance": return 1.0f;
            case "tconstruct:block_angle": return 360.0f;
            case "tconstruct:block_amount": return 15.0f;
            case "tconstruct:durability": return 10000.0f;
            case "tconstruct:projectile_damage": return 15.0f;
            case "tconstruct:draw_speed": return 5.0f;
            case "tconstruct:mining_speed": return 20.0f;
            case "tconstruct:accuracy": return 5.0f;
            case "tconstruct:attack_speed": return 5.0f;
            case "tconstruct:velocity": return 5.0f;
            // TiC 扩展属性最大值
            case "tconstruct:player.jump_count": return 3.0f;
            case "tconstruct:player.mining_speed_multiplier": return 3.0f;
            case "tconstruct:player.use_item_speed": return 2.0f;
            case "tconstruct:generic.bad_effect_duration_multiplier": return 1.0f;
            case "tconstruct:generic.bouncy": return 1.5f;
            case "tconstruct:generic.crouch_damage_multiplier": return 2.0f;
            case "tconstruct:generic.good_effect_duration_multiplier": return 2.0f;
            case "tconstruct:generic.jump_boost": return 2.0f;
            case "tconstruct:generic.knockback_multiplier": return 3.0f;
            case "tconstruct:generic.protection_cap": return 25.0f;
            case "tconstruct:generic.safe_fall_distance": return 10.0f;
            case "tconstruct:player.critical_damage": return 3.0f;
            case "tconstruct:player.experience_multiplier": return 2.0f;
            default: return 10.0f;
        }
    }

    /**
     * 采集等级数值映射
     */
    private static float getHarvestTierValue(String harvestTier) {
        switch (harvestTier) {
            case "minecraft:netherite": return 1.0f;
            case "minecraft:diamond": return 0.8f;
            case "minecraft:iron": return 0.6f;
            case "minecraft:stone": return 0.4f;
            case "minecraft:wood": return 0.2f;
            case "minecraft:gold": return 0.3f;
            default: return 0.1f;
        }
    }

    /**
     * 保持向后兼容的方法（不包含玩家属性）
     */
    public static float[] calculateTicTalentValues(NbtCompound nbt) {
        // 如果没有提供玩家属性，使用默认值（全为0）
        float[] defaultPlayerAttributes = new float[6];
        return calculateTicTalentValues(nbt, defaultPlayerAttributes);
    }
}
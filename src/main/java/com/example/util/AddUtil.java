package com.example.util;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random; // 使用 Minecraft 的 Random

import java.util.UUID;

public final class AddUtil {

    // 定义与 ForgingScreenHandler 中相同的 UUID 常量
    private static final UUID SWORD_BONUS_UUID = UUID.fromString("a1c2d3e4-5678-90ab-cdef-123456789abc");
    private static final UUID ARMOR_BONUS_UUID = UUID.fromString("b2c3d4e5-6789-01bc-def2-234567890bcd");
    private static final UUID TOUGH_BONUS_UUID  = UUID.fromString("5b8f7c3d-7b3d-4c66-a3b4-4f4f8c0e8a5b");
    private static final UUID KB_RES_BONUS_UUID = UUID.fromString("7f3e3a10-5f3d-4c66-a3b4-4f4f8c0e8a5b");

    /* ============================== 属性加成 ============================== */
    public static void applyForgingBonus(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();
        int level = tag.getInt("forge_level");
        double multiplier = LevelUtil.getBonusMultiplier(level);

        if (RemoveUtil.isWeapon(stack)) {
            double base = RemoveUtil.getTrueBaseDamage(stack);
            double bonus = base * multiplier;

            RemoveUtil.removeSingleModifier(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    SWORD_BONUS_UUID, EquipmentSlot.MAINHAND);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    new EntityAttributeModifier(SWORD_BONUS_UUID, "forge_bonus",
                            bonus, EntityAttributeModifier.Operation.ADDITION),
                    EquipmentSlot.MAINHAND
            );
        }

        if (RemoveUtil.isArmor(stack)) {
            EquipmentSlot slot = RemoveUtil.getEquipmentSlot(stack);

            // 护甲
            double baseArmor = RemoveUtil.getCleanBaseArmor(stack);
            double armorBonus = baseArmor * multiplier;
            RemoveUtil.removeSingleModifier(stack, EntityAttributes.GENERIC_ARMOR,
                    ARMOR_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_ARMOR,
                    new EntityAttributeModifier(ARMOR_BONUS_UUID, "forge_bonus",
                            armorBonus, EntityAttributeModifier.Operation.ADDITION),
                    slot);

            // 护甲韧性
            double baseTough = RemoveUtil.getCleanBaseToughness(stack);
            double toughBonus = baseTough * multiplier;
            RemoveUtil.removeSingleModifier(stack, EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                    TOUGH_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                    new EntityAttributeModifier(TOUGH_BONUS_UUID, "forge_bonus",
                            toughBonus, EntityAttributeModifier.Operation.ADDITION),
                    slot);

            // 抗击退
            double baseKbRes = RemoveUtil.getCleanBaseKnockbackRes(stack);
            double kbResBonus = baseKbRes * multiplier;
            RemoveUtil.removeSingleModifier(stack, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    KB_RES_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    new EntityAttributeModifier(KB_RES_BONUS_UUID, "forge_bonus",
                            kbResBonus, EntityAttributeModifier.Operation.ADDITION),
                    slot);
        }
    }

    // 应用星痕强化收益加成
    public static void applyStarMarkBonus(ItemStack stack, float starMark, boolean isWeapon, Random random) {
        NbtCompound tag = stack.getOrCreateNbt();

        if (isWeapon) {
            // 武器：增加攻击力
            double baseDamage = RemoveUtil.getTrueBaseDamage(stack);
            double starBonus = baseDamage * (starMark * 0.01); // 1星痕 = 1%基础攻击力
            tag.putDouble("star_mark_bonus_damage", starBonus);
        } else {
            // 护甲：随机选择护甲值或护甲韧性
            if (random.nextBoolean()) {
                // 增加护甲值
                double baseArmor = RemoveUtil.getCleanBaseArmor(stack);
                double armorBonus = baseArmor * (starMark * 0.01);
                tag.putDouble("star_mark_bonus_armor", armorBonus);
            } else {
                // 增加护甲韧性
                double baseToughness = RemoveUtil.getCleanBaseToughness(stack);
                double toughnessBonus = baseToughness * (starMark * 0.01);
                tag.putDouble("star_mark_bonus_toughness", toughnessBonus);
            }
        }
    }

    // 应用云巡药水效果
    public static void applyCloudRoamEffects(ItemStack stack, float cloudRoam) {
        if (cloudRoam >= 0.5f) {
            NbtCompound tag = stack.getOrCreateNbt();
            int effectLevel = (int) ((cloudRoam - 0.5f) / 0.1f) + 1; // 每0.1云巡增加1级
            tag.putInt("cloud_roam_effect_level", Math.min(effectLevel, 5)); // 最大5级
        }
    }

    /* 防止实例化 */
    private AddUtil() {}
}
package com.example.client;

import com.example.screen.ForgingScreenHandler;
import com.example.util.RemoveUtil;
import com.example.util.LevelUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClientForgeEvents {
    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            // 直接获取 NBT 并检查是否为 null
            NbtCompound nbt = stack.getNbt();
            if (nbt == null) return;

            // 调试输出：物品名称和NBT内容
            System.out.println("=== 物品工具提示调试 ===");
            System.out.println("物品: " + stack.getName().getString());
            System.out.println("NBT: " + nbt.toString());

            /* ---------- 预览 ---------- */
            if (nbt.contains("PreviewDamage")) {
                double preview = nbt.getDouble("PreviewDamage");
                // 修复浮点数精度问题
                double roundedPreview = Math.round(preview * 10) / 10.0;
                lines.add(Text.literal("预览攻击力加成: +" + String.format("%.1f", roundedPreview))
                        .formatted(Formatting.GRAY));
                // 调试输出：预览攻击力
                System.out.println("预览攻击力: " + roundedPreview);
            }
            if (nbt.contains("PreviewArmor")) {
                double preview = nbt.getDouble("PreviewArmor");
                // 修复浮点数精度问题
                double roundedPreview = Math.round(preview * 10) / 10.0;
                lines.add(Text.literal("预览护甲值加成: +" + String.format("%.1f", roundedPreview))
                        .formatted(Formatting.GRAY));
            }
            if (nbt.contains("PreviewChance")) {
                float chance = nbt.getFloat("PreviewChance") * 100;
                // 修复浮点数精度问题
                float roundedChance = Math.round(chance * 10) / 10.0f;
                lines.add(Text.literal("成功率: " + String.format("%.1f", roundedChance) + "%")
                        .formatted(Formatting.YELLOW));
            }

            /* ---------- 实际强化属性 ---------- */
            if (nbt.contains("forge_level", NbtElement.INT_TYPE)) {
                int level = nbt.getInt("forge_level");
                lines.add(Text.literal("强化等级: " + level).formatted(Formatting.GOLD));

                // 使用与服务器端相同的非线性计算方法
                double multiplier = LevelUtil.getBonusMultiplier(level);

                // 调试输出：强化等级和加成倍数
                System.out.println("强化等级: " + level);
                System.out.println("加成倍数: " + multiplier);

                if (ForgingScreenHandler.isWeapon(stack)) {
                    // 获取武器的基础攻击力（不包括玩家基础攻击力）
                    double baseWeaponDamage = nbt.contains("base_damage")
                            ? nbt.getDouble("base_damage")
                            : RemoveUtil.getTrueBaseDamage(stack);

                    // 计算强化系统提供的额外加成
                    double bonusFromForging = baseWeaponDamage * (multiplier - 1.0);

                    // 修复浮点数精度问题，四舍五入到小数点后一位
                    double roundedBonus = Math.round(bonusFromForging * 10) / 10.0;

                    // 调试输出
                    System.out.println("武器基础攻击力: " + baseWeaponDamage);
                    System.out.println("强化系统加成: " + roundedBonus);
                    System.out.println("格式化后的显示值: " + String.format("%.1f", roundedBonus));

                    // 显示强化系统提供的额外攻击力
                    lines.add(Text.literal("强化攻击力加成: +" + String.format("%.1f", roundedBonus))
                            .formatted(Formatting.GREEN));

                } else if (ForgingScreenHandler.isArmor(stack)) {
                    EquipmentSlot slot = ForgingScreenHandler.getEquipmentSlot(stack);
                    if (slot == null) return;

                    // 护甲值
                    double baseArmor = nbt.contains("base_armor")
                            ? nbt.getDouble("base_armor")
                            : RemoveUtil.getCleanBaseArmor(stack);
                    double armorBonusFromForging = baseArmor * (multiplier - 1.0);
                    // 修复浮点数精度问题
                    double roundedArmorBonus = Math.round(armorBonusFromForging * 10) / 10.0;
                    lines.add(Text.literal("强化护甲值加成: +" + String.format("%.1f", roundedArmorBonus))
                            .formatted(Formatting.BLUE));

                    // 护甲韧性
                    double baseTough = nbt.contains("base_toughness")
                            ? nbt.getDouble("base_toughness")
                            : RemoveUtil.getCleanBaseToughness(stack);
                    double toughBonusFromForging = baseTough * (multiplier - 1.0);
                    // 修复浮点数精度问题
                    double roundedToughBonus = Math.round(toughBonusFromForging * 10) / 10.0;
                    if (roundedToughBonus > 0) {
                        lines.add(Text.literal("强化护甲韧性加成: +" + String.format("%.1f", roundedToughBonus))
                                .formatted(Formatting.AQUA));
                    }

                    // 抗击退
                    double baseKb = nbt.contains("base_knockback_res")
                            ? nbt.getDouble("base_knockback_res")
                            : RemoveUtil.getCleanBaseKnockbackRes(stack);
                    double kbResBonusFromForging = baseKb * (multiplier - 1.0);
                    // 修复浮点数精度问题
                    double roundedKbResBonus = Math.round(kbResBonusFromForging * 10) / 10.0;
                    if (roundedKbResBonus > 0) {
                        lines.add(Text.literal("强化抗击退加成: +" + String.format("%.1f", roundedKbResBonus))
                                .formatted(Formatting.LIGHT_PURPLE));
                    }
                }
            }
            System.out.println("=== 调试结束 ===\n");
        });
    }
}
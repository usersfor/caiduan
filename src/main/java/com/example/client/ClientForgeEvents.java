package com.example.client;

import com.example.screen.ForgingScreenHandler;
import com.example.util.ItemUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClientForgeEvents {
    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();

            /* ---------- 预览 ---------- */
            if (nbt.contains("PreviewDamage")) {
                double preview = nbt.getDouble("PreviewDamage");
                lines.add(Text.literal("预览攻击力加成: +" + String.format("%.1f", preview))
                        .formatted(Formatting.GRAY));
            }
            if (nbt.contains("PreviewArmor")) {
                double preview = nbt.getDouble("PreviewArmor");
                lines.add(Text.literal("预览护甲值加成: +" + String.format("%.1f", preview))
                        .formatted(Formatting.GRAY));
            }
            if (nbt.contains("PreviewChance")) {
                float chance = nbt.getFloat("PreviewChance") * 100;
                lines.add(Text.literal("成功率: " + String.format("%.1f", chance) + "%")
                        .formatted(Formatting.YELLOW));
            }

            /* ---------- 实际强化属性 ---------- */
            if (nbt.contains("forge_level", NbtElement.INT_TYPE)) {
                int level = nbt.getInt("forge_level");
                lines.add(Text.literal("强化等级: " + level).formatted(Formatting.GOLD));

                if (ForgingScreenHandler.isWeapon(stack)) {
                    double base = nbt.contains("base_damage")
                            ? nbt.getDouble("base_damage")
                            : ItemUtil.getTrueBaseDamage(stack);
                    double bonus = ItemUtil.getSwordBonus() * base * level;
                    lines.add(Text.literal("攻击力加成: +" + String.format("%.1f", bonus))
                            .formatted(Formatting.GREEN));

                } else if (ForgingScreenHandler.isArmor(stack)) {
                    EquipmentSlot slot = ForgingScreenHandler.getEquipmentSlot(stack);
                    if (slot == null) return; // 仅跳过护甲段落，不影响预览

                    // 护甲值
                    double baseArmor = nbt.contains("base_armor")
                            ? nbt.getDouble("base_armor")
                            : ItemUtil.getCleanBaseArmor(stack, slot);
                    double armorBonus = ItemUtil.getArmorBonus() * baseArmor * level;
                    lines.add(Text.literal("护甲值加成: +" + String.format("%.1f", armorBonus))
                            .formatted(Formatting.BLUE));

                    // 护甲韧性
                    double baseTough = nbt.contains("base_toughness")
                            ? nbt.getDouble("base_toughness")
                            : ItemUtil.getCleanBaseToughness(stack, slot);
                    double toughBonus = ItemUtil.getArmorBonus() * baseTough * level;
                    if (toughBonus > 0) {
                        lines.add(Text.literal("护甲韧性加成: +" + String.format("%.1f", toughBonus))
                                .formatted(Formatting.AQUA));
                    }

                    // 抗击退
                    double baseKb = nbt.contains("base_knockback_res")
                            ? nbt.getDouble("base_knockback_res")
                            : ItemUtil.getCleanBaseKnockbackRes(stack, slot);
                    double kbResBonus = ItemUtil.getArmorBonus() * baseKb * level;
                    if (kbResBonus > 0) {
                        lines.add(Text.literal("抗击退加成: +" + String.format("%.1f", kbResBonus))
                                .formatted(Formatting.LIGHT_PURPLE));
                    }
                }
            }
        });
    }
}
package com.example.util;

import net.minecraft.item.ItemStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class PrefixEffects {

    // 新增：映射表，存储每个前缀对应的UUID，用于只移除特定前缀的修饰符
    private static final Map<String, UUID[]> PREFIX_UUIDS = new HashMap<>();
    static {
        PREFIX_UUIDS.put("龙鳞的", new UUID[]{UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3")});

    }

    // 根据前缀名称应用不同的属性加成
    public static void applyEffect(ItemStack stack, String prefix, double value, ItemPrefix.ItemType itemType) {
        // 修改：不再移除所有属性修饰符，只移除当前前缀可能添加的旧修饰符
        removeOldPrefixModifiers(stack, prefix);

        // 根据前缀类型应用不同的加成
        switch (prefix) {
            case "崩刃的":
            case "卷刃的":
                // 降低攻击伤害 - 使用 value 计算负面效果强度
                double damageReduction = -value * 0.3; // value 越大，负面效果越强
                addAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        "Attack damage penalty", damageReduction,
                        EntityAttributeModifier.Operation.ADDITION,
                        UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5C1"));
                break;

            case "铸裂的":
            case "开裂的":
                // 降低耐久度 - 使用 value 计算负面效果强度
                if (stack.isDamageable()) {
                    int durabilityReduction = (int) (value * 0.4);
                    stack.getOrCreateNbt().putInt("DurabilityReduction", durabilityReduction);
                }
                break;

            case "蚀孔的":
            case "夹渣的":
                // 降低护甲值（如果是护甲）- 使用 value 计算负面效果强度
                double armorReduction = -value * 0.2;
                addAttributeModifier(stack, EntityAttributes.GENERIC_ARMOR,
                        "Armor penalty", armorReduction,
                        EntityAttributeModifier.Operation.ADDITION,
                        UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5C2"));
                break;

            case "形变的":
            case "硌手的":
                // 降低攻击速度 - 使用 value 计算负面效果强度
                double attackSpeedReduction = -value * 0.15;
                addAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_SPEED,
                        "Attack speed penalty", attackSpeedReduction,
                        EntityAttributeModifier.Operation.ADDITION,
                        UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5C3"));
                break;

            case "晦暗的":
            case "无光的":
            case "哑光的":
                // 降低挖掘速度（如果是工具）- 使用 value 计算负面效果强度
                double miningSpeedReduction = -value * 0.1;
                addAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_SPEED,
                        "Mining speed penalty", miningSpeedReduction,
                        EntityAttributeModifier.Operation.ADDITION,
                        UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5C4"));
                break;

            case "泛锈的":
            case "褪色的":
                // 使用时有小概率对使用者造成伤害 - 使用 value 计算效果强度
                int poisonAmplifier = (int) (value * 0.1); // 使用 value 计算中毒等级
                int poisonDuration = 20 + (int) (value * 5); // 使用 value 计算持续时间
                addPotionEffect(stack, StatusEffects.POISON, poisonAmplifier, poisonDuration);
                break;

            case "松垮的":
            case "晃动的":
                // 降低移动速度（当装备时）- 使用 value 计算负面效果强度
                double movementSpeedReduction = -value * 0.05;
                addAttributeModifier(stack, EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        "Movement speed penalty", movementSpeedReduction,
                        EntityAttributeModifier.Operation.ADDITION,
                        UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5C5"));
                break;

            case "遗弃的":
            case "战损的":
                // 降低幸运值 - 使用 value 计算负面效果强度
                double luckReduction = -value * 0.25;
                addAttributeModifier(stack, EntityAttributes.GENERIC_LUCK,
                        "Luck penalty", luckReduction,
                        EntityAttributeModifier.Operation.ADDITION,
                        UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5C6"));
                break;

            // 这里可以继续添加更多前缀效果...

            default:
                // 默认加成根据物品类型 - 使用 value 计算正面效果强度
                if (itemType == ItemPrefix.ItemType.WEAPON) {
                    double damageBonus = value * 0.5; // 使用 value 计算伤害加成
                    addAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            "Attack damage bonus", damageBonus,
                            EntityAttributeModifier.Operation.ADDITION,
                            UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"));
                } else if (itemType == ItemPrefix.ItemType.ARMOR) {
                    double armorBonus = value * 0.5; // 使用 value 计算护甲加成
                    addAttributeModifier(stack, EntityAttributes.GENERIC_ARMOR,
                            "Armor bonus", armorBonus,
                            EntityAttributeModifier.Operation.ADDITION,
                            UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"));
                }
                break;
        }
    }

    // 添加属性修饰符到物品
    private static void addAttributeModifier(ItemStack stack, net.minecraft.entity.attribute.EntityAttribute attribute,
                                             String name, double amount, EntityAttributeModifier.Operation operation, UUID uuid) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("AttributeModifiers", 9)) {
            nbt.put("AttributeModifiers", new NbtList());
        }

        NbtList modifiers = nbt.getList("AttributeModifiers", 10);
        NbtCompound modifier = new NbtCompound();
        modifier.putString("AttributeName", attribute.getTranslationKey());
        modifier.putString("Name", name);
        modifier.putDouble("Amount", amount);
        modifier.putInt("Operation", operation.getId());
        modifier.putUuid("UUID", uuid);
        modifier.putString("Slot", EquipmentSlot.MAINHAND.getName());

        modifiers.add(modifier);
        nbt.put("AttributeModifiers", modifiers);
    }

    // 添加药水效果（使用原版状态效果）
    private static void addPotionEffect(ItemStack stack, StatusEffect effect, int amplifier, int duration) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound potionNbt = new NbtCompound();

        // 存储效果ID
        Identifier effectId = Registries.STATUS_EFFECT.getId(effect);
        if (effectId != null) {
            potionNbt.putString("Effect", effectId.toString());
        }

        potionNbt.putInt("Amplifier", amplifier);
        potionNbt.putInt("Duration", duration);
        nbt.put("PotionEffect", potionNbt);
    }



    // 新增：只移除特定前缀相关的修饰符，保留其他系统的修饰符（如强化系统）
    private static void removeOldPrefixModifiers(ItemStack stack, String prefix) {
        UUID[] uuidsToRemove = PREFIX_UUIDS.getOrDefault(prefix, new UUID[0]);
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("AttributeModifiers", 9)) return;

        NbtList modifiers = nbt.getList("AttributeModifiers", 10);
        NbtList newModifiers = new NbtList();

        for (int i = 0; i < modifiers.size(); i++) {
            NbtCompound modifier = modifiers.getCompound(i);
            UUID uuid = modifier.getUuid("UUID");

            boolean shouldRemove = false;
            for (UUID toRemove : uuidsToRemove) {
                if (uuid.equals(toRemove)) {
                    shouldRemove = true;
                    break;
                }
            }

            if (!shouldRemove) {
                newModifiers.add(modifier);
            }
        }

        if (newModifiers.isEmpty()) {
            nbt.remove("AttributeModifiers");
        } else {
            nbt.put("AttributeModifiers", newModifiers);
        }
    }

    // 移除属性修饰符（不再使用，保留以防其他地方调用）
    private static void removeAttributeModifiers(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("AttributeModifiers", 9)) {
            nbt.remove("AttributeModifiers");
        }
    }
}
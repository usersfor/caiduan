package com.example.util;

import com.example.screen.ForgingScreenHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;


import java.util.UUID;

public final class RemoveUtil {



    /* ================================================================
       对外暴露：读取“干净”的基础值（无强化 modifier）
       ================================================================ */
    public static double getCleanBaseArmor(ItemStack stack) {
        if (stack.isOf(Items.SHIELD)) return 0;
        double tinker = getTinkerDouble(stack, "tconstruct:armor");
        if (tinker > 0) return tinker;
        if (stack.getItem() instanceof ArmorItem armor) return armor.getProtection();
        return 0;
    }

    public static double getCleanBaseToughness(ItemStack stack ) {
        if (stack.isOf(Items.SHIELD)) return 0;
        double tinker = getTinkerDouble(stack, "tconstruct:armor_toughness");
        if (tinker > 0) return tinker;
        if (stack.getItem() instanceof ArmorItem armor) return armor.getToughness();
        return 0;
    }

    public static double getCleanBaseKnockbackRes(ItemStack stack) {
        if (stack.isOf(Items.SHIELD)) return 0;
        double tinker = getTinkerDouble(stack, "tconstruct:knockback_resistance");
        if (tinker > 0) return tinker;
        return 0;
    }

    /* ================================================================
       武器原始攻击
       ================================================================ */
    public static double getTrueBaseDamage(ItemStack stack) {
        double tinker = getTinkerDouble(stack, "tconstruct:attack_damage");
        if (tinker > 0) return tinker;

        if (stack.getItem() instanceof net.minecraft.item.SwordItem s) return s.getAttackDamage();
        if (stack.getItem() instanceof net.minecraft.item.AxeItem a)   return a.getAttackDamage();
        if (stack.getItem() instanceof net.minecraft.item.TridentItem) return 9.0;

        /* 兜底：读取 AttributeModifiers 再减去所有 modifier */
        ItemStack copy = stack.copy();
        removeSingleModifier(copy, EntityAttributes.GENERIC_ATTACK_DAMAGE,
                UUID.fromString("a1c2d3e4-5678-90ab-cdef-123456789abc"),
                EquipmentSlot.MAINHAND);
        return copy.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                .stream()
                .findFirst()
                .map(EntityAttributeModifier::getValue)   // ← 方法引用
                .orElse(1.0);
    }

    /* ================================================================
       一次性把“干净”的基础值写进 NBT（服务端调用）
       ================================================================ */
    public static void writeBaseValuesToNbt(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (isWeapon(stack)) {
            nbt.putDouble("base_damage", getTrueBaseDamage(stack));
        } else if (isArmor(stack)) {
            EquipmentSlot slot = getEquipmentSlot(stack);
            if (slot == null) return;
            nbt.putDouble("base_armor",         getCleanBaseArmor(stack));
            nbt.putDouble("base_toughness",     getCleanBaseToughness(stack));
            nbt.putDouble("base_knockback_res", getCleanBaseKnockbackRes(stack));
        }
    }

    /* ================================================================
       Tinker 读取
       ================================================================ */
    private static double getTinkerDouble(ItemStack stack, String key) {
        NbtCompound root = stack.getNbt();
        if (root == null || !root.contains("tic_stats", NbtElement.COMPOUND_TYPE)) return 0;
        return root.getCompound("tic_stats").getDouble(key);
    }

    /* ================================================================
       删除指定 UUID 的 AttributeModifier（仅操作 copy）
       ================================================================ */
    public static void removeSingleModifier(ItemStack stack,
                                            EntityAttribute attribute,
                                            UUID uuid,
                                            EquipmentSlot slot) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) return;

        NbtList oldList = nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
        NbtList newList = new NbtList();

        // 防 NPE + 消除警告
        Identifier id = Registries.ATTRIBUTE.getId(attribute);
        String attrId = id != null ? id.toString() : attribute.toString();
        String slotId = slot.getName();

        for (int i = 0; i < oldList.size(); i++) {
            NbtCompound tag = oldList.getCompound(i);
            boolean matchUuid = uuid.equals(tag.getUuid("UUID"));
            boolean matchAttr = attrId.equals(tag.getString("AttributeName"));
            boolean matchSlot = slotId.equals(tag.getString("Slot"));
            if (!(matchUuid && matchAttr && matchSlot)) {
                newList.add(tag);
            }
        }

        if (newList.isEmpty()) nbt.remove("AttributeModifiers");
        else nbt.put("AttributeModifiers", newList);
    }

    /* ------------------- 小工具 ------------------- */
    public static boolean isWeapon(ItemStack s) {
        return ForgingScreenHandler.isWeapon(s);
    }
    public static boolean isArmor(ItemStack s) {
        return ForgingScreenHandler.isArmor(s);
    }
    public static EquipmentSlot getEquipmentSlot(ItemStack s) {
        return ForgingScreenHandler.getEquipmentSlot(s);
    }

    /* 防止实例化 */
    private RemoveUtil() {}
}
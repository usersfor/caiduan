package com.example.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class IronUtil {
    private static final Map<String, String> MATERIAL_MAPPING = new HashMap<>();
    private static final Map<String, MaterialData> MATERIAL_DATA = new HashMap<>();

    // 材料数据内部类
    public static class MaterialData {
        public final float baseBonus;
        public final float decayRate;

        public MaterialData(float baseBonus, float decayRate) {
            this.baseBonus = baseBonus;
            this.decayRate = decayRate;
        }
    }

    static {
        // 原版材料
        MATERIAL_MAPPING.put("minecraft:coal", "coal");
        MATERIAL_MAPPING.put("minecraft:copper_ingot", "copper");
        MATERIAL_MAPPING.put("minecraft:iron_ingot", "iron");
        MATERIAL_MAPPING.put("minecraft:lapis_lazuli", "lapis");
        MATERIAL_MAPPING.put("minecraft:redstone", "redstone");
        MATERIAL_MAPPING.put("minecraft:gold_ingot", "gold");
        MATERIAL_MAPPING.put("minecraft:emerald", "emerald");
        MATERIAL_MAPPING.put("minecraft:diamond", "diamond");
        MATERIAL_MAPPING.put("minecraft:netherite_ingot", "netherite");

        // 锡 - 不同模组的锡锭
        MATERIAL_MAPPING.put("modern_industrialization:tin_ingot", "tin");
        MATERIAL_MAPPING.put("mekanism:tin_ingot", "tin");
        MATERIAL_MAPPING.put("thermal:tin_ingot", "tin");
        MATERIAL_MAPPING.put("create:tin_ingot", "tin");
        MATERIAL_MAPPING.put("immersiveengineering:ingot_tin", "tin");
        MATERIAL_MAPPING.put("tconstruct:tin_ingot", "tin");

        // 铅 - 不同模组的铅锭
        MATERIAL_MAPPING.put("modern_industrialization:lead_ingot", "lead");
        MATERIAL_MAPPING.put("mekanism:lead_ingot", "lead");
        MATERIAL_MAPPING.put("thermal:lead_ingot", "lead");
        MATERIAL_MAPPING.put("create:lead_ingot", "lead");
        MATERIAL_MAPPING.put("immersiveengineering:ingot_lead", "lead");
        MATERIAL_MAPPING.put("tconstruct:lead_ingot", "lead");

        // 银 - 不同模组的银锭
        MATERIAL_MAPPING.put("modern_industrialization:silver_ingot", "silver");
        MATERIAL_MAPPING.put("mekanism:silver_ingot", "silver");
        MATERIAL_MAPPING.put("thermal:silver_ingot", "silver");
        MATERIAL_MAPPING.put("create:silver_ingot", "silver");
        MATERIAL_MAPPING.put("immersiveengineering:ingot_silver", "silver");
        MATERIAL_MAPPING.put("tconstruct:silver_ingot", "silver");

        // 青铜 - 不同模组的青铜锭
        MATERIAL_MAPPING.put("modern_industrialization:bronze_ingot", "bronze");
        MATERIAL_MAPPING.put("mekanism:bronze_ingot", "bronze");
        MATERIAL_MAPPING.put("thermal:bronze_ingot", "bronze");
        MATERIAL_MAPPING.put("create:bronze_ingot", "bronze");
        MATERIAL_MAPPING.put("immersiveengineering:ingot_bronze", "bronze");
        MATERIAL_MAPPING.put("tconstruct:bronze_ingot", "bronze");

        // 钢 - 不同模组的钢锭
        MATERIAL_MAPPING.put("modern_industrialization:steel_ingot", "steel");
        MATERIAL_MAPPING.put("mekanism:steel_ingot", "steel");
        MATERIAL_MAPPING.put("thermal:steel_ingot", "steel");
        MATERIAL_MAPPING.put("create:steel_ingot", "steel");
        MATERIAL_MAPPING.put("immersiveengineering:ingot_steel", "steel");
        MATERIAL_MAPPING.put("tconstruct:steel_ingot", "steel");

        // 材料基础加成和衰减率
        // 原版材料
        MATERIAL_DATA.put("coal", new MaterialData(0.005F, 0.15F));
        MATERIAL_DATA.put("copper", new MaterialData(0.006F, 0.14F));
        MATERIAL_DATA.put("iron", new MaterialData(0.008F, 0.13F));
        MATERIAL_DATA.put("lapis", new MaterialData(0.007F, 0.14F));
        MATERIAL_DATA.put("redstone", new MaterialData(0.007F, 0.14F));
        MATERIAL_DATA.put("gold", new MaterialData(0.009F, 0.12F));
        MATERIAL_DATA.put("emerald", new MaterialData(0.012F, 0.11F));
        MATERIAL_DATA.put("diamond", new MaterialData(0.015F, 0.10F));
        MATERIAL_DATA.put("netherite", new MaterialData(0.02F, 0.08F));

        // 其他模组材料
        MATERIAL_DATA.put("tin", new MaterialData(0.006F, 0.14F));
        MATERIAL_DATA.put("lead", new MaterialData(0.007F, 0.13F));
        MATERIAL_DATA.put("silver", new MaterialData(0.009F, 0.12F));
        MATERIAL_DATA.put("bronze", new MaterialData(0.008F, 0.13F));
        MATERIAL_DATA.put("steel", new MaterialData(0.011F, 0.11F));
    }

    public static String getMaterialType(Item item) {
        Identifier itemId = Registries.ITEM.getId(item);
        return MATERIAL_MAPPING.getOrDefault(itemId.toString(), "unknown");
    }

    public static boolean isValidMaterial(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        return MATERIAL_MAPPING.containsKey(itemId.toString());
    }

    public static MaterialData getMaterialData(String materialType) {
        return MATERIAL_DATA.getOrDefault(materialType, new MaterialData(0.005F, 0.15F));
    }

    public static MaterialData getMaterialData(Item item) {
        String materialType = getMaterialType(item);
        return getMaterialData(materialType);
    }
}
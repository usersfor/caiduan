package com.example.item;

import com.example.TemplateMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModTabs {

    // 声明注册表 key
    public static final RegistryKey<ItemGroup> FORGE_TAB_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(TemplateMod.MOD_ID, "forge"));

    // 构建并注册 ItemGroup（必须在 onInitialize 阶段完成）
    public static void register() {
        ItemGroup forgeTab = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.CONDUIT)) // 使用海灵核心作为图标
                .displayName(Text.translatable("itemGroup." + TemplateMod.MOD_ID + ".forge"))
                .build();

        Registry.register(Registries.ITEM_GROUP, FORGE_TAB_KEY, forgeTab);

        // 添加所有新物品到创造模式物品栏
        ItemGroupEvents.modifyEntriesEvent(FORGE_TAB_KEY).register(entries -> {
            // 【普通·白】
            entries.add(ModItems.CAN_GUANG_QIANG_HUA_SHI);
            entries.add(ModItems.CUI_HUO_SUI_JING);

            // 【高级·蓝】
            entries.add(ModItems.CANG_YUE_HUI_SHI);
            entries.add(ModItems.XING_CHEN_DUAN_PIAN);

            // 【稀有·紫】
            entries.add(ModItems.LONG_YAN_JIE_JING);
            entries.add(ModItems.SHI_ZHI_MO_HE);

            // 【神器·粉】
            entries.add(ModItems.MING_YUAN_PO_JIE_SHI);
            entries.add(ModItems.TIAN_ZHAO_HUANG_XING_ZUAN);

            // 【传说·橙】
            entries.add(ModItems.ZHONG_YAN_HUN_DUN_YUAN_HUO);
            entries.add(ModItems.SHI_SHI_YONG_HENG_CHAO_XIN_XING);

            // 【使徒级·特殊】
            entries.add(ModItems.HE_ER_DE_DE_SHI_KONG_SUI_YING);
            entries.add(ModItems.KA_EN_DE_HUI_MIE_YU_JIN);
        });
    }
}
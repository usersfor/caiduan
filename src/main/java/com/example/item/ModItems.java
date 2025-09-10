// ModItems.java
package com.example.item;

import com.example.TemplateMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    // 【普通·白】
    public static final Item CAN_GUANG_QIANG_HUA_SHI = new Item(new Item.Settings());
    public static final Item CUI_HUO_SUI_JING = new Item(new Item.Settings());

    // 【高级·蓝】
    public static final Item CANG_YUE_HUI_SHI = new Item(new Item.Settings());
    public static final Item XING_CHEN_DUAN_PIAN = new Item(new Item.Settings());

    // 【稀有·紫】
    public static final Item LONG_YAN_JIE_JING = new Item(new Item.Settings());
    public static final Item SHI_ZHI_MO_HE = new Item(new Item.Settings());

    // 【神器·粉】
    public static final Item MING_YUAN_PO_JIE_SHI = new Item(new Item.Settings());
    public static final Item TIAN_ZHAO_HUANG_XING_ZUAN = new Item(new Item.Settings());

    // 【传说·橙】
    public static final Item ZHONG_YAN_HUN_DUN_YUAN_HUO = new Item(new Item.Settings());
    public static final Item SHI_SHI_YONG_HENG_CHAO_XIN_XING = new Item(new Item.Settings());

    // 【使徒级·特殊】
    public static final Item HE_ER_DE_DE_SHI_KONG_SUI_YING = new Item(new Item.Settings());
    public static final Item KA_EN_DE_HUI_MIE_YU_JIN = new Item(new Item.Settings());

    public static void register() {
        // 【普通·白】
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "can_guang_qiang_hua_shi"), CAN_GUANG_QIANG_HUA_SHI);
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "cui_huo_sui_jing"), CUI_HUO_SUI_JING);

        // 【高级·蓝】
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "cang_yue_hui_shi"), CANG_YUE_HUI_SHI);
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "xing_chen_duan_pian"), XING_CHEN_DUAN_PIAN);

        // 【稀有·紫】
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "long_yan_jie_jing"), LONG_YAN_JIE_JING);
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "shi_zhi_mo_he"), SHI_ZHI_MO_HE);

        // 【神器·粉】
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "ming_yuan_po_jie_shi"), MING_YUAN_PO_JIE_SHI);
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "tian_zhao_huang_xing_zuan"), TIAN_ZHAO_HUANG_XING_ZUAN);

        // 【传说·橙】
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "zhong_yan_hun_dun_yuan_huo"), ZHONG_YAN_HUN_DUN_YUAN_HUO);
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "shi_shi_yong_heng_chao_xin_xing"), SHI_SHI_YONG_HENG_CHAO_XIN_XING);

        // 【使徒级·特殊】
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "he_er_de_de_shi_kong_sui_ying"), HE_ER_DE_DE_SHI_KONG_SUI_YING);
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "ka_en_de_hui_mie_yu_jin"), KA_EN_DE_HUI_MIE_YU_JIN);
    }
}
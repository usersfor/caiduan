package com.example.item;

import com.example.TemplateMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;              // ← 新增
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModTabs {

    // 1. 声明注册表 key
    public static final RegistryKey<ItemGroup> FORGE_TAB_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(TemplateMod.MOD_ID, "forge"));

    // 2. 构建并注册 ItemGroup（必须在 onInitialize 阶段完成）
    public static void register() {
        ItemGroup forgeTab = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.CONDUIT))          // ← 改成海灵核心
                .displayName(Text.translatable("itemGroup." + TemplateMod.MOD_ID + ".forge"))
                .build();

        Registry.register(Registries.ITEM_GROUP, FORGE_TAB_KEY, forgeTab);

        // 添加物品
        ItemGroupEvents.modifyEntriesEvent(FORGE_TAB_KEY).register(entries -> {
            entries.add(ModItems.SWORD_UPGRADE_TEMPLATE);
            entries.add(ModItems.ARMOR_UPGRADE_TEMPLATE);
            entries.add(ModItems.FORGING_TABLE_ITEM);
        });
    }
}
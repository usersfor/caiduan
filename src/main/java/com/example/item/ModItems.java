// ModItems.java
package com.example.item;

import com.example.TemplateMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item SWORD_UPGRADE_TEMPLATE = new Item(new Item.Settings());
    public static final Item ARMOR_UPGRADE_TEMPLATE = new Item(new Item.Settings());

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "sword_upgrade_template"), SWORD_UPGRADE_TEMPLATE);
        Registry.register(Registries.ITEM, new Identifier(TemplateMod.MOD_ID, "armor_upgrade_template"), ARMOR_UPGRADE_TEMPLATE);
    }
}
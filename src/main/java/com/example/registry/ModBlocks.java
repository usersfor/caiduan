package com.example.registry;

import com.example.block.ForgingTableBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    // 直接 copy 铁砧的设置，不需要 Material
    public static final ForgingTableBlock FORGING_TABLE = new ForgingTableBlock(
            net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings
                    .copyOf(net.minecraft.block.Blocks.ANVIL));

    public static void register() {
        Registry.register(Registries.BLOCK,
                new Identifier("template-mod", "forging_table"), FORGING_TABLE);
        Registry.register(Registries.ITEM,
                new Identifier("template-mod", "forging_table"),
                new BlockItem(FORGING_TABLE, new FabricItemSettings()));
    }
}
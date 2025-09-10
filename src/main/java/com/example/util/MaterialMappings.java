package com.example.util;

import java.util.HashMap;
import java.util.Map;

public class MaterialMappings {
    // 材料映射表 - 将不同模组的相同材料映射到统一标识
    public static final Map<String, String> MATERIAL_MAPPING = new HashMap<>();

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
        MATERIAL_MAPPING.put("minecraft:crying_obsidian", "obsidian");
        MATERIAL_MAPPING.put("minecraft:obsidian", "obsidian");
        MATERIAL_MAPPING.put("minecraft:glowstone", "glowstone");
        MATERIAL_MAPPING.put("minecraft:amethyst_shard", "crystal");
        MATERIAL_MAPPING.put("minecraft:bone", "bone");


        // 不同模组的锡锭
        MATERIAL_MAPPING.put("mekanism:ingot_tin", "tin");
        // 不同模组的铅锭
        MATERIAL_MAPPING.put("mekanism:ingot_lead", "lead");
        MATERIAL_MAPPING.put("tconstruct:lead_ingot", "lead");
        // 不同模组的银锭
        MATERIAL_MAPPING.put("mekanism:silver_ingot", "silver");

        MATERIAL_MAPPING.put("tconstruct:silver_ingot", "silver");
        // 不同模组的青铜锭
        MATERIAL_MAPPING.put("mekanism:ingot_bronze", "bronze");
        MATERIAL_MAPPING.put("tconstruct:amethyst_bronze_ingot", "bronze");
        MATERIAL_MAPPING.put("tconstruct:bronze_ingot", "bronze");
        // 不同模组的钢锭
        MATERIAL_MAPPING.put("mekanism:ingot_steel", "steel");
        MATERIAL_MAPPING.put("megacells:sky_steel_ingot", "steel");
        MATERIAL_MAPPING.put("tconstruct:steel_ingot", "steel");
        MATERIAL_MAPPING.put("tconstruct:slimesteel_ingot", "steel");
        MATERIAL_MAPPING.put("cataclysm:black_steel_ingot", "steel");
        // 不同模组的锇锭
        MATERIAL_MAPPING.put("mekanism:ingot_osmium", "osmium");
        // 不同模组的铀锭
        MATERIAL_MAPPING.put("mekanism:ingot_uranium", "uranium");
        // 不同模组的黑曜石
        MATERIAL_MAPPING.put("mekanism:ingot_refined_obsidian", "obsidian");
        MATERIAL_MAPPING.put("draconicevolution:infused_obsidian", "obsidian");
        // 不同模组的荧石锭
        MATERIAL_MAPPING.put("mekanism:ingot_refined_glowstone", "glowstone");
        MATERIAL_MAPPING.put("kamen_tinker:glowstone_azure_steel", "glowstone");
        MATERIAL_MAPPING.put("tinkers_advanced:osgloglas_ingot", "glowstone");
        // 不同模组的水晶
        MATERIAL_MAPPING.put("ae2:certus_quartz_crystal", "crystal");
        MATERIAL_MAPPING.put("ae2:fluix_crystal", "crystal");
        MATERIAL_MAPPING.put("ae2:charged_certus_quartz_crystal", "crystal");
        MATERIAL_MAPPING.put("tconstruct:ender_slime_crystal", "crystal");
        MATERIAL_MAPPING.put("tconstruct:ichor_slime_crystal", "crystal");
        MATERIAL_MAPPING.put("tconstruct:earth_slime_crystal", "crystal");
        MATERIAL_MAPPING.put("tconstruct:sky_slime_crystal", "crystal");

        MATERIAL_MAPPING.put("tinkers_advanced:disintegrate_crystal", "crystal");
        MATERIAL_MAPPING.put("tinkers_advanced:voltaic_crystal", "crystal");
        MATERIAL_MAPPING.put("tinkers_advanced:resonance_crystal", "crystal");
        MATERIAL_MAPPING.put("enderio:presclent_crystal", "crystal");
        MATERIAL_MAPPING.put("enderio:pulsating_crystal", "crystal");
        MATERIAL_MAPPING.put("enderio:vibrant_crystal", "crystal");
        MATERIAL_MAPPING.put("enderio:enticing_crystal", "crystal");
        MATERIAL_MAPPING.put("enderio:ender_crystal", "crystal");
        MATERIAL_MAPPING.put("enderio:weather_crystal", "crystal");

        MATERIAL_MAPPING.put("botania:gaia_pylon", "crystal");
        MATERIAL_MAPPING.put("botania:natura_pylon", "crystal");
        MATERIAL_MAPPING.put("botania:mana_pylon", "crystal");
        MATERIAL_MAPPING.put("botania:corporea_crystal_cube", "crystal");
        // 不同模组的硅
        MATERIAL_MAPPING.put("ae2:silicon", "silicon");
        MATERIAL_MAPPING.put("enderio:silicon", "silicon");
        MATERIAL_MAPPING.put("mekanism_extras:ingot_naquadah", "silicon");
        //mek和他的扩展模组的
        MATERIAL_MAPPING.put("mekanism:alloy_infused", "mekanism");
        MATERIAL_MAPPING.put("mekanism:alloy_reinforced", "mekanism");
        MATERIAL_MAPPING.put("mekanism:alloy_atomic", "mekanism");
        MATERIAL_MAPPING.put("mekanism_extras:alloy_radiance", "mekanismex");
        MATERIAL_MAPPING.put("mekanism_extras:ingot_thermonuclear", "mekanismex");
        MATERIAL_MAPPING.put("mekanism_extras:ingot_shining", "mekanismex");
        MATERIAL_MAPPING.put("mekanism_extras:ingot_spectrum", "mekanismex");
        // 戈伯模组的
        MATERIAL_MAPPING.put("gobber2:gobber2_ingot", "gobber2");
        MATERIAL_MAPPING.put("gobber2:gobber2_ingot_nether", "gobber2");
        MATERIAL_MAPPING.put("gobber2:gobber2_ingot_end", "gobber2");
        // tetra模组的
        MATERIAL_MAPPING.put("tetra:pristine_lapis", "tetra");
        MATERIAL_MAPPING.put("tetra:pristine_emerald", "tetra");
        MATERIAL_MAPPING.put("tetra:pristine_diamond", "tetra");
        MATERIAL_MAPPING.put("tetra:pristine_amethyst", "tetra");
        //匠魂模组的
        MATERIAL_MAPPING.put("tconstruct:cobalt_ingot", "tconstruct");
        MATERIAL_MAPPING.put("tconstruct:manyullyn_ingot", "tconstruct");
        MATERIAL_MAPPING.put("tconstruct:rose_gold_ingot", "tconstruct");
        MATERIAL_MAPPING.put("tconstruct:pig_iron_ingot", "tconstruct");
        MATERIAL_MAPPING.put("tconstruct:cinderslime_ingot", "tconstruct");
        MATERIAL_MAPPING.put("tconstruct:queens_slime_ingot", "tconstruct");
        MATERIAL_MAPPING.put("tconstruct:hepatizon_ingot", "tconstruct");
        MATERIAL_MAPPING.put("tconstruct:seared_brick", "tconstruct");
        MATERIAL_MAPPING.put("tconstruct:scorched_brick", "tconstruct");
        //龙之进化模组的
        MATERIAL_MAPPING.put("draconicevolution:draconium_ingot", "draconicevolution");
        MATERIAL_MAPPING.put("draconicevolution:draconium_core", "draconicevolution");
        MATERIAL_MAPPING.put("draconicevolution:wyvern_core", "draconicevolution");
        MATERIAL_MAPPING.put("draconicevolution:chaotic_core", "draconicevolution");
        MATERIAL_MAPPING.put("draconicevolution:awakened_core", "draconicevolution");
        //灾变模组的
        MATERIAL_MAPPING.put("cataclysm:witherite_ingot", "cataclysm");
        MATERIAL_MAPPING.put("cataclysm:ancient_metal_ingot", "cataclysm");
        MATERIAL_MAPPING.put("cataclysm:lacrima", "cataclysm");
        MATERIAL_MAPPING.put("cataclysm:ignitium_ingot", "cataclysm");
        MATERIAL_MAPPING.put("cataclysm:cursium_ingot", "cataclysm");
        MATERIAL_MAPPING.put("cataclysm:essence_of_the_storm", "cataclysm");
        MATERIAL_MAPPING.put("cataclysm:lava_power_cell", "cataclysm");
        MATERIAL_MAPPING.put("cataclysm:monstrous_horn", "cataclysm");
        //暮色森林模组的
        MATERIAL_MAPPING.put("twilightforest:ironwood_ingot", "twilightforest");
        MATERIAL_MAPPING.put("twilightforest:steeleaf_ingot", "twilightforest");
        MATERIAL_MAPPING.put("twilightforest:knightmetal_ingot", "twilightforest");
        MATERIAL_MAPPING.put("twilightforest:fiery_ingot", "twilightforest");
        MATERIAL_MAPPING.put("twilightforest:carminite", "twilightforest");
        //植物魔法模组的
        MATERIAL_MAPPING.put("botania:manasteel_ingot", "botania");
        MATERIAL_MAPPING.put("botania:terrasteel_ingot", "botania");
        MATERIAL_MAPPING.put("botania:elementium_ingot", "botania");
        MATERIAL_MAPPING.put("botania:gaia_ingot", "botania");
        //额外植物学模组的
        MATERIAL_MAPPING.put("extrabotany:orichalcos_ingot", "extrabotany");
        MATERIAL_MAPPING.put("extrabotany:photonium_ingot", "extrabotany");
        MATERIAL_MAPPING.put("extrabotany:shadowium_ingot", "extrabotany");
        MATERIAL_MAPPING.put("extrabotany:aerialite_ingot", "extrabotany");
        //浮云工坊模组的
        MATERIAL_MAPPING.put("cloudertinker:chimera_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:colairon_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:evilmare_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:bloodshed_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:dectird_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:prublaze_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:minogold_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:minodiamond_gem", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:hydra_scale", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:phantom_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:fierdistear", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:forest_gem", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:timering", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:orescore", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:changeheart", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:sorteye", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:mazeslime_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:steeleafalloy", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:steelbone", "bone");
        MATERIAL_MAPPING.put("cloudertinker:forestbone", "bone");
        MATERIAL_MAPPING.put("cloudertinker:devil_chain", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:questiron_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:glaze_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:magnet_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:compositesteeleaf_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:frostiron_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:magala_ingot", "cloudertinker");
        MATERIAL_MAPPING.put("cloudertinker:glavenus_ingot", "cloudertinker");
        //墨工坊模组的
        MATERIAL_MAPPING.put("momotinker:dimensional_prism", "momotinker");
        MATERIAL_MAPPING.put("momotinker:interdimensional_crystal", "momotinker");
        MATERIAL_MAPPING.put("momotinker:arriving_at_the_other_shore", "momotinker");
        MATERIAL_MAPPING.put("momotinker:heartsteel", "momotinker");
        MATERIAL_MAPPING.put("momotinker:spirit_visage", "momotinker");
        MATERIAL_MAPPING.put("momotinker:gluttony_core", "momotinker");
        MATERIAL_MAPPING.put("momotinker:greedy_contract", "momotinker");
        MATERIAL_MAPPING.put("momotinker:lust_mirror", "momotinker");
        MATERIAL_MAPPING.put("momotinker:arrogance_proof", "momotinker");
        MATERIAL_MAPPING.put("momotinker:rage_stone_statue", "momotinker");
        MATERIAL_MAPPING.put("momotinker:lazy_grail", "momotinker");
        MATERIAL_MAPPING.put("momotinker:jealous_notes", "momotinker");
        MATERIAL_MAPPING.put("momotinker:compassion_mask", "momotinker");
        MATERIAL_MAPPING.put("momotinker:dim_dark_gold", "momotinker");
        MATERIAL_MAPPING.put("momotinker:stained_blood_gold", "momotinker");
        MATERIAL_MAPPING.put("momotinker:starry_mysterious_gold", "momotinker");
        MATERIAL_MAPPING.put("momotinker:devouring_demon_gold", "momotinker");
        MATERIAL_MAPPING.put("momotinker:nihilism", "momotinker");
        MATERIAL_MAPPING.put("momotinker:meteor_nucleus", "momotinker");
        MATERIAL_MAPPING.put("momotinker:meteor_nucleus_block", "momotinker");
        MATERIAL_MAPPING.put("momotinker:ashen_platinum", "momotinker");
        MATERIAL_MAPPING.put("momotinker:hadal_platinum", "momotinker");
        MATERIAL_MAPPING.put("momotinker:stellar_core_platinum", "momotinker");
        MATERIAL_MAPPING.put("momotinker:crystallized_platinum", "momotinker");
        MATERIAL_MAPPING.put("momotinker:living_platinum", "momotinker");
        MATERIAL_MAPPING.put("momotinker:twilight_purple_gold", "momotinker");
        MATERIAL_MAPPING.put("momotinker:timetrace_purple_gold", "momotinker");
        MATERIAL_MAPPING.put("momotinker:reversetime_purple_gold", "momotinker");
        MATERIAL_MAPPING.put("momotinker:core_of_felony", "momotinker");
        MATERIAL_MAPPING.put("momotinker:star_seeking_pointer", "momotinker");
        MATERIAL_MAPPING.put("momotinker:rhythm_slime", "momotinker");
        MATERIAL_MAPPING.put("momotinker:dragon_jade", "momotinker");
        //蘑菇幻想模组的
        MATERIAL_MAPPING.put("mushroom:blue_eye_white_dragon", "mushroom");
        MATERIAL_MAPPING.put("mushroom:rat_world", "mushroom");
        MATERIAL_MAPPING.put("mushroom:heimdallr", "mushroom");
        MATERIAL_MAPPING.put("mushroom:slime_court_jester", "mushroom");
        MATERIAL_MAPPING.put("mushroom:dragon_gobber", "mushroom");
        MATERIAL_MAPPING.put("mushroom:mizi", "mushroom");
        MATERIAL_MAPPING.put("mushroom:sjngp", "mushroom");
        MATERIAL_MAPPING.put("mushroom:shiguan", "mushroom");
        MATERIAL_MAPPING.put("mushroom:ham", "mushroom");
        MATERIAL_MAPPING.put("mushroom:plague_steel", "mushroom");
        MATERIAL_MAPPING.put("mushroom:bee_man", "mushroom");
        MATERIAL_MAPPING.put("mushroom:copper_green", "mushroom");
        MATERIAL_MAPPING.put("mushroom:nightmare_fuel", "mushroom");
        MATERIAL_MAPPING.put("mushroom:pure_horror", "mushroom");
        MATERIAL_MAPPING.put("mushroom:dark_thing", "mushroom");
        MATERIAL_MAPPING.put("mushroom:sublimation_substance", "mushroom");
        MATERIAL_MAPPING.put("mushroom:holy", "mushroom");
        MATERIAL_MAPPING.put("mushroom:liga", "mushroom");
        MATERIAL_MAPPING.put("mushroom:legend_paper", "mushroom");
        MATERIAL_MAPPING.put("mushroom:no_head", "mushroom");
        MATERIAL_MAPPING.put("mushroom:slime_pawn", "mushroom");
        MATERIAL_MAPPING.put("mushroom:zero_mushroom", "mushroom");
        MATERIAL_MAPPING.put("mushroom:ultimate_alchemical_compound", "mushroom");
        //前沿匠艺模组的
        MATERIAL_MAPPING.put("advanced:bismuth_ingot", "advanced");
        MATERIAL_MAPPING.put("advanced:bismuthinite", "advanced");
        MATERIAL_MAPPING.put("advanced:irradium_ingot", "advanced");
        MATERIAL_MAPPING.put("advanced:hot_reinforced_steel", "advanced");
        MATERIAL_MAPPING.put("advanced:penumatic_reinforced_steel", "advanced");
        MATERIAL_MAPPING.put("advanced:basalz_signalum", "advanced");
        MATERIAL_MAPPING.put("advanced:blitz_lumium", "advanced");
        MATERIAL_MAPPING.put("advanced:blizz_enderium", "advanced");
        MATERIAL_MAPPING.put("advanced:disintegrate_crystal", "advanced");
        MATERIAL_MAPPING.put("advanced:activated_chromatic_steel", "advanced");
        MATERIAL_MAPPING.put("advanced:blaze_netherite", "advanced");
        MATERIAL_MAPPING.put("advanced:iridium_chunk", "advanced");
        MATERIAL_MAPPING.put("advanced:densium_ingot", "advanced");
        MATERIAL_MAPPING.put("advanced:neutronite_ingot", "advanced");
        MATERIAL_MAPPING.put("advanced:osgloglas_ingot", "advanced");
        MATERIAL_MAPPING.put("advanced:resonance_crystal", "advanced");
        MATERIAL_MAPPING.put("advanced:voltaic_crystal", "advanced");
        MATERIAL_MAPPING.put("advanced:protocite_pellet", "advanced");
        MATERIAL_MAPPING.put("advanced:antimony_ingot", "advanced");
        MATERIAL_MAPPING.put("advanced:stibnite", "advanced");
        MATERIAL_MAPPING.put("advanced:blitz_lumium_nugget", "advanced");
        MATERIAL_MAPPING.put("advanced:antimony_nugget", "advanced");
        MATERIAL_MAPPING.put("advanced:bismuth_nugget", "advanced");
        MATERIAL_MAPPING.put("advanced:nutrition_slime_ingot", "advanced");
        MATERIAL_MAPPING.put("advanced:ultra_dense_book", "advanced");
        //魂樱工匠模组的
        MATERIAL_MAPPING.put("sakuratinker:youkai_ingot", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:soul_sakura", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:fiery_crystal", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:wither_heart", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:nihilite_raw_ore", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:nihilite_ingot", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:nihilite_nugget", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:eezo_ingot", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:eezo_nugget", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:arcane_alloy", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:colorful_ingot", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:blood_bound_steel", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:steady_alloy", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:blood_ball", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:blood_drop", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:south_star", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:terracryst", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:prometheum_raw", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:prometheum_ingot", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:orichalcum_raw", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:orichalcum_ingot", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:aurumos", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:bear_interest_ingot", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:slime_crystal_earth", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:slime_crystal_sky", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:slime_crystal_nether", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:slime_ball_frost", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:slime_ball_mycelium", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:slime_ball_echo", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:mycelium_slimesteel", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:frost_slimesteel", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:echo_slimesteel", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:goozma", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:pyrothium", "sakuratinker");
        MATERIAL_MAPPING.put("sakuratinker:unholy_alloy", "sakuratinker");
        //匠魂校准模组的
        MATERIAL_MAPPING.put("tinkerscalibration:mangobberslime_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:fazelle_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:lindsteel_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:emperorslime_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:soulgold_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:mandite_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:titanium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:jazz_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:halleium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:gravity_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:grain_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:hothium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:immersed_silver_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:inert_witherium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:magiga_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:oraclium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:steamium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:tonium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:rabbit_iron_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:mushroom_iron_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:beetroot_iron_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:stellarium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:witherium_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:alumite_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:moonsteel_ingot", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:gravity_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:halleium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:hothium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:tonium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:immersed_silver_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:inert_witherium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:magiga_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:rabbit_iron_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:mushroom_iron_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:beetroot_iron_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:oraclium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:steamium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:stellarium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:witherium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:hymon", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:mangobberslime_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:fazelle_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:lindsteel_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:emperorslime_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:mandite_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:titanium_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:jazz_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:alumite_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:soulgold_nugget", "tinkerscalibration");
        MATERIAL_MAPPING.put("tinkerscalibration:hymon_scrap", "tinkerscalibration");
        //拔刀剑模组的
        MATERIAL_MAPPING.put("slashblade:proudsoul", "slashblade");
        MATERIAL_MAPPING.put("slashblade:proudsoul_tiny", "slashblade");
        MATERIAL_MAPPING.put("slashblade:proudsoul_ingot", "slashblade");
        MATERIAL_MAPPING.put("slashblade:proudsoul_sphere", "slashblade");
        MATERIAL_MAPPING.put("slashblade:proudsoul_crystal", "slashblade");
        MATERIAL_MAPPING.put("slashblade:proudsoul_trapezohedron", "slashblade");
        //不同模组的骨
        MATERIAL_MAPPING.put("tconstruct:venombone", "bone");
        MATERIAL_MAPPING.put("tconstruct:blazing_bone", "bone");
        MATERIAL_MAPPING.put("kamen_tinker:skeleton_azure_steel", "bone");
        MATERIAL_MAPPING.put("kamen_tinker:wither_skeleton_azure_steel", "bone");
        MATERIAL_MAPPING.put("more_mod_tetra:bone_ingot", "bone");
        MATERIAL_MAPPING.put("more_mod_tetra:wither_bone_ingot", "bone");
        MATERIAL_MAPPING.put("cataclysm:koboleton_bone", "bone");
        MATERIAL_MAPPING.put("irons_spellbooks:frozen_bone", "bone");


        // 其他常见模组材料可以继续添加...
    }

    // 检查是否为有效材料（原版或模组材料）
    public static boolean isValidMaterial(String itemId) {
        return MATERIAL_MAPPING.containsKey(itemId);
    }

    // 获取材料类型
    public static String getMaterialType(String itemId) {
        return MATERIAL_MAPPING.getOrDefault(itemId, "unknown");
    }
}
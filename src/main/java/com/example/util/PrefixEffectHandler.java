package com.example.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrefixEffectHandler {
    // 记录每位玩家上一tick手持物品赋予的效果，用于对比和移除
    private static final Map<UUID, Map<StatusEffect, Integer>> lastTickEffects = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                handlePlayerEffects(player);
            }
        });
    }

    private static void handlePlayerEffects(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        Map<StatusEffect, Integer> currentEffects = new HashMap<>(); // 当前tick应生效的效果

        // 检查主手和副手物品
        ItemStack mainHandStack = player.getMainHandStack();
        ItemStack offHandStack = player.getOffHandStack();

        // 从手持物品中读取效果并添加到当前应生效的映射中
        addEffectsFromItem(mainHandStack, currentEffects);
        addEffectsFromItem(offHandStack, currentEffects);

        // 获取上一tick记录的效果
        Map<StatusEffect, Integer> previousEffects = lastTickEffects.getOrDefault(playerId, new HashMap<>());

        // 移除不再存在的效果（例如切换了物品）
        for (StatusEffect effect : previousEffects.keySet()) {
            if (!currentEffects.containsKey(effect)) {
                player.removeStatusEffect(effect);
            }
        }

        // 添加当前需要生效的效果
        for (Map.Entry<StatusEffect, Integer> entry : currentEffects.entrySet()) {
            StatusEffect effect = entry.getKey();
            int amplifier = entry.getValue();

            // 检查是否已有该效果且等级相同，如果没有或者等级不同则应用新效果
            StatusEffectInstance currentInstance = player.getStatusEffect(effect);
            if (currentInstance == null || currentInstance.getAmplifier() != amplifier) {
                // 创建一个新的效果实例，持续时间设置稍长（这里设为100 tick，即5秒），并通过tick事件持续刷新
                StatusEffectInstance newInstance = new StatusEffectInstance(
                        effect,
                        100, // 持续时间（ticks）
                        amplifier,
                        true, // 环境效果（通常药水效果为此类）
                        false, // 不显示粒子（可选，根据喜好）
                        true // 在库存界面显示图标
                );
                player.addStatusEffect(newInstance);
            }
        }

        // 更新记录，为下一tick比较做准备
        lastTickEffects.put(playerId, currentEffects);
    }

    // 从ItemStack的NBT中读取药水效果信息
    private static void addEffectsFromItem(ItemStack stack, Map<StatusEffect, Integer> effectsMap) {
        if (stack.isEmpty()) return;

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("PotionEffect")) { // 与你存储的键保持一致
            NbtCompound potionNbt = nbt.getCompound("PotionEffect");
            String effectIdStr = potionNbt.getString("Effect");
            int amplifier = potionNbt.getInt("Amplifier");
            // Duration 通常用于显示，实际应用时我们可以持续刷新，这里不需要读取

            Identifier effectId = Identifier.tryParse(effectIdStr);
            if (effectId != null) {
                StatusEffect effect = Registries.STATUS_EFFECT.get(effectId);
                if (effect != null) {
                    effectsMap.put(effect, amplifier);
                }
            }
        }
    }
}
package com.example.screen;

import com.example.TemplateMod;
import com.example.util.ItemPrefix;
import com.example.util.ItemUtil;
import com.example.util.MaterialMappings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class ForgingScreenHandler extends ScreenHandler {

    private final Inventory inv = new SimpleInventory(5);

    private static final int SLOT_INPUT    = 0;
    private static final int SLOT_MAT1     = 1;
    private static final int SLOT_MAT2     = 2;
    private static final int SLOT_MAT3     = 3;
    private static final int SLOT_RESULT   = 4;

    private static final UUID SWORD_BONUS_UUID = UUID.fromString("a1c2d3e4-5678-90ab-cdef-123456789abc");
    private static final UUID ARMOR_BONUS_UUID = UUID.fromString("b2c3d4e5-6789-01bc-def2-234567890bcd");
    private static final UUID TOUGH_BONUS_UUID  = UUID.fromString("5b8f7c3d-7b3d-4c66-a3b4-4f4f8c0e8a5b");
    private static final UUID KB_RES_BONUS_UUID = UUID.fromString("7f3e3a10-5f3d-4c66-a3b4-4f4f8c0e8a5b");

    // 特殊材料组合的额外加成
    private static final Map<Set<String>, Float> SPECIAL_COMBOS = new HashMap<>();
    static {
        // 添加特殊材料组合及其额外加成
        // 锡和铅组合
        SPECIAL_COMBOS.put(Set.of("tin", "lead"), 0.03F);
        // 铜和锡组合（青铜）
        SPECIAL_COMBOS.put(Set.of("copper", "tin"), 0.04F);
        // 铁和煤组合（钢）
        SPECIAL_COMBOS.put(Set.of("iron", "coal"), 0.05F);
        // 金和红石组合
        SPECIAL_COMBOS.put(Set.of("gold", "redstone"), 0.035F);
        // 钻石和绿宝石组合
        SPECIAL_COMBOS.put(Set.of("diamond", "emerald"), 0.045F);
    }

    // 根据材料等级计算基础加成和衰减率
    private static float getBaseBonusByRank(int rank) {
        return 0.004F + rank * 0.0012F;
    }

    private static float getDecayRateByRank(int rank) {
        return 0.22F - rank * 0.012F;
    }

    // 创建材料属性映射表
    private static final Map<String, Integer> MATERIAL_RANKS = new HashMap<>();
    static {
        MATERIAL_RANKS.put("coal", 1);
        MATERIAL_RANKS.put("copper", 2);
        MATERIAL_RANKS.put("tin", 3);
        MATERIAL_RANKS.put("iron", 4);
        MATERIAL_RANKS.put("lead", 5);
        MATERIAL_RANKS.put("lapis", 6);
        MATERIAL_RANKS.put("redstone", 7);
        MATERIAL_RANKS.put("silver", 8);
        MATERIAL_RANKS.put("gold", 9);
        MATERIAL_RANKS.put("bronze", 10);
        MATERIAL_RANKS.put("emerald", 11);
        MATERIAL_RANKS.put("diamond", 12);
        MATERIAL_RANKS.put("steel", 13);
        MATERIAL_RANKS.put("netherite", 14);
    }

    /* ---------- 工具 ---------- */
    public  static EquipmentSlot getEquipmentSlot(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem armor) return armor.getSlotType();
        if (item == Items.ELYTRA)            return EquipmentSlot.CHEST;
        if (item == Items.SHIELD)            return EquipmentSlot.OFFHAND;
        return null;
    }

    private static boolean isTinkerWeapon(ItemStack stack) {
        var nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("tic_stats", 10)) return false;
        return nbt.getCompound("tic_stats").contains("tconstruct:attack_damage");
    }

    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var nbt = stack.getNbt();
        if (nbt != null && nbt.getBoolean("is_weapon")) return true;
        if (stack.getItem() instanceof SwordItem) return true;
        if (stack.getItem() instanceof AxeItem) return true;
        if (stack.getItem() instanceof TridentItem) return true;
        if (stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .containsKey(EntityAttributes.GENERIC_ATTACK_DAMAGE)) return true;
        return isTinkerWeapon(stack);
    }

    private static boolean isTinkerArmor(ItemStack stack) {
        var nbt = stack.getNbt();
        return nbt != null &&
                nbt.contains("tic_stats", 10) &&
                nbt.getCompound("tic_stats").contains("tconstruct:armor");
    }

    public static boolean isArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.isOf(Items.SHIELD)) return true;
        var nbt = stack.getNbt();
        if (nbt != null && nbt.getBoolean("is_armor")) return true;
        if (getEquipmentSlot(stack) != null) return true;
        return isTinkerArmor(stack);
    }

    // 检查是否为有效材料（原版或模组材料）
    private static boolean isValidMaterial(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        return MaterialMappings.isValidMaterial(itemId.toString());
    }

    // 获取材料类型
    private static String getMaterialType(Item material) {
        Identifier itemId = Registries.ITEM.getId(material);
        String itemIdString = itemId.toString();

        // 使用 MaterialMappings 中的映射表
        return MaterialMappings.MATERIAL_MAPPING.getOrDefault(itemIdString, itemIdString);
    }

    // 获取材料等级
    private static int getMaterialRank(Item material) {
        String materialType = getMaterialType(material);
        return MATERIAL_RANKS.getOrDefault(materialType, 0);
    }

    // 获取材料的基础加成
    private static float getMaterialBaseBonus(Item material) {
        int rank = getMaterialRank(material);
        return getBaseBonusByRank(rank);
    }

    // 获取材料的衰减率
    private static float getMaterialDecayRate(Item material) {
        int rank = getMaterialRank(material);
        return getDecayRateByRank(rank);
    }

    /* ============================== 构造 ============================== */
    public ForgingScreenHandler(int syncId,
                                PlayerInventory playerInv,
                                ScreenHandlerContext context) {
        super(TemplateMod.FORGING_SCREEN_HANDLER, syncId);

        addSlot(new Slot(inv, SLOT_INPUT, 27, 35) {
            @Override public boolean canInsert(ItemStack stack) {
                return isWeapon(stack) || isArmor(stack);
            }
        });

        // 三个材料槽，每个最多可放64个
        for (int i = 0; i < 3; i++) {
            addSlot(new Slot(inv, SLOT_MAT1 + i, 47 + i * 18, 17) {
                @Override public boolean canInsert(ItemStack stack) {
                    return isValidMaterial(stack);
                }
                @Override public int getMaxItemCount() {
                    return 64; // 允许最多堆叠64个
                }
            });
        }

        addSlot(new Slot(inv, SLOT_RESULT, 151, 35) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public void onTakeItem(PlayerEntity player, ItemStack stack) {
                inv.setStack(SLOT_RESULT, ItemStack.EMPTY);
                if (!player.getWorld().isClient) onCraft(player);
                player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            }
        });

        /* 玩家背包 */
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 9; x++)
                addSlot(new Slot(playerInv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
        for (int x = 0; x < 9; x++)
            addSlot(new Slot(playerInv, x, 8 + x * 18, 142));

        addListener(new ScreenHandlerListener() {
            @Override public void onSlotUpdate(ScreenHandler h, int slotId, ItemStack stack) { updateResult(); }
            @Override public void onPropertyUpdate(ScreenHandler h, int property, int value) {}
        });
        updateResult();
    }

    public static ForgingScreenHandler createClient(int syncId,
                                                    PlayerInventory inv,
                                                    PacketByteBuf buf) {
        return new ForgingScreenHandler(syncId, inv,
                ScreenHandlerContext.create(inv.player.getWorld(), buf.readBlockPos()));
    }

    /* ============================== 工具 ============================== */
    private float getBaseChance(int level) {
        if (level <= 5) return 0.90F;
        if (level <= 9) return 0.80F;
        if (level <= 15) return 0.70F;
        if (level <= 19) return 0.60F;
        if (level <= 25) return 0.50F;
        if (level <= 29) return 0.40F;
        if (level <= 35) return 0.30F;
        if (level <= 39) return 0.20F;
        if (level <= 45) return 0.10F;
        if (level <= 49) return 0.08F;
        if (level <= 55) return 0.06F;
        if (level <= 59) return 0.05F;
        if (level <= 65) return 0.04F;
        if (level <= 69) return 0.02F;
        if (level <= 75) return 0.01F;
        if (level <= 79) return 0.005F;
        return 0.001F;
    }

    // 将方法移到类主体中作为独立方法
    private float calculateMaterialBonus(int count, float baseBonus, float decayRate) {
        if (count <= 0) return 0;

        float totalBonus = 0;
        for (int i = 1; i <= count; i++) {
            // 使用指数衰减公式
            float factor = (float) Math.exp(-decayRate * (i - 1));
            totalBonus += baseBonus * factor;
        }
        return totalBonus;
    }

    // 检查特殊材料组合并提供额外加成
    private float checkSpecialCombos(ItemStack mat1, ItemStack mat2, ItemStack mat3) {
        if (mat1.isEmpty() || mat2.isEmpty() || mat3.isEmpty()) {
            return 0.0F;
        }

        Set<String> currentCombo = new HashSet<>();
        currentCombo.add(getMaterialType(mat1.getItem()));
        currentCombo.add(getMaterialType(mat2.getItem()));
        currentCombo.add(getMaterialType(mat3.getItem()));

        // 检查是否有匹配的特殊组合
        for (Map.Entry<Set<String>, Float> entry : SPECIAL_COMBOS.entrySet()) {
            if (currentCombo.equals(entry.getKey())) {
                return entry.getValue();
            }
        }

        return 0.0F;
    }

    /* ============================== 新的强化数值计算 ============================== */
    private double getBonusMultiplier(int level) {
        if (level <= 0) return 1.0; // 0级时，加成倍率为1.0（100%基础值）
        if (level <= 9) return 1.0 + level * 0.01; // +1~+9 每级+1%（1.01, 1.02, ..., 1.09）

        return switch (level) {
            case 10 -> 1.15;
            case 11 -> 1.25;
            case 12 -> 1.39;
            case 13 -> 1.55;
            case 14 -> 1.74;
            case 15 -> 1.96;
            case 16 -> 2.21;
            case 17 -> 2.49;
            case 18 -> 2.80;
            case 19 -> 3.14;
            case 20 -> 3.51;
            default -> 3.51 + (level - 20) * 0.01;
        };
    }

    /* ============================== 预览 ============================== */
    private void updateResult() {
        ItemStack input = inv.getStack(SLOT_INPUT);
        ItemStack mat1  = inv.getStack(SLOT_MAT1);
        ItemStack mat2  = inv.getStack(SLOT_MAT2);
        ItemStack mat3  = inv.getStack(SLOT_MAT3);

        // 检查是否有输入和至少一个材料
        boolean hasMaterials = !mat1.isEmpty() || !mat2.isEmpty() || !mat3.isEmpty();
        if (input.isEmpty() || !hasMaterials) {
            inv.setStack(SLOT_RESULT, ItemStack.EMPTY);
            return;
        }

        // 使用新方法获取材料数量
        MaterialCounts counts = calculateMaterialCounts(mat1, mat2, mat3);

        // 1. 复制物品，但只拷贝"匠魂需要"的字段，不整块覆盖
        ItemStack preview = input.copy();
        NbtCompound src = input.getOrCreateNbt();
        NbtCompound dst = preview.getOrCreateNbt();

        // 把匠魂核心字段搬过去
        dst.copyFrom(src);
        // 清空预览用的临时键
        dst.remove("PreviewDamage");
        dst.remove("PreviewArmor");
        dst.remove("PreviewChance");

        int currentLevel = dst.getInt("forge_level");
        int nextLevel = currentLevel + 1;

        if (isWeapon(preview)) {
            double base = ItemUtil.getTrueBaseDamage(preview);
            double currentMultiplier = getBonusMultiplier(currentLevel);
            double nextMultiplier = getBonusMultiplier(nextLevel);
            // 计算下一级相比当前级增加的伤害值
            double bonusIncrease = base * (nextMultiplier - currentMultiplier);
            dst.putDouble("PreviewDamage", bonusIncrease);
        } else if (isArmor(preview)) {
            EquipmentSlot slot = getEquipmentSlot(preview);
            double base = ItemUtil.getCleanBaseArmor(preview, slot);
            double currentMultiplier = getBonusMultiplier(currentLevel);
            double nextMultiplier = getBonusMultiplier(nextLevel);
            // 计算下一级相比当前级增加的护甲值
            double bonusIncrease = base * (nextMultiplier - currentMultiplier);
            dst.putDouble("PreviewArmor", bonusIncrease);
        }

        dst.putFloat("PreviewChance", getCurrentChance(input, counts.material1Rank, counts.material1Count,
                counts.material2Rank, counts.material2Count, counts.material3Rank, counts.material3Count, mat1, mat2, mat3));
        inv.setStack(SLOT_RESULT, preview);
    }

    /* ============================== 强化逻辑 ============================== */
    private void onCraft(PlayerEntity player) {
        ItemStack input = inv.getStack(SLOT_INPUT);
        ItemStack mat1 = inv.getStack(SLOT_MAT1);
        ItemStack mat2 = inv.getStack(SLOT_MAT2);
        ItemStack mat3 = inv.getStack(SLOT_MAT3);

        // 检查是否有输入和至少一个材料
        boolean hasMaterials = !mat1.isEmpty() || !mat2.isEmpty() || !mat3.isEmpty();
        if (input.isEmpty() || !hasMaterials) return;

        // 使用新方法获取材料数量
        MaterialCounts counts = calculateMaterialCounts(mat1, mat2, mat3);

        // 消耗材料 - 每个槽位消耗1个材料
        if (counts.material1Count > 0) {
            inv.removeStack(SLOT_MAT1, 1);
        }
        if (counts.material2Count > 0) {
            inv.removeStack(SLOT_MAT2, 1);
        }
        if (counts.material3Count > 0) {
            inv.removeStack(SLOT_MAT3, 1);
        }

        float chance = getCurrentChance(input, counts.material1Rank, counts.material1Count,
                counts.material2Rank, counts.material2Count, counts.material3Rank, counts.material3Count, mat1, mat2, mat3);
        boolean success = player.getRandom().nextFloat() < chance;

        if (success) {
            NbtCompound tag = input.getOrCreateNbt();
            int baseLevel = tag.getInt("forge_level");

            // 检查跳级概率
            float jumpChance = player.getRandom().nextFloat();
            int levelsGained = 1; // 默认增加1级

            if (jumpChance < 0.005F) { // 0.5%概率跳3级
                levelsGained = 3;
            } else if (jumpChance < 0.015F) { // 1%概率跳2级
                levelsGained = 2;
            }

            int newLevel = baseLevel + levelsGained;
            tag.putInt("forge_level", newLevel);

            // ✅ 新增：写入基础值到 NBT，供客户端直接读取
            ItemUtil.writeBaseValuesToNbt(input);

            // 应用属性加成和修饰词
            applyForgingBonus(input);

            // 根据物品类型应用修饰词 - 使用新的ItemPrefix方法
            ItemPrefix.ItemType itemType = ItemPrefix.getItemType(input);
            if (itemType == ItemPrefix.ItemType.WEAPON) {
                double baseDamage = ItemUtil.getTrueBaseDamage(input);
                double multiplier = getBonusMultiplier(newLevel);

                ItemPrefix.applyItemPrefix(input, newLevel, baseDamage, multiplier);
            } else if (itemType == ItemPrefix.ItemType.ARMOR) {
                EquipmentSlot slot = getEquipmentSlot(input);
                double baseArmor = ItemUtil.getCleanBaseArmor(input, slot);
                double multiplier = getBonusMultiplier(newLevel);

                ItemPrefix.applyItemPrefix(input, newLevel, baseArmor, multiplier);
            }

            // 根据跳级情况发送不同的消息
            if (levelsGained > 1) {
                player.sendMessage(Text.literal("§6强化成功！触发跳级效果！当前等级: " + newLevel + " (+" + levelsGained + ")"), false);
            } else {
                player.sendMessage(Text.literal("强化成功！当前等级: " + newLevel), false);
            }
        } else {
            NbtCompound tag = input.getOrCreateNbt();
            int currentLevel = tag.getInt("forge_level");

            // 根据当前等级决定失败惩罚
            if (currentLevel >= 15) {
                // +15 以上失败直接清除武器
                inv.setStack(SLOT_INPUT, ItemStack.EMPTY);
                player.sendMessage(Text.literal("强化失败！武器已损毁！"), false);
            } else if (currentLevel >= 12) {
                // +12 开始失败不掉落但掉 3 级
                int newLevel = Math.max(0, currentLevel - 3); // 确保等级不低于0
                tag.putInt("forge_level", newLevel);

                // 重新应用属性修饰符以反映等级下降
                applyForgingBonus(input);

                // 更新修饰词 - 使用新的ItemPrefix方法
                ItemPrefix.ItemType itemType = ItemPrefix.getItemType(input);
                if (itemType == ItemPrefix.ItemType.WEAPON) {
                    double baseDamage = ItemUtil.getTrueBaseDamage(input);
                    double multiplier = getBonusMultiplier(newLevel);

                    ItemPrefix.applyItemPrefix(input, newLevel, baseDamage, multiplier);
                } else if (itemType == ItemPrefix.ItemType.ARMOR) {
                    EquipmentSlot slot = getEquipmentSlot(input);
                    double baseArmor = ItemUtil.getCleanBaseArmor(input, slot);
                    double multiplier = getBonusMultiplier(newLevel);

                    ItemPrefix.applyItemPrefix(input, newLevel, baseArmor, multiplier);
                }

                player.sendMessage(Text.literal("强化失败！武器等级下降了3级！当前等级: " + newLevel), false);
                // 更新物品栈以触发客户端同步
                inv.setStack(SLOT_INPUT, input);
            } else {
                // 12级以下失败清除武器（原逻辑）
                inv.setStack(SLOT_INPUT, ItemStack.EMPTY);
                player.sendMessage(Text.literal("强化失败！"), false);
            }
        }
        updateResult();
    }

    // 修改后的材料计数类
    private static class MaterialCounts {
        public final int material1Rank;
        public final int material1Count;
        public final int material2Rank;
        public final int material2Count;
        public final int material3Rank;
        public final int material3Count;

        public MaterialCounts(int material1Rank, int material1Count, int material2Rank, int material2Count,
                              int material3Rank, int material3Count) {
            this.material1Rank = material1Rank;
            this.material1Count = material1Count;
            this.material2Rank = material2Rank;
            this.material2Count = material2Count;
            this.material3Rank = material3Rank;
            this.material3Count = material3Count;
        }
    }

    // 修改后的计算材料数量的方法
    private MaterialCounts calculateMaterialCounts(ItemStack mat1, ItemStack mat2, ItemStack mat3) {
        int material1Rank = 0;
        int material1Count = mat1.isEmpty() ? 0 : mat1.getCount();

        if (!mat1.isEmpty()) {
            material1Rank = getMaterialRank(mat1.getItem());
        }

        int material2Rank = 0;
        int material2Count = mat2.isEmpty() ? 0 : mat2.getCount();

        if (!mat2.isEmpty()) {
            material2Rank = getMaterialRank(mat2.getItem());
        }

        int material3Rank = 0;
        int material3Count = mat3.isEmpty() ? 0 : mat3.getCount();

        if (!mat3.isEmpty()) {
            material3Rank = getMaterialRank(mat3.getItem());
        }

        return new MaterialCounts(material1Rank, material1Count, material2Rank, material2Count,
                material3Rank, material3Count);
    }

    // 修改后的getCurrentChance方法，添加了特殊组合检查
    private float getCurrentChance(ItemStack weapon, int material1Rank, int material1Count,
                                   int material2Rank, int material2Count, int material3Rank, int material3Count,
                                   ItemStack mat1Stack, ItemStack mat2Stack, ItemStack mat3Stack) {
        int level = weapon.getOrCreateNbt().getInt("forge_level");
        if (level >= 99) return 0.0F;
        float base = getBaseChance(level);

        // 材料1加成
        float material1Bonus = 0.0F;
        if (material1Rank > 0) {
            float baseBonus = getMaterialBaseBonus(mat1Stack.getItem());
            float decayRate = getMaterialDecayRate(mat1Stack.getItem());
            material1Bonus = calculateMaterialBonus(material1Count, baseBonus, decayRate);
        }

        // 材料2加成
        float material2Bonus = 0.0F;
        if (material2Rank > 0) {
            float baseBonus = getMaterialBaseBonus(mat2Stack.getItem());
            float decayRate = getMaterialDecayRate(mat2Stack.getItem());
            material2Bonus = calculateMaterialBonus(material2Count, baseBonus, decayRate);
        }

        // 材料3加成
        float material3Bonus = 0.0F;
        if (material3Rank > 0) {
            float baseBonus = getMaterialBaseBonus(mat3Stack.getItem());
            float decayRate = getMaterialDecayRate(mat3Stack.getItem());
            material3Bonus = calculateMaterialBonus(material3Count, baseBonus, decayRate);
        }

        // 特殊组合加成
        float comboBonus = checkSpecialCombos(mat1Stack, mat2Stack, mat3Stack);

        return base + material1Bonus + material2Bonus + material3Bonus + comboBonus;
    }

    /* ============================== 属性加成 ============================== */
    private void applyForgingBonus(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();
        int level = tag.getInt("forge_level");
        double multiplier = getBonusMultiplier(level);

        if (isWeapon(stack)) {
            double base = ItemUtil.getTrueBaseDamage(stack);
            double bonus = base * multiplier;

            removeSingleModifier(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    SWORD_BONUS_UUID, EquipmentSlot.MAINHAND);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    new EntityAttributeModifier(SWORD_BONUS_UUID, "forge_bonus",
                            bonus, EntityAttributeModifier.Operation.ADDITION),
                    EquipmentSlot.MAINHAND
            );
        }

        if (isArmor(stack)) {
            EquipmentSlot slot = getEquipmentSlot(stack);

            // 护甲
            double baseArmor = ItemUtil.getCleanBaseArmor(stack, slot);
            double armorBonus = baseArmor * multiplier;
            removeSingleModifier(stack, EntityAttributes.GENERIC_ARMOR,
                    ARMOR_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_ARMOR,
                    new EntityAttributeModifier(ARMOR_BONUS_UUID, "forge_bonus",
                            armorBonus, EntityAttributeModifier.Operation.ADDITION),
                    slot);

            // 护甲韧性
            double baseTough = ItemUtil.getCleanBaseToughness(stack, slot);
            double toughBonus = baseTough * multiplier;
            removeSingleModifier(stack, EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                    TOUGH_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                    new EntityAttributeModifier(TOUGH_BONUS_UUID, "forge_bonus",
                            toughBonus, EntityAttributeModifier.Operation.ADDITION),
                    slot);

            // 抗击退
            double baseKbRes = ItemUtil.getCleanBaseKnockbackRes(stack, slot);
            double kbResBonus = baseKbRes * multiplier;
            removeSingleModifier(stack, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    KB_RES_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    new EntityAttributeModifier(KB_RES_BONUS_UUID, "forge_bonus",
                            kbResBonus, EntityAttributeModifier.Operation.ADDITION),
                    slot);
        }
    }

    /* ================================================================
       删除指定 UUID 的那一条 AttributeModifier；其余保留
       ================================================================ */
    private static void removeSingleModifier(ItemStack stack,
                                             EntityAttribute attribute,
                                             UUID uuid,
                                             EquipmentSlot slot) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) return;

        NbtList oldList = nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
        NbtList newList = new NbtList();

        String attrId = Registries.ATTRIBUTE.getId(attribute).toString();
        String slotId = slot.getName();

        for (NbtElement elem : oldList) {
            NbtCompound tag = (NbtCompound) elem;
            if (tag.getUuid("UUID").equals(uuid)
                    && attrId.equals(tag.getString("AttributeName"))
                    && slotId.equals(tag.getString("Slot"))) continue;
            newList.add(tag);
        }

        if (newList.isEmpty()) nbt.remove("AttributeModifiers");
        else nbt.put("AttributeModifiers", newList);
    }

    /* ============================== 必要覆写 ============================== */
    @Override public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
                inv.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    @Override public boolean canUse(PlayerEntity player) { return true; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();

        if (index >= 5) {
            if (isWeapon(stack) || isArmor(stack)) {
                if (!insertItem(stack, SLOT_INPUT, SLOT_INPUT + 1, false))
                    return ItemStack.EMPTY;
            } else if (isValidMaterial(stack)) {
                // 尝试放入任意一个材料槽位
                boolean inserted = false;
                for (int i = SLOT_MAT1; i <= SLOT_MAT3; i++) {
                    if (insertItem(stack, i, i + 1, false)) {
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) return ItemStack.EMPTY;
            } else return ItemStack.EMPTY;
        } else if (!insertItem(stack, 5, 41, true)) return ItemStack.EMPTY;

        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return copy;
    }
}
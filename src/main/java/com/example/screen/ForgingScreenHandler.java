package com.example.screen;

import com.example.TemplateMod;
import com.example.item.ModItems;
import com.example.util.ItemUtil;
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

import java.util.UUID;

public class ForgingScreenHandler extends ScreenHandler {

    private final Inventory inv = new SimpleInventory(5);

    private static final int SLOT_INPUT    = 0;
    private static final int SLOT_TEMPLATE = 1;
    private static final int SLOT_ADD_MAT1 = 2;
    private static final int SLOT_ADD_MAT2 = 3;
    private static final int SLOT_RESULT   = 4;

    private static final UUID SWORD_BONUS_UUID = UUID.fromString("a1c2d3e4-5678-90ab-cdef-123456789abc");
    private static final UUID ARMOR_BONUS_UUID = UUID.fromString("b2c3d4e5-6789-01bc-def2-234567890bcd");
    private static final UUID TOUGH_BONUS_UUID  = UUID.fromString("5b8f7c3d-7b3d-4c66-a3b4-4f4f8c0e8a5b");
    private static final UUID KB_RES_BONUS_UUID = UUID.fromString("7f3e3a10-5f3d-4c66-a3b4-4f4f8c0e8a5b");

    // 材料基础加成和衰减率
    private static final float PROTECTION_STONE_BASE = 0.04F;
    private static final float PROTECTION_STONE_DECAY = 0.04F;
    private static final float DIAMOND_BASE = 0.01F;
    private static final float DIAMOND_DECAY = 0.1F;
    private static final float NAUTILUS_SHELL_BASE = 0.015F;
    private static final float NAUTILUS_SHELL_DECAY = 0.09F;
    private static final float NETHERITE_INGOT_BASE = 0.02F;
    private static final float NETHERITE_INGOT_DECAY = 0.07F;
    private static final float NETHER_STAR_BASE = 0.025F;
    private static final float NETHER_STAR_DECAY = 0.06F;
    private static final float DRAGON_EGG_BASE = 0.03F;
    private static final float DRAGON_EGG_DECAY = 0.05F;

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
        addSlot(new Slot(inv, SLOT_TEMPLATE, 47, 17) {
            @Override public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.SWORD_UPGRADE_TEMPLATE);
            }
        });
        addSlot(new Slot(inv, SLOT_ADD_MAT1, 75, 17) {
            @Override public boolean canInsert(ItemStack stack) {
                // 保护石槽位
                return stack.isOf(ModItems.PROTECTION_STONE);
            }
        });
        addSlot(new Slot(inv, SLOT_ADD_MAT2, 93, 17) {
            @Override public boolean canInsert(ItemStack stack) {
                // 多种材料槽位：钻石、海洋之星、下界合金锭、下界之星、龙蛋
                return stack.isOf(Items.DIAMOND) ||
                        stack.isOf(Items.NAUTILUS_SHELL) ||
                        stack.isOf(Items.NETHERITE_INGOT) ||
                        stack.isOf(Items.NETHER_STAR) ||
                        stack.isOf(Items.DRAGON_EGG);
            }
        });
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
            totalBonus += baseBonus * (1.0F - (i - 1) * decayRate);
        }
        return totalBonus;
    }

    /* ============================== 新的强化数值计算 ============================== */
    private double getBonusMultiplier(int level) {
        if (level <= 0) return 1.0; // 0级时，加成倍率为1.0（100%基础值）
        if (level <= 9) return 1.0 + level * 0.01; // +1~+9 每级+1%（1.01, 1.02, ..., 1.09）

        switch (level) {
            case 10: return 1.15;  // +15%（总共115%）
            case 11: return 1.25;  // +25%（总共125%）
            case 12: return 1.39;  // +39%（总共139%）
            case 13: return 1.55;  // +55%（总共155%）
            case 14: return 1.74;  // +74%（总共174%）
            case 15: return 1.96;  // +96%（总共196%）
            case 16: return 2.21;  // +121%（总共221%）
            case 17: return 2.49;  // +149%（总共249%）
            case 18: return 2.80;  // +180%（总共280%）
            case 19: return 3.14;  // +214%（总共314%）
            case 20: return 3.51;  // +251%（总共351%）
            default: // 20级以后，每级增加1%
                return 3.51 + (level - 20) * 0.01;
        }
    }
    /* ============================== 预览 ============================== */
    private void updateResult() {
        ItemStack input = inv.getStack(SLOT_INPUT);
        ItemStack mat1  = inv.getStack(SLOT_ADD_MAT1);
        ItemStack mat2  = inv.getStack(SLOT_ADD_MAT2);
        ItemStack temp  = inv.getStack(SLOT_TEMPLATE);

        if (input.isEmpty() || mat2.isEmpty()) {
            inv.setStack(SLOT_RESULT, ItemStack.EMPTY);
            return;
        }

        // 使用新方法获取材料数量
        MaterialCounts counts = calculateMaterialCounts(temp, mat1, mat2);

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

        dst.putFloat("PreviewChance", getCurrentChance(input, counts.templateCount, counts.protectionStone, counts.material2Type, counts.material2Count));
        inv.setStack(SLOT_RESULT, preview);
    }

    /* ============================== 强化逻辑 ============================== */
    private void onCraft(PlayerEntity player) {
        ItemStack input = inv.getStack(SLOT_INPUT);
        ItemStack mat1 = inv.getStack(SLOT_ADD_MAT1);
        ItemStack mat2 = inv.getStack(SLOT_ADD_MAT2);
        ItemStack temp = inv.getStack(SLOT_TEMPLATE);

        if (input.isEmpty() || mat2.isEmpty()) return;

        // 使用新方法获取材料数量
        MaterialCounts counts = calculateMaterialCounts(temp, mat1, mat2);

        // 消耗材料
        if (counts.protectionStone > 0) {
            consumeMaterialFromSlots(counts.protectionStone, ModItems.PROTECTION_STONE);
        }

        if (counts.material2Count > 0) {
            consumeMaterialFromSlots(counts.material2Count, counts.material2Type);
        }

        if (counts.templateCount > 0) {
            inv.removeStack(SLOT_TEMPLATE, counts.templateCount);
        }

        float chance = getCurrentChance(input, counts.templateCount, counts.protectionStone, counts.material2Type, counts.material2Count);
        boolean success = player.getRandom().nextFloat() < chance;

        // 其余失败处理逻辑保持不变...
        if (success) {
            NbtCompound tag = input.getOrCreateNbt();
            int newLevel = tag.getInt("forge_level") + 1;
            tag.putInt("forge_level", newLevel);

            // ✅ 新增：写入基础值到 NBT，供客户端直接读取
            ItemUtil.writeBaseValuesToNbt(input);

            applyForgingBonus(input);   // 只追加 AttributeModifier
            player.sendMessage(Text.literal("强化成功！当前等级: " + newLevel), false);
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
        public final int templateCount;
        public final int protectionStone;
        public final Item material2Type;
        public final int material2Count;

        public MaterialCounts(int templateCount, int protectionStone, Item material2Type, int material2Count) {
            this.templateCount = templateCount;
            this.protectionStone = protectionStone;
            this.material2Type = material2Type;
            this.material2Count = material2Count;
        }
    }

    // 修改后的计算材料数量的方法
    private MaterialCounts calculateMaterialCounts(ItemStack template, ItemStack mat1, ItemStack mat2) {
        final int MAX_MATERIALS = 10;

        int templateCount = Math.min(template.isEmpty() ? 0 : template.getCount(), MAX_MATERIALS);
        int protectionStone = 0;
        Item material2Type = null;
        int material2Count = 0;

        // 处理保护石
        if (!mat1.isEmpty() && mat1.isOf(ModItems.PROTECTION_STONE)) {
            protectionStone = Math.min(mat1.getCount(), MAX_MATERIALS);
        }

        // 处理第二个材料槽
        if (!mat2.isEmpty()) {
            if (mat2.isOf(Items.DIAMOND) ||
                    mat2.isOf(Items.NAUTILUS_SHELL) ||
                    mat2.isOf(Items.NETHERITE_INGOT) ||
                    mat2.isOf(Items.NETHER_STAR) ||
                    mat2.isOf(Items.DRAGON_EGG)) {
                material2Type = mat2.getItem();
                material2Count = Math.min(mat2.getCount(), MAX_MATERIALS);
            }
        }

        return new MaterialCounts(templateCount, protectionStone, material2Type, material2Count);
    }

    // 修改后的getCurrentChance方法
    private float getCurrentChance(ItemStack weapon, int templateCount, int protectionStone, Item material2Type, int material2Count) {
        int level = weapon.getOrCreateNbt().getInt("forge_level");
        if (level >= 99) return 0.0F;
        float base = getBaseChance(level);

        // 模板加成
        float templateBonus = calculateMaterialBonus(templateCount, 0.03F, 0.05F);

        // 保护石加成（只有当保护石存在时）
        float protectionStoneBonus = 0.0F;
        if (protectionStone > 0) {
            protectionStoneBonus = calculateMaterialBonus(protectionStone, PROTECTION_STONE_BASE, PROTECTION_STONE_DECAY);
        }

        // 第二个材料槽加成（根据材料类型）
        float material2Bonus = 0.0F;
        if (material2Type != null) {
            if (material2Type == Items.DIAMOND) {
                material2Bonus = calculateMaterialBonus(material2Count, DIAMOND_BASE, DIAMOND_DECAY);
            } else if (material2Type == Items.NAUTILUS_SHELL) {
                material2Bonus = calculateMaterialBonus(material2Count, NAUTILUS_SHELL_BASE, NAUTILUS_SHELL_DECAY);
            } else if (material2Type == Items.NETHERITE_INGOT) {
                material2Bonus = calculateMaterialBonus(material2Count, NETHERITE_INGOT_BASE, NETHERITE_INGOT_DECAY);
            } else if (material2Type == Items.NETHER_STAR) {
                material2Bonus = calculateMaterialBonus(material2Count, NETHER_STAR_BASE, NETHER_STAR_DECAY);
            } else if (material2Type == Items.DRAGON_EGG) {
                material2Bonus = calculateMaterialBonus(material2Count, DRAGON_EGG_BASE, DRAGON_EGG_DECAY);
            }
        }

        return base + templateBonus + protectionStoneBonus + material2Bonus;
    }

    // 从两个材料槽中消耗指定数量的材料
    private void consumeMaterialFromSlots(int amountToConsume, Item item) {
        int remaining = amountToConsume;

        // 先尝试从第一个材料槽消耗
        ItemStack slot1 = inv.getStack(SLOT_ADD_MAT1);
        if (!slot1.isEmpty() && slot1.isOf(item)) {
            int consumeFromSlot1 = Math.min(remaining, slot1.getCount());
            inv.removeStack(SLOT_ADD_MAT1, consumeFromSlot1);
            remaining -= consumeFromSlot1;
        }

        // 如果还需要更多，从第二个材料槽消耗
        if (remaining > 0) {
            ItemStack slot2 = inv.getStack(SLOT_ADD_MAT2);
            if (!slot2.isEmpty() && slot2.isOf(item)) {
                int consumeFromSlot2 = Math.min(remaining, slot2.getCount());
                inv.removeStack(SLOT_ADD_MAT2, consumeFromSlot2);
            }
        }
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
            } else if (stack.isOf(ModItems.SWORD_UPGRADE_TEMPLATE)) {
                if (!insertItem(stack, SLOT_TEMPLATE, SLOT_TEMPLATE + 1, false))
                    return ItemStack.EMPTY;
            } else if (stack.isOf(ModItems.PROTECTION_STONE)) {
                if (!insertItem(stack, SLOT_ADD_MAT1, SLOT_ADD_MAT1 + 1, false))
                    return ItemStack.EMPTY;
            } else if (stack.isOf(Items.DIAMOND) ||
                    stack.isOf(Items.NAUTILUS_SHELL) ||
                    stack.isOf(Items.NETHERITE_INGOT) ||
                    stack.isOf(Items.NETHER_STAR) ||
                    stack.isOf(Items.DRAGON_EGG)) {
                if (!insertItem(stack, SLOT_ADD_MAT2, SLOT_ADD_MAT2 + 1, false))
                    return ItemStack.EMPTY;
            } else return ItemStack.EMPTY;
        } else if (!insertItem(stack, 5, 41, true)) return ItemStack.EMPTY;

        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return copy;
    }
}
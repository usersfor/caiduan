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
                return stack.isOf(Items.DIAMOND);
            }
        });
        addSlot(new Slot(inv, SLOT_ADD_MAT2, 93, 17) {
            @Override public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.NETHERITE_INGOT);
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

    private float getCurrentChance(ItemStack weapon,
                                   ItemStack template,
                                   ItemStack mat1,
                                   ItemStack mat2) {
        int level = weapon.getOrCreateNbt().getInt("forge_level");
        if (level >= 99) return 0.0F;
        float base = getBaseChance(level);

        int templateCount = template.isEmpty() ? 0 : template.getCount();
        int netherite = 0, diamond = 0;
        for (ItemStack s : new ItemStack[]{mat1, mat2}) {
            if (!s.isEmpty()) {
                if (s.isOf(Items.NETHERITE_INGOT)) netherite += s.getCount();
                if (s.isOf(Items.DIAMOND))         diamond  += s.getCount();
            }
        }
        float bonus = templateCount * 0.03F
                + netherite * 0.02F
                + diamond   * 0.01F;
        return Math.min(base + bonus, 1.0F);
    }

    /* ============================== 预览 ============================== */
    private void updateResult() {
        ItemStack input = inv.getStack(SLOT_INPUT);
        ItemStack mat1  = inv.getStack(SLOT_ADD_MAT1);
        ItemStack mat2  = inv.getStack(SLOT_ADD_MAT2);
        ItemStack temp  = inv.getStack(SLOT_TEMPLATE);

        if (input.isEmpty() || mat1.isEmpty() || mat2.isEmpty()) {
            inv.setStack(SLOT_RESULT, ItemStack.EMPTY);
            return;
        }

        // 1. 复制物品，但只拷贝“匠魂需要”的字段，不整块覆盖
        ItemStack preview = input.copy();
        NbtCompound src = input.getOrCreateNbt();
        NbtCompound dst = preview.getOrCreateNbt();

        // 把匠魂核心字段搬过去
        dst.copyFrom(src);
        // 清空预览用的临时键
        dst.remove("PreviewDamage");
        dst.remove("PreviewArmor");
        dst.remove("PreviewChance");

        int nextLevel = dst.getInt("forge_level") + 1;

        if (isWeapon(preview)) {
            double base  = ItemUtil.getTrueBaseDamage(preview);
            double bonus = ItemUtil.getSwordBonus() * base * nextLevel;
            dst.putDouble("PreviewDamage", bonus);
        } else if (isArmor(preview)) {
            EquipmentSlot slot = getEquipmentSlot(preview);
            double base  = ItemUtil.getCleanBaseArmor(preview, slot);
            double bonus = ItemUtil.getArmorBonus() * base * nextLevel;
            dst.putDouble("PreviewArmor", bonus);
        }

        dst.putFloat("PreviewChance", getCurrentChance(input, temp, mat1, mat2));
        inv.setStack(SLOT_RESULT, preview);
    }

    /* ============================== 强化逻辑 ============================== */
    private void onCraft(PlayerEntity player) {
        ItemStack input = inv.getStack(SLOT_INPUT);
        ItemStack mat1  = inv.getStack(SLOT_ADD_MAT1);
        ItemStack mat2  = inv.getStack(SLOT_ADD_MAT2);
        ItemStack temp  = inv.getStack(SLOT_TEMPLATE);

        if (input.isEmpty() || mat1.isEmpty() || mat2.isEmpty()) return;

        inv.removeStack(SLOT_ADD_MAT1, 1);
        inv.removeStack(SLOT_ADD_MAT2, 1);
        if (!temp.isEmpty()) inv.removeStack(SLOT_TEMPLATE, 1);

        float chance = getCurrentChance(input, temp, mat1, mat2);
        boolean success = player.getRandom().nextFloat() < chance;

        if (success) {
            NbtCompound tag = input.getOrCreateNbt();
            int newLevel = tag.getInt("forge_level") + 1;
            tag.putInt("forge_level", newLevel);

            // ✅ 新增：写入基础值到 NBT，供客户端直接读取
            ItemUtil.writeBaseValuesToNbt(input);

            applyForgingBonus(input);   // 只追加 AttributeModifier
            player.sendMessage(Text.literal("强化成功！当前等级: " + newLevel), false);
        } else {
            inv.setStack(SLOT_INPUT, ItemStack.EMPTY);
            player.sendMessage(Text.literal("强化失败！"), false);
        }
        updateResult();}


    /* 读取指定 UUID 的 AttributeModifier 值，不存在则返回 0 */
    private static double getExistingBonus(ItemStack stack,
                                           EntityAttribute attribute,
                                           UUID uuid,
                                           EquipmentSlot slot) {
        var modifiers = stack.getAttributeModifiers(slot).get(attribute);
        if (modifiers != null) {
            for (var mod : modifiers) {
                if (mod.getId().equals(uuid)) {
                    return mod.getValue();
                }
            }
        }
        return 0;
    }
    /* ============================== 属性加成 ============================== */
    private void applyForgingBonus(ItemStack stack) {
        if (isWeapon(stack)) {
            double base  = ItemUtil.getTrueBaseDamage(stack);
            double old   = getExistingBonus(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    SWORD_BONUS_UUID, EquipmentSlot.MAINHAND);
            double bonus = old + ItemUtil.getSwordBonus() * base; // 累加

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
            double oldArmor  = getExistingBonus(stack, EntityAttributes.GENERIC_ARMOR,
                    ARMOR_BONUS_UUID, slot);
            double armorAdd  = oldArmor + ItemUtil.getArmorBonus() * baseArmor;
            removeSingleModifier(stack, EntityAttributes.GENERIC_ARMOR,
                    ARMOR_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_ARMOR,
                    new EntityAttributeModifier(ARMOR_BONUS_UUID, "forge_bonus",
                            armorAdd, EntityAttributeModifier.Operation.ADDITION),
                    slot);

            // 护甲韧性
            double baseTough = ItemUtil.getCleanBaseToughness(stack, slot);
            double oldTough  = getExistingBonus(stack, EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                    TOUGH_BONUS_UUID, slot);
            double toughAdd  = oldTough + ItemUtil.getArmorBonus() * baseTough;
            removeSingleModifier(stack, EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                    TOUGH_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                    new EntityAttributeModifier(TOUGH_BONUS_UUID, "forge_bonus",
                            toughAdd, EntityAttributeModifier.Operation.ADDITION),
                    slot);

            // 抗击退
            double baseKbRes = ItemUtil.getCleanBaseKnockbackRes(stack, slot);
            double oldKbRes  = getExistingBonus(stack, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    KB_RES_BONUS_UUID, slot);
            double kbResAdd  = oldKbRes + ItemUtil.getArmorBonus() * baseKbRes;
            removeSingleModifier(stack, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    KB_RES_BONUS_UUID, slot);
            stack.addAttributeModifier(
                    EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    new EntityAttributeModifier(KB_RES_BONUS_UUID, "forge_bonus",
                            kbResAdd, EntityAttributeModifier.Operation.ADDITION),
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
            } else if (stack.isOf(Items.DIAMOND)) {
                if (!insertItem(stack, SLOT_ADD_MAT1, SLOT_ADD_MAT1 + 1, false))
                    return ItemStack.EMPTY;
            } else if (stack.isOf(Items.NETHERITE_INGOT)) {
                if (!insertItem(stack, SLOT_ADD_MAT2, SLOT_ADD_MAT2 + 1, false))
                    return ItemStack.EMPTY;
            } else return ItemStack.EMPTY;
        } else if (!insertItem(stack, 5, 41, true)) return ItemStack.EMPTY;

        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return copy;
    }
}
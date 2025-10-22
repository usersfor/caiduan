package com.example.screen;

import com.example.TemplateMod;
import com.example.item.ModItems;
import com.example.util.IronUtil;
import com.example.util.RemoveUtil;
import com.example.util.LevelUtil;
import com.example.util.AddUtil;
import net.minecraft.entity.EquipmentSlot;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;


public class ForgingScreenHandler extends ScreenHandler {

    private final Inventory inv = new SimpleInventory(4); // 减少到4个槽位

    private static final int SLOT_INPUT  = 0;
    private static final int SLOT_MAT1   = 1;
    private static final int SLOT_MAT2   = 2;
    private static final int SLOT_RESULT = 3;

    // 玩家属性存储
    private float tidalRate = 0.0f;    // 潮律
    private float starMark = 0.0f;     // 星痕
    private float mountainStand = 0.0f; // 山屹
    private float moonReflection = 0.0f; // 月映
    private float cloudRoam = 0.0f;    // 云巡
    private float skyBalance = 0.0f;   // 天衡

    // 特殊材料的基础加成和衰减率 - 使用您的强化率数据
    private static final Map<Item, IronUtil.MaterialData> SPECIAL_MATERIAL_DATA = new HashMap<>();
    static {
        SPECIAL_MATERIAL_DATA.put(ModItems.SWORD_UPGRADE_TEMPLATE, new IronUtil.MaterialData(0.03F, 0.05F));
        SPECIAL_MATERIAL_DATA.put(ModItems.PROTECTION_STONE, new IronUtil.MaterialData(0.04F, 0.04F));
        SPECIAL_MATERIAL_DATA.put(Items.DIAMOND, new IronUtil.MaterialData(0.01F, 0.1F));
        SPECIAL_MATERIAL_DATA.put(Items.NAUTILUS_SHELL, new IronUtil.MaterialData(0.015F, 0.09F));
        SPECIAL_MATERIAL_DATA.put(Items.NETHERITE_INGOT, new IronUtil.MaterialData(0.02F, 0.07F));
        SPECIAL_MATERIAL_DATA.put(Items.NETHER_STAR, new IronUtil.MaterialData(0.025F, 0.06F));
        SPECIAL_MATERIAL_DATA.put(Items.DRAGON_EGG, new IronUtil.MaterialData(0.03F, 0.05F));
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

    /* ============================== 构造 ============================== */
    public ForgingScreenHandler(int syncId,
                                PlayerInventory playerInv,
                                ScreenHandlerContext context) {
        super(TemplateMod.FORGING_SCREEN_HANDLER, syncId);

        addSlot(new Slot(inv, SLOT_INPUT,  220,  10){
            @Override public boolean canInsert(ItemStack stack) {
                return isWeapon(stack) || isArmor(stack);
            }
        });
        // 在 ForgingScreenHandler.java 中修改材料槽位

        addSlot(new Slot(inv, SLOT_MAT1, 220, 40) {
            @Override
            public boolean canInsert(ItemStack stack) {
                // 统一材料槽位 - 允许所有有效材料
                return isValidMaterial(stack) && this.getStack().isEmpty(); // 只能放入空槽位
            }

            @Override
            public int getMaxItemCount() {
                return 1; // 限制最大堆叠数为1
            }

            @Override
            public int getMaxItemCount(ItemStack stack) {
                return 1; // 限制最大堆叠数为1
            }
        });

        addSlot(new Slot(inv, SLOT_MAT2, 220, 70) {
            @Override
            public boolean canInsert(ItemStack stack) {
                // 统一材料槽位 - 允许所有有效材料
                return isValidMaterial(stack) && this.getStack().isEmpty(); // 只能放入空槽位
            }

            @Override
            public int getMaxItemCount() {
                return 1; // 限制最大堆叠数为1
            }

            @Override
            public int getMaxItemCount(ItemStack stack) {
                return 1; // 限制最大堆叠数为1
            }
        });
        addSlot(new Slot(inv, SLOT_RESULT, 220, 100) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public void onTakeItem(PlayerEntity player, ItemStack stack) {
                inv.setStack(SLOT_RESULT, ItemStack.EMPTY);
                if (!player.getWorld().isClient) onCraft(player);
                player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            }
        });


        // 主背包 (3x9)
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 9; x++)
                addSlot(new Slot(playerInv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 + 40));

        for (int x = 0; x < 9; x++)
            addSlot(new Slot(playerInv, x, 8 + x * 18, 142 + 40));

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

    /* ============================== 玩家属性相关 ============================== */

    // 设置玩家属性（应该在打开界面时调用）
    public void setPlayerAttributes(float tidalRate, float starMark, float mountainStand,
                                    float moonReflection, float cloudRoam, float skyBalance) {
        this.tidalRate = tidalRate;
        this.starMark = starMark;
        this.mountainStand = mountainStand;
        this.moonReflection = moonReflection;
        this.cloudRoam = cloudRoam;
        this.skyBalance = skyBalance;
    }

    // 获取山屹提供的失败保护次数
    private int getMountainStandProtectionCount() {
        int protectionCount = (int) (mountainStand / 0.2f);
        return Math.min(protectionCount, 5); // 最多5次
    }

    // 获取潮律提供的跳级概率
    private float getTidalRateJumpChance() {
        return (tidalRate / 0.2f) * 0.001f; // 每0.2潮律增加0.1%跳级概率
    }

    // 获取月映提供的成功率加成
    private float getMoonReflectionSuccessBonus() {
        return moonReflection * 0.01f; // 月映越高，成功率加成越高
    }

    // 获取天衡提供的材料收益平衡
    private float getSkyBalanceMaterialBonus(float baseMaterialBonus) {
        if (baseMaterialBonus < 0.05f) { // 如果材料收益过低
            return baseMaterialBonus + (skyBalance / 0.2f) * 0.01f; // 每0.2天衡增强平衡加的数值
        }
        return baseMaterialBonus;
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

    // 检查是否为有效材料
    private boolean isValidMaterial(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 首先检查特殊材料
        if (SPECIAL_MATERIAL_DATA.containsKey(stack.getItem())) {
            return true;
        }

        // 然后检查 IronUtil 中的材料
        return IronUtil.isValidMaterial(stack);
    }

    // 获取材料的加成数据
    private IronUtil.MaterialData getMaterialData(Item item) {
        // 首先检查特殊材料
        if (SPECIAL_MATERIAL_DATA.containsKey(item)) {
            return SPECIAL_MATERIAL_DATA.get(item);
        }

        // 然后从 IronUtil 中获取材料数据
        return IronUtil.getMaterialData(item);
    }

    // 计算材料加成
    private float calculateMaterialBonus(int count, float baseBonus, float decayRate) {
        if (count <= 0) return 0;

        float totalBonus = 0;
        for (int i = 1; i <= count; i++) {
            totalBonus += baseBonus * (1.0F - (i - 1) * decayRate);
        }
        return totalBonus;
    }

    /* ============================== 预览 ============================== */
    private void updateResult() {
        ItemStack input = inv.getStack(SLOT_INPUT);
        ItemStack mat1  = inv.getStack(SLOT_MAT1);
        ItemStack mat2  = inv.getStack(SLOT_MAT2);

        if (input.isEmpty() || (mat1.isEmpty() && mat2.isEmpty())) {
            inv.setStack(SLOT_RESULT, ItemStack.EMPTY);
            return;
        }

        // 计算材料总数
        Map<Item, Integer> materialCounts = new HashMap<>();
        countMaterials(mat1, materialCounts);
        countMaterials(mat2, materialCounts);

        // 1. 复制物品
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
            double base = RemoveUtil.getTrueBaseDamage(preview);
            double currentMultiplier = LevelUtil.getBonusMultiplier(currentLevel);
            double nextMultiplier = LevelUtil.getBonusMultiplier(nextLevel);
            // 计算下一级相比当前级增加的伤害值
            double bonusIncrease = base * (nextMultiplier - currentMultiplier);
            dst.putDouble("PreviewDamage", bonusIncrease);
        } else if (isArmor(preview)) {
            EquipmentSlot slot = getEquipmentSlot(preview);
            double base = RemoveUtil.getCleanBaseArmor(preview);
            double currentMultiplier = LevelUtil.getBonusMultiplier(currentLevel);
            double nextMultiplier = LevelUtil.getBonusMultiplier(nextLevel);
            // 计算下一级相比当前级增加的护甲值
            double bonusIncrease = base * (nextMultiplier - currentMultiplier);
            dst.putDouble("PreviewArmor", bonusIncrease);
        }

        dst.putFloat("PreviewChance", getCurrentChance(input, materialCounts));
        inv.setStack(SLOT_RESULT, preview);
    }

    /* ============================== 强化逻辑 ============================== */
    private void onCraft(PlayerEntity player) {
        ItemStack input = inv.getStack(SLOT_INPUT);
        ItemStack mat1 = inv.getStack(SLOT_MAT1);
        ItemStack mat2 = inv.getStack(SLOT_MAT2);

        if (input.isEmpty() || (mat1.isEmpty() && mat2.isEmpty())) return;

        // 计算材料总数
        Map<Item, Integer> materialCounts = new HashMap<>();
        countMaterials(mat1, materialCounts);
        countMaterials(mat2, materialCounts);

        // 消耗材料
        consumeMaterials(materialCounts);

        float chance = getCurrentChance(input, materialCounts);

        // 应用月映成功率加成
        chance += getMoonReflectionSuccessBonus();

        boolean success = player.getRandom().nextFloat() < chance;

        // 检查山屹保护
        boolean usedProtection = false;
        int protectionCount = getMountainStandProtectionCount();

        if (success) {
            NbtCompound tag = input.getOrCreateNbt();
            int newLevel = tag.getInt("forge_level") + 1;

            // 检查潮律跳级
            float jumpChance = getTidalRateJumpChance();
            if (player.getRandom().nextFloat() < jumpChance && newLevel < 99) {
                newLevel += 1;
                player.sendMessage(Text.literal("潮律之力！强化跳级！当前等级: " + newLevel), false);
            }

            tag.putInt("forge_level", newLevel);

            // ✅ 新增：写入基础值到 NBT，供客户端直接读取
            RemoveUtil.writeBaseValuesToNbt(input);

            // 应用星痕强化收益加成（传入player.getRandom()）
            AddUtil.applyStarMarkBonus(input, starMark, isWeapon(input), player.getRandom());

            // 应用云巡药水效果
            AddUtil.applyCloudRoamEffects(input, cloudRoam);

            AddUtil.applyForgingBonus(input);  // 只追加 AttributeModifier
            player.sendMessage(Text.literal("强化成功！当前等级: " + newLevel), false);
        } else{
            NbtCompound tag = input.getOrCreateNbt();
            int currentLevel = tag.getInt("forge_level");

            // 检查山屹保护
            if (protectionCount > 0) {
                usedProtection = true;
                player.sendMessage(Text.literal("山屹之力保护了你的装备！"), false);
                // 不执行任何惩罚，只是强化失败
            } else {
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
                    AddUtil.applyForgingBonus(input);
                    player.sendMessage(Text.literal("强化失败！武器等级下降了3级！当前等级: " + newLevel), false);

                    // 更新物品栈以触发客户端同步
                    inv.setStack(SLOT_INPUT, input);
                } else {
                    // 12级以下失败清除武器（原逻辑）
                    inv.setStack(SLOT_INPUT, ItemStack.EMPTY);
                    player.sendMessage(Text.literal("强化失败！"), false);
                }
            }
        }
        updateResult();
    }

    // 计算材料数量
    private void countMaterials(ItemStack stack, Map<Item, Integer> counts) {
        if (!stack.isEmpty() && isValidMaterial(stack)) {
            Item item = stack.getItem();
            counts.put(item, counts.getOrDefault(item, 0) + stack.getCount());
        }
    }

    // 消耗材料
    private void consumeMaterials(Map<Item, Integer> materialCounts) {
        for (Map.Entry<Item, Integer> entry : materialCounts.entrySet()) {
            Item item = entry.getKey();
            int count = entry.getValue();

            // 从两个材料槽中消耗材料
            consumeFromSlot(SLOT_MAT1, item, count);
            if (count > 0) {
                consumeFromSlot(SLOT_MAT2, item, count);
            }
        }
    }

    // 从指定槽位消耗材料
    private void consumeFromSlot(int slot, Item item, int count) {
        ItemStack stack = inv.getStack(slot);
        if (!stack.isEmpty() && stack.getItem() == item) {
            int toConsume = Math.min(count, stack.getCount());
            inv.removeStack(slot, toConsume);
            count -= toConsume;
        }
    }

    // 计算当前成功率
    private float getCurrentChance(ItemStack weapon, Map<Item, Integer> materialCounts) {
        int level = weapon.getOrCreateNbt().getInt("forge_level");
        if (level >= 99) return 0.0F;
        float base = getBaseChance(level);
        float totalBonus = 0.0F;

        // 计算所有材料的加成
        for (Map.Entry<Item, Integer> entry : materialCounts.entrySet()) {
            Item item = entry.getKey();
            int count = entry.getValue();
            IronUtil.MaterialData data = getMaterialData(item);

            // 应用天衡材料收益平衡
            float materialBonus = calculateMaterialBonus(count, data.baseBonus, data.decayRate);
            materialBonus = getSkyBalanceMaterialBonus(materialBonus);

            totalBonus += materialBonus;
        }

        return base + totalBonus;
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

        if (index >= 4) { // 玩家背包槽位
            if (isWeapon(stack) || isArmor(stack)) {
                if (!insertItem(stack, SLOT_INPUT, SLOT_INPUT + 1, false))
                    return ItemStack.EMPTY;
            } else if (isValidMaterial(stack)) {
                // 尝试放入材料槽，但只能放入空槽位
                boolean inserted = false;
                if (inv.getStack(SLOT_MAT1).isEmpty()) {
                    if (insertItem(stack, SLOT_MAT1, SLOT_MAT1 + 1, false)) {
                        inserted = true;
                    }
                }
                if (!inserted && inv.getStack(SLOT_MAT2).isEmpty()) {
                    if (insertItem(stack, SLOT_MAT2, SLOT_MAT2 + 1, false)) {
                        inserted = true;
                    }
                }
                if (!inserted) {
                    return ItemStack.EMPTY;
                }
            } else return ItemStack.EMPTY;
        } else if (!insertItem(stack, 4, 40, true)) return ItemStack.EMPTY;

        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return copy;
    }
}
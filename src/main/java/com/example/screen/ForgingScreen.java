package com.example.screen;

import com.example.util.LevelUtil;
import com.example.util.RemoveUtil;
import com.example.util.TetraUtil;
import com.example.util.TicUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public class ForgingScreen extends HandledScreen<ForgingScreenHandler> {

    // 新的复合属性定义 - 严格1:1对应
    private final String[] talents = {"潮律", "星痕", "山屹", "月映", "云巡", "天衡"};
    private float[] talentValues = new float[6]; // 动态数据
    private final int[] talentColors = {
            0xFF9933FF, // 潮律 - 潮汐紫
            0xFF3399FF, // 星痕 - 星空蓝
            0xFF996633, // 山屹 - 大地棕
            0xFFFFCC00, // 月映 - 月光金
            0xFF66CCFF, // 云巡 - 云朵蓝
            0xFFFF3300  // 天衡 - 临界红
    };

    // 武器权重矩阵 - 简化版本，只保留必要的权重
    private static final float[][] WEAPON_WEIGHTS = {
            // 潮律：生命 - 完全由玩家的最大生命值决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 星痕：攻击 - 完全由武器的攻击力决定
            {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 山屹：防御 - 完全由玩家的实体范围决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 月映：魔法 - 完全由玩家的魔法强度
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 云巡：移动速度 - 完全由玩家的移速决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 天衡：强化等级 - 完全由装备的强化等级决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f}
    };

    // 护甲权重矩阵 - 简化版本，只保留必要的权重
    private static final float[][] ARMOR_WEIGHTS = {
            // 潮律：生命 - 完全由玩家的最大生命值决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 星痕：攻击 - 完全由玩家的方块范围决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 山屹：防御 - 完全由装备的护甲和护甲韧性决定
            {0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f},
            // 月映：魔法 - 完全由玩家的魔法强度决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 云巡：移动速度 - 完全由玩家的移速决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            // 天衡：强化等级 - 完全由装备的强化等级决定
            {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f}
    };

    // 获取装备权重 - 与权重矩阵保持一致
    private float getEquipmentWeight(int talentIndex, boolean isWeapon) {
        if (isWeapon) {
            switch (talentIndex) {
                case 0: // 潮律：生命 - 完全由玩家的最大生命值决定
                case 2: // 山屹：防御 - 完全由玩家的实体范围决定
                case 3: // 月映：魔法 - 完全由玩家的魔法强度
                case 4: // 云巡：移动速度 - 完全由玩家的移速决定
                    return 0.0f;
                case 1: // 星痕：攻击 - 完全由武器的攻击力决定
                case 5: // 天衡：强化等级 - 完全由装备的强化等级决定
                    return 1.0f;
                default:
                    return 0.5f;
            }
        } else {
            // 护甲
            switch (talentIndex) {
                case 0: // 潮律：生命 - 完全由玩家的最大生命值决定
                case 1: // 星痕：攻击 - 完全由玩家的方块范围决定
                case 3: // 月映：魔法 - 完全由玩家的魔法强度决定
                case 4: // 云巡：移动速度 - 完全由玩家的移速决定
                    return 0.0f;
                case 2: // 山屹：防御 - 完全由装备的护甲和护甲韧性决定
                case 5: // 天衡：强化等级 - 完全由装备的强化等级决定
                    return 1.0f;
                default:
                    return 0.5f;
            }
        }
    }

    // 雷达图渲染器
    private RadarChartRenderer radarChart;

    public ForgingScreen(ForgingScreenHandler handler,
                         PlayerInventory inventory,
                         Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 256;
        this.backgroundHeight = 220;
        Arrays.fill(talentValues, 0f);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        this.titleY = 5;
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        // 初始化雷达图渲染器
        this.radarChart = new RadarChartRenderer(
                this.textRenderer,
                this.talents,
                this.talentColors
        );

        // 设置雷达图显示参数
        this.radarChart.setDisplayParams(0.35f, 0.6f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        // 更新雷达图动画
        if (radarChart != null) {
            radarChart.updateAnimations();
        }

        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    // 获取玩家所有属性值 - 修复月映计算
    private float[] getPlayerAttributeValues() {
        float[] playerAttrs = new float[6];
        var player = this.client.player;

        // 0. 潮律 - 生命值比率
        var maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        float maxHealth = maxHealthAttr != null ? (float) maxHealthAttr.getValue() : 20f;
        playerAttrs[0] = Math.min(maxHealth / 100f, 1f);

        // 1. 星痕 - 攻击相关（伤害+攻速）
        var attackDamageAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var attackSpeedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        float attackDamage = attackDamageAttr != null ? (float) attackDamageAttr.getBaseValue() : 1f;
        float attackSpeed = attackSpeedAttr != null ? (float) attackSpeedAttr.getValue() : 1.0f;
        playerAttrs[1] = Math.min(Math.max(attackDamage / 20f, attackSpeed / 4f), 1f);

        // 2. 山屹 - 防御相关（护甲+韧性）
        var armorAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        var toughnessAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        float armorValue = armorAttr != null ? (float) armorAttr.getValue() : 0f;
        float toughnessValue = toughnessAttr != null ? (float) toughnessAttr.getValue() : 0f;
        playerAttrs[2] = Math.min((armorValue + toughnessValue) / 30f, 1f);

        // 3. 月映 - 魔法强度（修复经验等级计算）
        float magicPower = 0f;
        boolean hasMagicMod = false;

        try {
            String[] spellTypes = {"fire", "ice", "lightning", "holy", "ender", "blood", "evocation", "nature"};
            for (String spellType : spellTypes) {
                Identifier attrId = new Identifier("irons_spellbooks", spellType + "_spell_power");
                var spellPowerAttr = Registries.ATTRIBUTE.get(attrId);

                if (spellPowerAttr != null) {
                    // 确保属性实例存在，如果不存在则创建
                    var attrInstance = player.getAttributeInstance(spellPowerAttr);
                    if (attrInstance == null) {
                        // 尝试获取或创建属性实例
                        attrInstance = player.getAttributes().getCustomInstance(spellPowerAttr);
                    }

                    if (attrInstance != null && attrInstance.getValue() > 0) {
                        magicPower += (float) attrInstance.getValue();
                        hasMagicMod = true;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }

        // 如果没有找到魔法模组属性，使用经验等级
        if (!hasMagicMod || magicPower == 0) {
            magicPower = player.experienceLevel;
            System.out.println("使用经验等级作为魔法强度: " + magicPower);
        }

        playerAttrs[3] = Math.min(magicPower / 100f, 1f);

        // 4. 云巡 - 移动速度，初始值设为10%
        var movementSpeedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        float movementSpeed = movementSpeedAttr != null ? (float) movementSpeedAttr.getValue() : 0.1f;
        // 设置基础值为10%，即使没有额外加成也能显示
        playerAttrs[4] = 0.1f + Math.min((movementSpeed - 0.1f) / 0.3f, 0.9f);

        // 5. 天衡 - 强化等级（玩家无基础强化，为0）
        playerAttrs[5] = 0f;

        // 调试输出玩家属性
        System.out.println("=== 玩家属性调试 ===");
        System.out.println("经验等级: " + player.experienceLevel);
        System.out.println("魔法强度: " + magicPower);
        System.out.println("月映属性: " + playerAttrs[3]);
        System.out.println("移动速度: " + movementSpeed);
        System.out.println("云巡属性: " + playerAttrs[4]);
        System.out.println("玩家属性数组: " + Arrays.toString(playerAttrs));

        return playerAttrs;
    }

    // 获取武器攻击速度
    private float getWeaponAttackSpeed(ItemStack stack) {
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        for (var modifier : modifiers.get(EntityAttributes.GENERIC_ATTACK_SPEED)) {
            return (float) modifier.getValue();
        }

        var item = stack.getItem();
        if (item instanceof net.minecraft.item.SwordItem) return 1.6f;
        if (item instanceof net.minecraft.item.AxeItem) return 1.0f;
        if (item instanceof net.minecraft.item.TridentItem) return 1.1f;
        return 1.4f;
    }

    // 结合玩家属性的复合属性计算
    private float[] calculateCompositeValuesWithPlayerAttributes(float[] baseValues, float[] playerAttributes, float[][] weights, boolean isWeapon) {
        float[] composite = new float[6];

        for (int i = 0; i < 6; i++) {
            float equipmentContribution = 0f;

            // 计算装备贡献
            for (int j = 0; j < 6; j++) {
                equipmentContribution += baseValues[j] * weights[i][j];
            }

            // 获取对应的玩家属性贡献
            float playerContribution = playerAttributes[i];

            // 根据复合元素类型调整权重
            float equipmentWeight = getEquipmentWeight(i, isWeapon);
            float playerWeight = 1f - equipmentWeight;

            // 合并贡献
            composite[i] = equipmentContribution * equipmentWeight + playerContribution * playerWeight;
            composite[i] = Math.min(Math.max(composite[i], 0f), 1f);
        }

        return composite;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int startX = (this.width - this.backgroundWidth) / 2;
        int startY = (this.height - this.backgroundHeight) / 2;

        updateTalentValues();
        context.fill(startX, startY, startX + backgroundWidth, startY + backgroundHeight, 0xCC1A1A1A);

        if (radarChart != null) {
            // 响应式布局：根据屏幕尺寸调整雷达图位置和大小
            int centerX = startX + backgroundWidth / 2;
            int centerY = startY + 60;

            // 动态计算雷达图半径，确保在不同屏幕尺寸下都能正常显示
            int maxRadius = Math.min(60, Math.min(this.width, this.height) / 6);

            radarChart.setPosition(centerX, centerY, maxRadius);
            radarChart.setBounds(
                    startX + 5,
                    startX + backgroundWidth - 5,
                    startY + 5,
                    startY + backgroundHeight - 5
            );

            // 使用动画值而不是直接设置
            float[] currentValues = getDynamicTalentValues();
            if (currentValues != null) {
                radarChart.setValues(currentValues);
            } else {
                radarChart.setValues(new float[6]);
            }

            radarChart.render(context, mouseX, mouseY);
        }

        context.drawBorder(startX, startY, backgroundWidth, backgroundHeight, 0xFF555555);
        drawItemStats(context, startX, startY);
    }

    private void updateTalentValues() {
        if (handler != null) {
            float[] newValues = getDynamicTalentValues();
            if (newValues != null) {
                // 使用雷达图内部的动画系统，不再需要外部插值
                for (int i = 0; i < talentValues.length; i++) {
                    talentValues[i] = newValues[i];
                }
            } else {
                Arrays.fill(talentValues, 0f);
            }
        }
    }

    private float[] getDynamicTalentValues() {
        ItemStack resultStack = handler.getSlot(3).getStack();
        if (!resultStack.isEmpty()) {
            return calculateTalentValues(resultStack);
        }

        ItemStack inputStack = handler.getSlot(0).getStack();
        if (!inputStack.isEmpty()) {
            return calculateTalentValues(inputStack);
        }

        return null;
    }

    private float mapAttributeToRadar(float rawValue, float maxRaw) {
        if (rawValue <= 0) return 0f;
        float ratio = rawValue / maxRaw;
        return MathHelper.clamp((float) Math.sqrt(ratio), 0f, 1f);
    }

    private float[] calculateTalentValues(ItemStack stack) {
        var nbt = stack.getNbt();

        // 优先检查TiC和Tetra工具
        if (nbt != null && TicUtil.isTicTool(nbt)) {
            float[] playerAttributes = getPlayerAttributeValues();
            return TicUtil.calculateTicTalentValues(nbt, playerAttributes);
        }

        if (nbt != null && TetraUtil.isTetraTool(nbt)) {
            return TetraUtil.calculateTetraTalentValues(nbt);
        }

        // 获取玩家属性值
        float[] playerAttributes = getPlayerAttributeValues();

        // 原版物品计算逻辑
        float[] baseValues = new float[6];
        float[] compositeValues = new float[6];

        if (stack.isEmpty() || nbt == null) {
            return compositeValues;
        }

        int forgeLevel = nbt.getInt("forge_level");
        int maxDurability = stack.getMaxDamage();
        int currentDurability = maxDurability - stack.getDamage();

        if (handler.isWeapon(stack)) {
            // 武器基础属性计算 - 严格对应
            try {
                double baseDamage = RemoveUtil.getTrueBaseDamage(stack);
                double currentDamage = baseDamage * LevelUtil.getBonusMultiplier(forgeLevel);
                baseValues[0] = mapAttributeToRadar((float) currentDamage, 9999f); // 攻击力
            } catch (Exception e) {
                baseValues[0] = 0f;
            }

            try {
                float attackSpeed = getWeaponAttackSpeed(stack);
                baseValues[3] = mapAttributeToRadar(attackSpeed, 9999f); // 攻速
            } catch (Exception e) {
                baseValues[3] = 0f;
            }

            // 潮律：强化等级
            baseValues[5] = Math.min(forgeLevel * 0.1f, 1.0f);

            // 使用武器权重计算复合属性
            compositeValues = calculateCompositeValuesWithPlayerAttributes(baseValues, playerAttributes, WEAPON_WEIGHTS, true);

        } else if (handler.isArmor(stack)) {
            // 护甲基础属性计算 - 严格对应
            try {
                double baseArmor = RemoveUtil.getCleanBaseArmor(stack);
                double currentArmor = baseArmor * LevelUtil.getBonusMultiplier(forgeLevel);
                baseValues[1] = mapAttributeToRadar((float) currentArmor, 999f); // 护甲值
            } catch (Exception e) {
                baseValues[1] = 0f;
            }

            try {
                double baseToughness = RemoveUtil.getCleanBaseToughness(stack);
                double currentToughness = baseToughness * LevelUtil.getBonusMultiplier(forgeLevel);
                baseValues[2] = mapAttributeToRadar((float) currentToughness, 999f); // 护甲韧性
            } catch (Exception e) {
                baseValues[2] = 0f;
            }

            // 云巡：生命值（从护甲属性获取）
            try {
                var modifiers = stack.getAttributeModifiers(EquipmentSlot.CHEST);
                for (var modifier : modifiers.get(EntityAttributes.GENERIC_MAX_HEALTH)) {
                    float healthBonus = (float) modifier.getValue();
                    baseValues[4] = mapAttributeToRadar(healthBonus, 999f);
                    break;
                }
            } catch (Exception e) {
                baseValues[4] = 0f;
            }

            // 潮律：强化等级
            baseValues[5] = Math.min(forgeLevel * 0.1f, 1.0f);

            // 使用护甲权重计算复合属性
            compositeValues = calculateCompositeValuesWithPlayerAttributes(baseValues, playerAttributes, ARMOR_WEIGHTS, false);
        }

        // 添加调试输出
        System.out.println("=== 属性计算调试 ===");
        System.out.println("物品: " + stack.getName().getString());
        System.out.println("是否武器: " + handler.isWeapon(stack));
        System.out.println("强化等级: " + forgeLevel);
        System.out.println("玩家属性: " + Arrays.toString(playerAttributes));
        System.out.println("基础值: " + Arrays.toString(baseValues));
        System.out.println("复合值: " + Arrays.toString(compositeValues));

        return compositeValues;
    }

    private void drawItemStats(DrawContext context, int startX, int startY) {
        ItemStack inputStack = handler.getSlot(0).getStack();
        ItemStack resultStack = handler.getSlot(3).getStack();

        if (inputStack.isEmpty()) {
            String prompt = "放入武器或护甲以查看属性";
            int textWidth = this.textRenderer.getWidth(prompt);
            int x = startX + (backgroundWidth - textWidth) / 2;
            int y = startY + 120;
            context.drawText(this.textRenderer, prompt, x, y, 0xFFFFFF, true);
            return;
        }

        var nbt = inputStack.getNbt();
        if (nbt == null) return;

        int forgeLevel = nbt.getInt("forge_level");
        int textY = startY + 120;
        int textColor = 0xFFFFFF;

        context.drawText(this.textRenderer, "强化等级: +" + forgeLevel, startX + 10, textY, textColor, false);

        if (!resultStack.isEmpty() && resultStack.hasNbt() && resultStack.getNbt().contains("PreviewChance")) {
            float chance = resultStack.getNbt().getFloat("PreviewChance");
            String chanceText = String.format("成功率: %.1f%%", chance * 100);
            context.drawText(this.textRenderer, chanceText, startX + 10, textY + 12, textColor, false);
        }

        if (!resultStack.isEmpty() && resultStack.hasNbt()) {
            var resultNbt = resultStack.getNbt();
            if (resultNbt.contains("PreviewDamage")) {
                double damageIncrease = resultNbt.getDouble("PreviewDamage");
                String damageText = String.format("伤害增加: +%.1f", damageIncrease);
                context.drawText(this.textRenderer, damageText, startX + 10, textY + 24, 0x00FF00, false);
            } else if (resultNbt.contains("PreviewArmor")) {
                double armorIncrease = resultNbt.getDouble("PreviewArmor");
                String armorText = String.format("护甲增加: +%.1f", armorIncrease);
                context.drawText(this.textRenderer, armorText, startX + 10, textY + 24, 0x00FF00, false);
            }
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
    }
}
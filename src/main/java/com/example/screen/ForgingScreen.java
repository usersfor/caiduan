package com.example.screen;

import com.example.util.LevelUtil;
import com.example.util.RemoveUtil;
import com.example.util.TetraUtil;
import com.example.util.TicUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
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
    private final String[] talents = {"水", "金", "土", "火", "木", "星"};
    private float[] talentValues = new float[6]; // 动态数据
    private final int[] talentColors = {
            0xFF0099FF, // 水 - 澈湖蓝
            0xFFFFD700, // 金 - 耀金黄
            0xFF8B4513, // 土 - 赭石棕
            0xFFFF4500, // 火 - 炽焰橙
            0xFF32CD32, // 木 - 新芽绿
            0xFFBB44FF  // 星 - 幻紫
    };

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

    //输出日志在聊天栏
    private static boolean CHAT_LOGGED = false;   // 只打印一次
    private void sayInChat(String text) {
        if (CHAT_LOGGED) return;          // 已经说过就跳过
        CHAT_LOGGED = true;
        // 1.20.1 必须用渲染线程调 addMessage
        RenderSystem.recordRenderCall(() ->
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal("§e[锻造血统]§r " + text))

        );
    }

    // 获取玩家所有属性值 - 统一公式版本
    private float[] getPlayerAttributeValues() {
        float[] playerAttrs = new float[6];
        var player = this.client.player;

        // 0. 水 - 生命值比率
        var maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        float maxHealth = maxHealthAttr != null ? (float) maxHealthAttr.getValue() : 20f;
        playerAttrs[0] = Math.min(maxHealth / 100f, 1f);

        // 1. 金 - 攻击相关（伤害+攻速）- 玩家部分设为0，完全由装备决定
        playerAttrs[1] = 0f;

        // 2. 土 - 防御相关 - 玩家部分设为0，完全由装备决定
        playerAttrs[2] = 0f;

        // 3. 火 - 魔法强度（修复经验等级计算）
        float magicPower = 0f;
        boolean hasMagicMod = false;

        try {
            String[] spellTypes = {"fire", "ice", "lightning", "holy", "ender", "blood", "evocation", "nature"};
            for (String spellType : spellTypes) {
                Identifier attrId = new Identifier("irons_spellbooks", spellType + "_spell_power");
                var spellPowerAttr = Registries.ATTRIBUTE.get(attrId);

                if (spellPowerAttr != null) {
                    var attrInstance = player.getAttributeInstance(spellPowerAttr);
                    if (attrInstance == null) {
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
        }

        playerAttrs[3] = Math.min(magicPower / 100f, 1f);

        // 4. 木 - 移动速度，初始值设为10%
        var movementSpeedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        float movementSpeed = movementSpeedAttr != null ? (float) movementSpeedAttr.getValue() : 0.1f;
        playerAttrs[4] = 0.1f + Math.min((movementSpeed - 0.1f) / 0.3f, 0.9f);

        // 5. 星 - 强化等级（玩家无基础强化，为0）
        playerAttrs[5] = 0f;

        sayInChat(
                String.format(
                        "水-HP: %.2f | 金-攻: %.2f | 土-防: %.2f | 火-魔: %.2f | 木-速: %.2f | 星-强: %.2f\n" +
                                "原始值→ 生命: %.1f 移速: %.3f 经验: %d 魔法: %.1f",
                        playerAttrs[0], playerAttrs[1], playerAttrs[2], playerAttrs[3], playerAttrs[4], playerAttrs[5],
                        maxHealth, movementSpeed, player.experienceLevel, magicPower
                )
        );
        return playerAttrs;
    }

    // 获取武器基础攻击速度（原版默认值）
    private float getBaseWeaponAttackSpeed(ItemStack stack) {
        var item = stack.getItem();
        if (item instanceof net.minecraft.item.SwordItem) return 1.6f;
        if (item instanceof net.minecraft.item.AxeItem) return 1.0f;
        if (item instanceof net.minecraft.item.TridentItem) return 1.1f;
        if (item instanceof net.minecraft.item.PickaxeItem) return 1.2f;
        if (item instanceof net.minecraft.item.ShovelItem) return 1.0f;
        if (item instanceof net.minecraft.item.HoeItem) return 1.0f;
        return 1.4f; // 默认值
    }

    // 获取武器实际攻击速度（包含所有修饰符）
    private float getWeaponAttackSpeed(ItemStack stack) {
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        for (var modifier : modifiers.get(EntityAttributes.GENERIC_ATTACK_SPEED)) {
            return (float) modifier.getValue();
        }
        return getBaseWeaponAttackSpeed(stack);
    }

    // 获取武器攻速加成（只计算超出基础值的部分）
    private float getWeaponAttackSpeedBonus(ItemStack stack) {
        float baseSpeed = getBaseWeaponAttackSpeed(stack);
        float actualSpeed = getWeaponAttackSpeed(stack);
        return Math.max(0, actualSpeed - baseSpeed);
    }

    // 统一计算装备的基础属性
    private float[] calculateEquipmentBaseValues(ItemStack stack, int forgeLevel) {
        float[] baseValues = new float[6];
        var nbt = stack.getNbt();
        if (nbt == null) return baseValues;

        try {
            // 获取基础属性
            double baseDamage = RemoveUtil.getTrueBaseDamage(stack);
            double currentDamage = baseDamage * LevelUtil.getBonusMultiplier(forgeLevel);
            float attackValue = (float) currentDamage;

            // 只计算超出基础值的攻速加成
            float attackSpeedBonus = getWeaponAttackSpeedBonus(stack);

            double baseArmor = RemoveUtil.getCleanBaseArmor(stack);
            double currentArmor = baseArmor * LevelUtil.getBonusMultiplier(forgeLevel);
            float armorValue = (float) currentArmor;

            double baseToughness = RemoveUtil.getCleanBaseToughness(stack);
            double currentToughness = baseToughness * LevelUtil.getBonusMultiplier(forgeLevel);
            float toughnessValue = (float) currentToughness;

            // 统一计算公式
            // 水：100% 玩家最大生命值（装备部分为0）
            baseValues[0] = 0f;

            // 金：100% 装备（基础攻击力和韧性取最大值）
            baseValues[1] = mapAttributeToRadar(Math.max(attackValue, toughnessValue), 5000f);

            // 土：100% 装备（护甲值和攻速加成取最大值）
            baseValues[2] = mapAttributeToRadar(Math.max(armorValue, attackSpeedBonus), 500f);

            // 火：100% 装备（魔法属性或经验等级）- 使用玩家属性中的火值
            float[] playerAttrs = getPlayerAttributeValues();
            baseValues[3] = playerAttrs[3]; // 直接使用玩家属性中的火值

            // 木：100% 玩家移动速度（装备部分为0）
            baseValues[4] = 0f;

            // 星：100% 装备强化等级
            baseValues[5] = Math.min(forgeLevel * 0.1f, 1.0f);

        } catch (Exception e) {
            Arrays.fill(baseValues, 0f);
        }

        return baseValues;
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

            float[] currentValues = getDynamicTalentValues();
            if (currentValues != null) {
                System.out.println("【雷达图输入值】：" + java.util.Arrays.toString(currentValues));
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

    private boolean hasPrintedPlayerDebug = false;
    private float[] getDynamicTalentValues() {
        ItemStack inputStack = handler.getSlot(0).getStack();
        ItemStack resultStack = handler.getSlot(3).getStack();

        ItemStack targetStack = !resultStack.isEmpty() ? resultStack : !inputStack.isEmpty() ? inputStack : ItemStack.EMPTY;

        // ✅ 检测到"放入物品"的瞬间
        if (!targetStack.isEmpty() && !hasPrintedPlayerDebug) {
            hasPrintedPlayerDebug = true; // 只允许打印一次
        } else if (targetStack.isEmpty()) {
            hasPrintedPlayerDebug = false; // 物品被拿走，重置标记
        }

        if (!resultStack.isEmpty()) {
            return calculateTalentValues(resultStack);
        } else if (!inputStack.isEmpty()) {
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

        if (stack.isEmpty() || nbt == null) {
            return new float[6];
        }

        int forgeLevel = nbt.getInt("forge_level");

        // 使用统一公式计算装备基础值
        float[] equipmentValues = calculateEquipmentBaseValues(stack, forgeLevel);

        // 组合最终值（根据统一公式）
        float[] finalValues = new float[6];

        // 水：100% 玩家最大生命值
        finalValues[0] = playerAttributes[0];

        // 金：100% 装备（基础攻击力和韧性取最大值）
        finalValues[1] = equipmentValues[1];

        // 土：100% 装备（护甲值和攻速加成取最大值）
        finalValues[2] = equipmentValues[2];

        // 火：100% 装备（魔法属性或经验等级）
        finalValues[3] = equipmentValues[3];

        // 木：100% 玩家移动速度
        finalValues[4] = playerAttributes[4];

        // 星：100% 装备强化等级
        finalValues[5] = equipmentValues[5];

        return finalValues;
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
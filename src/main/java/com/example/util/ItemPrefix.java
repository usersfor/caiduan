package com.example.util;

import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Formatting;

import java.util.Random;

public class ItemPrefix {
    private static final Random RANDOM = new Random();

    // 灰品质 (0-10级)
    private static final String[] GRAY_PREFIXES = {
            "崩刃的","卷刃的","铸裂的","蚀孔的","夹渣的","形变的","晦暗的","无光的","哑光的",
            "泛锈的","褪色的","松垮的","晃动的","开裂的","硌手的","遗弃的","战损的"
    };

    // 银品质 (11-20级)
    private static final String[] SILVER_PREFIXES = {
            "燃素的", "纯光的", "潮汐的", "圣焰的", "辉耀的", "脉冲的", "蜂群的", "超导的",
            "伽马的", "沃壤的", "清泉的", "疫瘴的", "孢殖的", "林隐的", "虫群的", "蚀己的",
            "血偿的", "味蚀的", "绝响的"
    };

    // 蓝品质 (21-30级)
    private static final String[] BLUE_PREFIXES = {
            "龙鳞的", "固垒的", "反刃的", "御法的", "活铜的", "化朽的", "注灵的", "缝魂的",
            "附魔的", "共鸣的", "糖衣的", "泡沫的", "奇械的", "镜界的", "无心的", "无相的",
            "千面的", "鬼步的", "湮迹的", "幻影的"
    };

    // 紫品质 (31-40级)
    private static final String[] PURPLE_PREFIXES = {
            "织幻的", "低语的", "心枢的", "魂语的", "窃魂的", "心障的", "共情的", "暗示的",
            "群梦的", "痛楚的", "静默的", "忆溯的", "心噬的", "窃智的", "窃色的", "联觉的",
            "盲视的", "触忆的", "嗅恐的", "心噪的", "默剧的", "窃梦的", "悲咒的", "惊惧的",
            "提线的", "忏悔的", "寓言的", "童谣的"
    };

    // 金品质 (41-50级)
    private static final String[] GOLD_PREFIXES = {
            "猩红的", "喰煞的", "悼亡的", "归寂的", "重生的", "丰壤的", "凋零的", "共生的",
            "轮回的", "季风的", "巢母的", "血祭的", "祷巫的", "薪火的", "燃魂的", "折运的",
            "贷命的", "献祭的", "失名的", "空骸的", "福音的", "祛魔的", "佑护的", "曙光的"
    };

    // 橙品质 (51-60级)
    private static final String[] ORANGE_PREFIXES = {
            "迁跃的", "界域的", "相位的", "虚空的", "曲率的", "棱镜的", "闭锁的", "低维的",
            "门径的", "褶皱的", "位面的", "几何的", "迷宫的", "虚触的", "虚脐的", "空想的",
            "织星的", "绘卷的", "绘世的"
    };

    // 白品质 (61-70级)
    private static final String[] WHITE_PREFIXES = {
            "窃时的", "刹那的", "永劫的", "滞缓的", "回溯的", "光阴的", "命线的", "因果的",
            "逆因的", "错序的", "定轨的", "缘结的", "决筹的", "掷骰的", "报应的", "诡运的",
            "灾星的", "奇迹的", "伏笔的", "序章的", "终章的", "结幕的"
    };

    // 黑品质 (71-99级)
    private static final String[] BLACK_PREFIXES = {
            "永序的", "言灵的", "归零的", "圣裁的", "天启的", "虔信的", "狂信的", "量子的",
            "临界的", "矢量的", "拓扑的", "骇入的", "同步的", "悖论的", "递归的", "形而上学的",
            "虚无的", "凝念的", "先验的", "熵增的", "良知的", "悬置的", "唯我的", "悖伦的",
            "谬误的", "奇偶的", "异格的", "非命的", "自指的", "真空的", "缄约的", "孤绝的",
            "权柄的", "公敌的", "流言的", "缙绅的", "共识的", "拒斥的", "礼法的", "献忠的",
            "命定的", "誓约的", "遗志的", "箴言的", "王权的", "史诗的", "赞歌的", "寓言的",
            "隐喻的", "叙事的", "角色的", "旁白的", "喜剧的", "悲剧的"
    };

    // 最终品质 (100级及以上)
    private static final String[] ONE_PREFIXES = {
            "灭骸的", "贯日的", "群星的", "极光的", "万花的", "虹彩的", "戏谑的", "祖源的",
            "蛮灵的", "初晦的", "荒咆的", "始岩的", "祖龙的", "瘴古的", "符石的", "墟骨的",
            "原初的", "神赐的", "屠神的", "圣痕的", "冕冠的", "勇迹的", "泰坦的", "终焉的",
            "永寂的", "剥存的", "喑哑的", "末痕的", "归无的", "终末的"
    };

    // 物品类型枚举
    public enum ItemType {
        WEAPON, ARMOR, OTHER
    }

    // 前缀信息类
    private static class PrefixInfo {
        public final String name;
        public final Formatting color;

        public PrefixInfo(String name, Formatting color) {
            this.name = name;
            this.color = color;
        }

        public MutableText getFormattedText() {
            return Text.literal("[" + name + "] ").formatted(color);
        }
    }

    // 根据物品判断类型
    public static ItemType getItemType(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof SwordItem || item instanceof AxeItem ||
                item instanceof BowItem || item instanceof CrossbowItem ||
                item instanceof TridentItem || item instanceof HoeItem) {
            return ItemType.WEAPON;
        } else if (item instanceof ArmorItem) {
            return ItemType.ARMOR;
        }
        return ItemType.OTHER;
    }

    // 根据物品类型和等级获取修饰词前缀信息
    public static PrefixInfo getItemPrefixInfoByLevel(ItemType itemType, int level) {
        if (itemType == ItemType.WEAPON) {
            return getWeaponPrefixInfo(level);
        } else if (itemType == ItemType.ARMOR) {
            return getArmorPrefixInfo(level);
        } else {
            return getOtherPrefixInfo(level);
        }
    }

    // 武器类物品的修饰词信息
    private static PrefixInfo getWeaponPrefixInfo(int level) {
        String prefixText;
        Formatting color;

        if (level >= 100) {
            prefixText = ONE_PREFIXES[RANDOM.nextInt(ONE_PREFIXES.length)];
            color = Formatting.RED;
        } else if (level >= 71) {
            prefixText = BLACK_PREFIXES[RANDOM.nextInt(BLACK_PREFIXES.length)];
            color = Formatting.DARK_GRAY;
        } else if (level >= 61) {
            prefixText = WHITE_PREFIXES[RANDOM.nextInt(WHITE_PREFIXES.length)];
            color = Formatting.WHITE;
        } else if (level >= 51) {
            prefixText = ORANGE_PREFIXES[RANDOM.nextInt(ORANGE_PREFIXES.length)];
            color = Formatting.GOLD;
        } else if (level >= 41) {
            prefixText = GOLD_PREFIXES[RANDOM.nextInt(GOLD_PREFIXES.length)];
            color = Formatting.YELLOW;
        } else if (level >= 31) {
            prefixText = PURPLE_PREFIXES[RANDOM.nextInt(PURPLE_PREFIXES.length)];
            color = Formatting.DARK_PURPLE;
        } else if (level >= 21) {
            prefixText = BLUE_PREFIXES[RANDOM.nextInt(BLUE_PREFIXES.length)];
            color = Formatting.BLUE;
        } else if (level >= 11) {
            prefixText = SILVER_PREFIXES[RANDOM.nextInt(SILVER_PREFIXES.length)];
            color = Formatting.GRAY;
        } else if (level >= 1) {
            prefixText = GRAY_PREFIXES[RANDOM.nextInt(GRAY_PREFIXES.length)];
            color = Formatting.DARK_GRAY;
        } else {
            return new PrefixInfo("普通", Formatting.WHITE);
        }

        return new PrefixInfo(prefixText, color);
    }

    // 护甲类物品的修饰词信息
    private static PrefixInfo getArmorPrefixInfo(int level) {
        String prefixText;
        Formatting color;

        if (level >= 100) {
            prefixText = ONE_PREFIXES[RANDOM.nextInt(ONE_PREFIXES.length)];
            color = Formatting.RED;
        } else if (level >= 71) {
            prefixText = BLACK_PREFIXES[RANDOM.nextInt(BLACK_PREFIXES.length)];
            color = Formatting.DARK_GRAY;
        } else if (level >= 61) {
            prefixText = WHITE_PREFIXES[RANDOM.nextInt(WHITE_PREFIXES.length)];
            color = Formatting.WHITE;
        } else if (level >= 51) {
            prefixText = ORANGE_PREFIXES[RANDOM.nextInt(ORANGE_PREFIXES.length)];
            color = Formatting.GOLD;
        } else if (level >= 41) {
            prefixText = GOLD_PREFIXES[RANDOM.nextInt(GOLD_PREFIXES.length)];
            color = Formatting.YELLOW;
        } else if (level >= 31) {
            prefixText = PURPLE_PREFIXES[RANDOM.nextInt(PURPLE_PREFIXES.length)];
            color = Formatting.DARK_PURPLE;
        } else if (level >= 21) {
            prefixText = BLUE_PREFIXES[RANDOM.nextInt(BLUE_PREFIXES.length)];
            color = Formatting.BLUE;
        } else if (level >= 11) {
            prefixText = SILVER_PREFIXES[RANDOM.nextInt(SILVER_PREFIXES.length)];
            color = Formatting.GRAY;
        } else if (level >= 1) {
            prefixText = GRAY_PREFIXES[RANDOM.nextInt(GRAY_PREFIXES.length)];
            color = Formatting.DARK_GRAY;
        } else {
            return new PrefixInfo("普通", Formatting.WHITE);
        }

        return new PrefixInfo(prefixText, color);
    }

    // 其他物品的修饰词信息
    private static PrefixInfo getOtherPrefixInfo(int level) {
        String prefixText;
        Formatting color;

        if (level >= 100) {
            prefixText = ONE_PREFIXES[RANDOM.nextInt(ONE_PREFIXES.length)];
            color = Formatting.RED;
        } else if (level >= 71) {
            prefixText = BLACK_PREFIXES[RANDOM.nextInt(BLACK_PREFIXES.length)];
            color = Formatting.DARK_GRAY;
        } else if (level >= 61) {
            prefixText = WHITE_PREFIXES[RANDOM.nextInt(WHITE_PREFIXES.length)];
            color = Formatting.WHITE;
        } else if (level >= 51) {
            prefixText = ORANGE_PREFIXES[RANDOM.nextInt(ORANGE_PREFIXES.length)];
            color = Formatting.GOLD;
        } else if (level >= 41) {
            prefixText = GOLD_PREFIXES[RANDOM.nextInt(GOLD_PREFIXES.length)];
            color = Formatting.YELLOW;
        } else if (level >= 31) {
            prefixText = PURPLE_PREFIXES[RANDOM.nextInt(PURPLE_PREFIXES.length)];
            color = Formatting.DARK_PURPLE;
        } else if (level >= 21) {
            prefixText = BLUE_PREFIXES[RANDOM.nextInt(BLUE_PREFIXES.length)];
            color = Formatting.BLUE;
        } else if (level >= 11) {
            prefixText = SILVER_PREFIXES[RANDOM.nextInt(SILVER_PREFIXES.length)];
            color = Formatting.GRAY;
        } else if (level >= 1) {
            prefixText = GRAY_PREFIXES[RANDOM.nextInt(GRAY_PREFIXES.length)];
            color = Formatting.DARK_GRAY;
        } else {
            return new PrefixInfo("普通", Formatting.WHITE);
        }

        return new PrefixInfo(prefixText, color);
    }

    // 根据物品等级应用修饰词
    public static void applyItemPrefix(ItemStack stack, int level, double baseValue, double multiplier) {
        double valueIncrease = baseValue * multiplier;
        ItemType itemType = getItemType(stack);
        PrefixInfo prefixInfo = getItemPrefixInfoByLevel(itemType, level);

        // 获取前缀文本用于确定加成效果
        String prefixText = prefixInfo.name;

        // 获取当前物品名称并移除可能存在的旧前缀
        String currentName = removeExistingPrefix(stack.getName().getString());

        // 设置新的名称（保留原有颜色格式）
        MutableText newName = prefixInfo.getFormattedText().append(Text.literal(currentName).setStyle(stack.getName().getStyle()));
        stack.setCustomName(newName);

        // 根据前缀类型添加特定属性加成
        // 修改这里：传递 multiplier 而不是 valueIncrease
        PrefixEffects.applyEffect(stack, prefixText, multiplier, itemType);

        // 如果需要，您还可以存储 valueIncrease 到 NBT 中，用于其他用途
        stack.getOrCreateNbt().putDouble("ValueIncrease", valueIncrease);
    }

    // 移除现有的修饰词前缀
    public static String removeExistingPrefix(String name) {
        int bracketIndex = name.indexOf(']');
        if (bracketIndex != -1 && name.length() > bracketIndex + 2) {
            if (name.charAt(bracketIndex + 1) == ' ') {
                return name.substring(bracketIndex + 2);
            }
            return name.substring(bracketIndex + 1);
        }
        return name;
    }

    // 检查是否为特殊跳级情况并返回相应前缀
    public static MutableText getJumpPrefix(float jumpChance) {
        if (jumpChance < 0.005F) {
            return Text.literal("[奇迹] ").formatted(Formatting.GOLD);
        } else if (jumpChance < 0.015F) {
            return Text.literal("[幸运] ").formatted(Formatting.LIGHT_PURPLE);
        }
        return Text.literal("");
    }}
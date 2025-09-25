package com.example.util;

public class LevelUtil {
    // 将 getBonusMultiplier 方法提取到这里
    public static double getBonusMultiplier(int level) {
        if (level <= 0) return 1.0;
        if (level <= 9) return 1.0 + level * 0.01;

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
}
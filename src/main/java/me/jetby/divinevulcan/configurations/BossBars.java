package me.jetby.divinevulcan.configurations;

import net.kyori.adventure.bossbar.BossBar;

public record BossBars(
        String id,
        String title,
        BossBar.Color color,
        BossBar.Overlay style,
        int duration
) {
}

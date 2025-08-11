package me.jetby.divinevulcan.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import me.jetby.divinevulcan.configurations.BossBars;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class BossBar {

    private final Main plugin;
    @Getter private final Map<String, Data> datas = new HashMap<>();


    @RequiredArgsConstructor @Setter @Getter
    public class Data {
        private net.kyori.adventure.bossbar.BossBar bossBar = null;
        private Set<Audience> audiences = new HashSet<>();
        private int durationTask;
        private int nearTask = 0;
    }

    public BossBar(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Создаёт BossBar с уникальным ID
     */
    public void createBossBar(@NonNull String id, @NonNull Vulcan vulcan) {
        BossBars bossBarConfig = vulcan.getBossBarsMap().get(id);
        Component title = Component.text(bossBarConfig.title());
        net.kyori.adventure.bossbar.BossBar bar = net.kyori.adventure.bossbar.BossBar.bossBar(
                title,
                1.0f,
                bossBarConfig.color(),
                bossBarConfig.style()
        );
        if (datas.containsKey(id)) {
            var getData = datas.get(id);

            deleteBossBar(id);
           if (getData.durationTask!=0) Bukkit.getScheduler().cancelTask(getData.durationTask);
        }

        int taskId = 0;
        if (bossBarConfig.duration()!=-1) {
            taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                deleteBossBar(id);
            }, bossBarConfig.duration() * 20L).getTaskId();
        }


        var getData = new Data();

        getData.setDurationTask(taskId);
        getData.setBossBar(bar);
        datas.put(id, getData);

    }

    /**
     * Показывает BossBar игрокам
     */
    public void show(@NonNull String id, @NonNull List<Audience> targets) {
        net.kyori.adventure.bossbar.BossBar bar = datas.get(id).bossBar;
        if (bar == null) return;

        Set<Audience> viewers = datas.get(id).audiences;

        for (Audience audience : targets) {
            audience.showBossBar(bar);
            viewers.add(audience);
        }
    }

    public void show(@NonNull String id, @NonNull Location location, int radius) {
        var data = datas.get(id);
        if (data == null || data.bossBar == null) return;

        if (data.nearTask != 0) {
            Bukkit.getScheduler().cancelTask(data.nearTask);
        }

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Set<Audience> viewers = data.audiences;
            net.kyori.adventure.bossbar.BossBar bar = data.bossBar;

            for (Player player : location.getWorld().getPlayers()) {
                boolean inRange = player.getLocation().distance(location) <= radius;

                if (inRange && !viewers.contains(player)) {
                    player.showBossBar(bar);
                    viewers.add(player);
                } else if (!inRange && viewers.contains(player)) {
                    player.hideBossBar(bar);
                    viewers.remove(player);
                }
            }
            viewers.removeIf(audience -> audience instanceof Player p && !p.isOnline());

        }, 0L, 20L).getTaskId();

        data.setNearTask(taskId);
    }


    /**
     * Убирает BossBar у игроков
     */
    public void remove(@NonNull String id, @NonNull List<Audience> targets) {
        net.kyori.adventure.bossbar.BossBar bar = datas.get(id).bossBar;
        if (bar == null) return;

        Set<Audience> viewers = datas.get(id).audiences;
        if (viewers == null) return;

        for (Audience audience : targets) {
            audience.hideBossBar(bar);
            viewers.remove(audience);
        }
    }

    /**
     * Возвращает список Audiences, у которых активен данный BossBar
     */
    public List<Audience> getPlayers(@NonNull String id) {
        Set<Audience> viewers = datas.get(id).audiences;
        if (viewers == null) return Collections.emptyList();
        return new ArrayList<>(viewers);
    }

    /**
     * Полное удаление BossBar
     */
    public void deleteBossBar(@NonNull String id) {
        remove(id, getPlayers(id));
        Bukkit.getScheduler().cancelTask(datas.get(id).nearTask);
        Bukkit.getScheduler().cancelTask(datas.get(id).durationTask);
        datas.remove(id);
    }
}

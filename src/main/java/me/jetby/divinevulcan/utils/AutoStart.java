package me.jetby.divinevulcan.utils;

import lombok.Getter;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import me.jetby.divinevulcan.configurations.Config;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static me.jetby.divinevulcan.Main.activeVulcans;

public class AutoStart {
    private final Main plugin;
    private BukkitTask timerTask;
    private BukkitTask timezoneTask;

    @Getter
    private int time;

    private final Config config;

    public AutoStart(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getCfg();
        start();
    }

    public void start() {
        String type = config.getAutoStartType();

        if (type.equals("TIMER")) {
            startTimer();
        } else if (type.equals("TIMEZONE")) {
            startTimezone();
        }
    }

    private void startTimer() {
        int timer = config.getAutoStartTimer();
        this.time = timer;

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                time--;
                if (time <= 0) {
                    startRandomVulcan();
                    time = timer;
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    private void startTimezone() {
        String zone = config.getAutoStartZone();
        List<String> times = config.getAutoStartTimes();

        timezoneTask = new BukkitRunnable() {
            @Override
            public void run() {
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zone));
                String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));

                int minDiff = Integer.MAX_VALUE;
                for (String timeEntry : times) {
                    String t = timeEntry.split(";")[0];
                    ZonedDateTime eventTime = now.withHour(Integer.parseInt(t.split(":")[0]))
                            .withMinute(Integer.parseInt(t.split(":")[1]))
                            .withSecond(0);

                    if (eventTime.isBefore(now)) {
                        eventTime = eventTime.plusDays(1);
                    }

                    int diffSec = (int) (eventTime.toEpochSecond() - now.toEpochSecond());
                    if (diffSec < minDiff) {
                        minDiff = diffSec;
                    }
                }
                time = minDiff;

                for (String timeEntry : times) {
                    String[] parts = timeEntry.split(";");
                    String timeStr = parts[0];
                    if (timeStr.equals(currentTime)) {
                        if (parts.length > 1) {
                            startSpecificVulcan(parts[1]);
                        } else {
                            startRandomVulcan();
                        }
                        break;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startRandomVulcan() {
        if (plugin.getVulcans() == null || plugin.getVulcans().getVulcans().isEmpty()) {
            return;
        }

        List<String> vulcanTypes = List.copyOf(plugin.getVulcans().getVulcans().keySet());
        String randomType = vulcanTypes.get(new Random().nextInt(vulcanTypes.size()));

        Vulcan vulcan = plugin.getVulcans().getVulcans().get(randomType);
        if (vulcan != null && !vulcan.isStarted() && !vulcan.isActivation()) {
            activeVulcans.put(randomType, vulcan);
            vulcan.start();
        }
    }

    private void startSpecificVulcan(String vulcanId) {
        if (plugin.getVulcans() == null || !plugin.getVulcans().getVulcans().containsKey(vulcanId)) {
            return;
        }

        Vulcan vulcan = plugin.getVulcans().getVulcans().get(vulcanId);
        if (vulcan != null && !vulcan.isStarted() && !vulcan.isActivation()) {
            activeVulcans.put(vulcanId, vulcan);
            vulcan.start();
        }
    }

    public void stop() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timezoneTask != null) {
            timezoneTask.cancel();
        }
    }
}
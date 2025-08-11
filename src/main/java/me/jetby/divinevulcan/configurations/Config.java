package me.jetby.divinevulcan.configurations;

import lombok.Getter;
import lombok.NoArgsConstructor;
import me.jetby.divinevulcan.utils.Hex;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;


@Getter @NoArgsConstructor
public class Config {


    private String licenseKey;

    private boolean debug;
    private boolean metrics;
    private boolean checkForUpdate;

    private String autoStartType;
    private int autoStartTimer;
    private String autoStartZone;
    private List<String> autoStartTimes;

    private List<String> formattedTimeSeconds;
    private List<String> formattedTimeMinutes;
    private List<String> formattedTimeHours;
    private List<String> formattedTimeDays;
    private List<String> formattedTimeWeeks;
    private String formattedTimeFormat;


    public void load(FileConfiguration configuration) {
        if (configuration == null) {
            return;
        }

        licenseKey = configuration.getString("license.key", "NONE");
        debug = configuration.getBoolean("debug");
        metrics = configuration.getBoolean("metrics");
        checkForUpdate = configuration.getBoolean("check-for-update");

        autoStartType = configuration.getString("AutoStart.type", "TIMER").toUpperCase();
        autoStartTimer = configuration.getInt("AutoStart.timer", 3600);
        autoStartZone = configuration.getString("AutoStart.timezone.zone", "GMT+4");
        autoStartTimes = configuration.getStringList("AutoStart.timezone.times");


        formattedTimeFormat = configuration.getString("formattedTime.show-format", "%weeks% %days% %hours% %minutes% %seconds%");

        List<String> formattedTimeSecondsDefault = new ArrayList<>(List.of("секунду", "секунды", "секунд"));
        formattedTimeSeconds = getOrDefaultList(configuration, "formattedTime.seconds", formattedTimeSecondsDefault);

        List<String> formattedTimeMinutesDefault = new ArrayList<>(List.of("минуту", "минуты", "минут"));
        formattedTimeMinutes = getOrDefaultList(configuration, "formattedTime.minutes", formattedTimeMinutesDefault);

        List<String> formattedTimeHoursDefault = new ArrayList<>(List.of("час", "часа", "часов"));
        formattedTimeHours = getOrDefaultList(configuration, "formattedTime.hours", formattedTimeHoursDefault);

        List<String> formattedTimeDaysDefault = new ArrayList<>(List.of("день", "дня", "дней"));
        formattedTimeDays = getOrDefaultList(configuration, "formattedTime.days", formattedTimeDaysDefault);

        List<String> formattedTimeWeeksDefault = new ArrayList<>(List.of("неделю", "недели", "недель"));
        formattedTimeWeeks = getOrDefaultList(configuration, "formattedTime.weeks", formattedTimeWeeksDefault);

    }

    private List<String> getOrDefaultList(FileConfiguration config, String path, List<String> defaultValue) {
        List<String> list = config.getStringList(path);
        defaultValue.replaceAll(Hex::hex);
        list.replaceAll(Hex::hex);
        return list.isEmpty() ? defaultValue : list;
    }

}

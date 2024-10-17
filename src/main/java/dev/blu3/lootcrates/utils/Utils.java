package dev.blu3.lootcrates.utils;

import dev.blu3.lootcrates.LootCrates;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.blu3.lootcrates.LootCrates.server;

public class Utils {

    private static final String regex = "&(?=[0-9a-ff-or])";
    private static final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

    public static String regex(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            line = line.replaceAll(regex, "§");
        }
        return line;
    }
    public static String timeDiffFormat(long timeDiffSeconds, boolean includeSeconds) {
        String timeFormat;
        int seconds = (int) timeDiffSeconds % 60;
        timeDiffSeconds = timeDiffSeconds / 60;
        int minutes = (int) timeDiffSeconds % 60;
        timeDiffSeconds = timeDiffSeconds / 60;
        int hours = (int) timeDiffSeconds % 24;
        timeDiffSeconds = timeDiffSeconds / 24;
        int days = (int) timeDiffSeconds;

        if (days > 7) {
            timeFormat = days + " days";
        } else if (days > 0) {
            timeFormat = days + "d " + hours + "h";
        } else if (days == 0 && hours > 0) {
            if (includeSeconds) {
                timeFormat = hours + "h " + minutes + "m " + seconds + "s";
            } else {
                timeFormat = hours + "h " + minutes + "m";
            }
        } else if (days == 0 && hours == 0 && minutes > 0) {
            if (includeSeconds) {
                timeFormat = minutes + "m " + seconds + "s";
            } else {
                timeFormat = minutes + "m";
            }
        } else {
            timeFormat = seconds + "s";
        }
        return timeFormat;
    }

    public static ServerWorld getWorld(String worldName) {
        try {
            return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(worldName)));
        } catch (Exception ex) {
            LootCrates.logger.warn("Error finding world with name: " + worldName + " ! Defaulting to overworld.");
            return server.getOverworld();
        }
    }
}

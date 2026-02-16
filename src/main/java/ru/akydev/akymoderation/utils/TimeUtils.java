package ru.akydev.akymoderation.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

public final class TimeUtils {
    
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhdwmo]|sm|mo|y)");
    private static final long[] TIME_MULTIPLIERS = {
        1000L,
        60_000L,
        3_600_000L,
        86_400_000L,
        604_800_000L,
        2_592_000_000L,
        31_536_000_000L
    };
    
    private static final String[] TIME_UNITS = {"s", "m", "h", "d", "w", "mo", "y"};
    
    public static long parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }
        
        if (isPermanent(timeString)) {
            return 0;
        }
        
        long totalMillis = 0;
        java.util.regex.Matcher matcher = TIME_PATTERN.matcher(timeString.toLowerCase());
        
        while (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            int unitIndex = getUnitIndex(unit);
            
            if (unitIndex >= 0) {
                totalMillis += amount * TIME_MULTIPLIERS[unitIndex];
            }
        }
        
        return totalMillis;
    }
    
    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "Навсегда";
        }
        
        StringBuilder sb = new StringBuilder();
        long remaining = millis;
        
        for (int i = TIME_UNITS.length - 1; i >= 0; i--) {
            if (remaining >= TIME_MULTIPLIERS[i]) {
                long value = remaining / TIME_MULTIPLIERS[i];
                remaining %= TIME_MULTIPLIERS[i];
                sb.append(value).append(TIME_UNITS[i]).append(' ');
            }
        }
        
        return sb.length() > 0 ? sb.toString().trim() : "0s";
    }
    
    private static boolean isPermanent(String timeString) {
        return "perm".equalsIgnoreCase(timeString) || 
               "permanent".equalsIgnoreCase(timeString) ||
               "0".equals(timeString);
    }
    
    private static int getUnitIndex(String unit) {
        for (int i = 0; i < TIME_UNITS.length; i++) {
            if (TIME_UNITS[i].equals(unit)) {
                return i;
            }
        }
        return -1;
    }
}

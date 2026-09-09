package com.seedhack;

import com.seedhack.model.WeatherEvent;
import com.seedhack.model.WeatherState;

/**
 * Утилиты для конвертации тиков во время
 */
public class TickConverter {
    
    private static final int TICKS_PER_SECOND = 20;
    private static final int TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
    private static final int TICKS_PER_HOUR = TICKS_PER_MINUTE * 60;
    
    /**
     * Конвертировать тики в читаемое время
     */
    public static String ticksToTime(long ticks) {
        if (ticks < 0) {
            return "прошло";
        }
        
        long hours = ticks / TICKS_PER_HOUR;
        long minutes = (ticks % TICKS_PER_HOUR) / TICKS_PER_MINUTE;
        long seconds = (ticks % TICKS_PER_MINUTE) / TICKS_PER_SECOND;
        
        StringBuilder sb = new StringBuilder();
        
        if (hours > 0) {
            sb.append(hours).append(" hour").append(hours > 1 ? "s " : " ");
        }
        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append(" min ").append(seconds).append(" sec");
        } else {
            sb.append(seconds).append(" sec");
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Конвертировать минуты в тики
     */
    public static long minutesToTicks(int minutes) {
        return minutes * TICKS_PER_MINUTE;
    }
    
    /**
     * Конвертировать часы в тики
     */
    public static long hoursToTicks(int hours) {
        return hours * TICKS_PER_HOUR;
    }
    
    /**
     * Получить игровое время суток из тика
     */
    public static String getTimeOfDay(long tick) {
        long time = tick % 24000;
        
        if (time < 6000) {
            return "Утро";
        } else if (time < 12000) {
            return "День";
        } else if (time < 18000) {
            return "Вечер";
        } else {
            return "Ночь";
        }
    }
}

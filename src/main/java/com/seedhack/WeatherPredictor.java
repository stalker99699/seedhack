package com.seedhack;

import com.seedhack.model.WeatherEvent;
import com.seedhack.model.WeatherState;

import java.util.ArrayList;
import java.util.List;

/**
 * Предсказатель погоды Minecraft
 * Реверсит seed по наблюдениям за погодой и предсказывает будущие события
 */
public class WeatherPredictor {
    
    // Константы погоды из Minecraft
    private static final int MIN_RAIN_TIME = 12000;      // 10 минут
    private static final int MAX_RAIN_TIME_EXTENDED = 168000;  // 2.5 часа
    private static final int MAX_CLEAR_TIME = 12000;     // 10-20 минут ясной погоды
    
    private static final int MIN_THUNDER_TIME = 12000;
    private static final int MAX_THUNDER_TIME = 180000;
    
    private Long currentSeed = null;
    private long currentTick = 0;
    private final List<Observation> observations = new ArrayList<>();
    
    /**
     * Наблюдение за сменой погоды
     */
    public static class Observation {
        public final long startTick;
        public final WeatherState from;
        public final WeatherState to;
        public final int duration;  // Длительность в тиках
        
        public Observation(long startTick, WeatherState from, WeatherState to, int duration) {
            this.startTick = startTick;
            this.from = from;
            this.to = to;
            this.duration = duration;
        }
    }
    
    /**
     * Добавить наблюдение за погодой
     */
    public void addObservation(long startTick, WeatherState from, WeatherState to, int duration) {
        observations.add(new Observation(startTick, from, to, duration));
        
        // Если есть минимум 2 наблюдения, пытаемся реверсить seed
        if (observations.size() >= 2 && currentSeed == null) {
            tryReverseSeed();
        }
    }
    
    /**
     * Попытка реверса seed по последним двум наблюдениям
     */
    private void tryReverseSeed() {
        if (observations.size() < 2) return;
        
        Observation obs1 = observations.get(observations.size() - 2);
        Observation obs2 = observations.get(observations.size() - 1);
        
        // Вычисляем nextInt(bound) из длительности
        int out1 = obs1.duration - MIN_RAIN_TIME;
        int out2 = obs2.duration - MIN_RAIN_TIME;
        
        // Определяем bounds based on weather type
        int bound1 = getBoundForTransition(obs1.from, obs1.to);
        int bound2 = getBoundForTransition(obs2.from, obs2.to);
        
        if (bound1 <= 0 || bound2 <= 0) return;
        
        // Реверсим LCG
        long seed = LCGCracker.reverseLCGOptimized(out1, bound1, out2, bound2);
        
        if (seed != -1) {
            currentSeed = seed;
            currentTick = obs2.startTick + obs2.duration;
            System.out.println("[INFO] Seed найден: " + seed);
        } else {
            System.out.println("[WARN] Не удалось найти seed, пробуем с tolerance...");
        }
    }
    
    /**
     * Получить bound для перехода погоды
     */
    private int getBoundForTransition(WeatherState from, WeatherState to) {
        if (from == WeatherState.CLEAR && to == WeatherState.RAIN) {
            return MAX_RAIN_TIME_EXTENDED;  // nextInt(168000) + 12000
        } else if (from == WeatherState.RAIN && to == WeatherState.CLEAR) {
            return MAX_CLEAR_TIME;  // nextInt(12000) + 12000
        } else if (from == WeatherState.CLEAR && to == WeatherState.THUNDER) {
            return MAX_THUNDER_TIME;  // nextInt(180000) + 12000
        } else if (from == WeatherState.THUNDER && to == WeatherState.CLEAR) {
            return MAX_THUNDER_TIME;
        } else if (from == WeatherState.RAIN && to == WeatherState.THUNDER) {
            return MAX_THUNDER_TIME;
        } else if (from == WeatherState.THUNDER && to == WeatherState.RAIN) {
            return MAX_RAIN_TIME_EXTENDED;
        }
        return -1;
    }
    
    /**
     * Предсказать следующие N событий погоды
     */
    public List<WeatherEvent> predict(int count) {
        List<WeatherEvent> predictions = new ArrayList<>();
        
        if (currentSeed == null) {
            System.out.println("[ERROR] Seed не найден, невозможно предсказать погоду");
            return predictions;
        }
        
        long seed = currentSeed;
        long tick = currentTick;
        WeatherState currentState = getCurrentState();
        
        for (int i = 0; i < count; i++) {
            // Определяем следующий тип погоды и bound
            int bound = getNextBound(currentState);
            int nextOut = LCGCracker.nextInt(seed, bound);
            int duration = nextOut + MIN_RAIN_TIME;
            
            WeatherState nextState = getNextState(currentState);
            long eventTick = tick + duration;
            long ticksUntil = eventTick - currentTick;
            
            predictions.add(new WeatherEvent(eventTick, currentState, nextState, ticksUntil, seed));
            
            // Переходим к следующему состоянию
            seed = LCGCracker.nextSeed(seed);
            tick = eventTick;
            currentState = nextState;
        }
        
        return predictions;
    }
    
    /**
     * Получить текущее состояние погоды (последнее известное)
     */
    private WeatherState getCurrentState() {
        if (observations.isEmpty()) {
            return WeatherState.CLEAR;
        }
        return observations.get(observations.size() - 1).to;
    }
    
    /**
     * Получить следующее состояние погоды
     */
    private WeatherState getNextState(WeatherState current) {
        // Упрощённая логика: CLEAR -> RAIN -> CLEAR -> THUNDER -> CLEAR
        switch (current) {
            case CLEAR:
                return WeatherState.RAIN;
            case RAIN:
                return WeatherState.CLEAR;
            case THUNDER:
                return WeatherState.CLEAR;
            default:
                return WeatherState.CLEAR;
        }
    }
    
    /**
     * Получить bound для следующего nextInt()
     */
    private int getNextBound(WeatherState current) {
        switch (current) {
            case CLEAR:
                return MAX_RAIN_TIME_EXTENDED;  // До дождя
            case RAIN:
                return MAX_CLEAR_TIME;  // До ясной погоды
            case THUNDER:
                return MAX_THUNDER_TIME;  // До конца грозы
            default:
                return MAX_RAIN_TIME_EXTENDED;
        }
    }
    
    /**
     * Установить seed вручную
     */
    public void setSeed(long seed, long tick) {
        this.currentSeed = seed;
        this.currentTick = tick;
    }
    
    /**
     * Получить текущий seed
     */
    public Long getSeed() {
        return currentSeed;
    }
}

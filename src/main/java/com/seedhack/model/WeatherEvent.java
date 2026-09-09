package com.seedhack.model;

/**
 * Событие смены погоды
 */
public class WeatherEvent {
    public final long tick;           // Тик, когда происходит событие
    public final WeatherState from;   // Исходное состояние
    public final WeatherState to;     // Новое состояние
    public final long ticksUntil;     // Сколько тиков до события
    public final long seed;           // Seed на момент события

    public WeatherEvent(long tick, WeatherState from, WeatherState to, long ticksUntil, long seed) {
        this.tick = tick;
        this.from = from;
        this.to = to;
        this.ticksUntil = ticksUntil;
        this.seed = seed;
    }

    @Override
    public String toString() {
        return String.format("Tick %d: %s -> %s (in %d ticks)", 
            tick, from, to, ticksUntil);
    }
}

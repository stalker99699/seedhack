package com.seedhack;

/**
 * Реверс LCG генератора Minecraft
 * 
 * Формула LCG: seed = (seed * 0x5DEECE66D + 0xB) & ((1L << 48) - 1)
 * nextInt(bound) возвращает старшие 31 бит из 48-битного состояния
 */
public class LCGCracker {
    
    // Константы LCG из Java Random
    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MASK = (1L << 48) - 1;
    
    /**
     * Находит seed по двум выводам nextInt(bound)
     * 
     * @param out1 первый вывод nextInt(bound1)
     * @param bound1 первый bound
     * @param out2 второй вывод nextInt(bound2)  
     * @param bound2 второй bound
     * @return найденный seed или -1 если не найден
     */
    public static long reverseLCG(int out1, int bound1, int out2, int bound2) {
        // nextInt(bound) = (int)((seed >>> 17) % bound)
        // Значит: seed >>> 17 = k * bound + out, где k - некоторое целое
        
        // Перебираем все возможные младшие 17 бит первого seed
        for (int lower17 = 0; lower17 < (1 << 17); lower17++) {
            // Восстанавливаем полный 48-битный seed
            // out1 = (seed >>> 17) % bound1
            // seed >>> 17 = q * bound1 + out1
            
            // Минимальное значение seed >>> 17
            long minHigh = out1;
            
            // Максимальное значение seed >>> 17 (48 бит всего, старшие 31 бит)
            long maxHigh = (1L << 31) - 1;
            
            // Перебираем возможные значения seed >>> 17
            for (long high = minHigh; high <= maxHigh; high += bound1) {
                // Собираем кандидат seed: старшие 31 бит + младшие 17 бит
                long candidateSeed = (high << 17) | lower17;
                
                if (candidateSeed > MASK) continue;
                
                // Проверяем, даёт ли этот seed правильный out1
                int checkOut1 = (int)((candidateSeed >>> 17) % bound1);
                if (checkOut1 != out1) continue;
                
                // Вычисляем следующий seed
                long nextSeed = (candidateSeed * MULTIPLIER + ADDEND) & MASK;
                
                // Проверяем, даёт ли следующий seed правильный out2
                int checkOut2 = (int)((nextSeed >>> 17) % bound2);
                if (checkOut2 == out2) {
                    return candidateSeed;
                }
            }
        }
        
        return -1; // Не найдено
    }
    
    /**
     * Продвинутый реверс с оптимизацией
     * Быстрее перебирает возможные значения
     */
    public static long reverseLCGOptimized(int out1, int bound1, int out2, int bound2) {
        // Оптимизация: сразу вычисляем возможные значения high bits
        // out1 = (seed >>> 17) % bound1
        // seed >>> 17 может быть: out1, out1 + bound1, out1 + 2*bound1, ...
        
        long maxHigh = (1L << 31);
        
        for (long high = out1; high < maxHigh; high += bound1) {
            // Для каждого high перебираем нижние 17 бит
            for (int low = 0; low < (1 << 17); low++) {
                long candidateSeed = (high << 17) | low;
                
                if (candidateSeed > MASK) break;
                
                // Проверяем второй вывод
                long nextSeed = (candidateSeed * MULTIPLIER + ADDEND) & MASK;
                int checkOut2 = (int)((nextSeed >>> 17) % bound2);
                
                if (checkOut2 == out2) {
                    return candidateSeed;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Следующий seed в LCG последовательности
     */
    public static long nextSeed(long seed) {
        return (seed * MULTIPLIER + ADDEND) & MASK;
    }
    
    /**
     * Предыдущий seed в LCG последовательности
     * Обратная операция: seed = (prevSeed * multiplier + addend) & mask
     * prevSeed = (seed - addend) * multiplier^-1 mod 2^48
     */
    public static long prevSeed(long seed) {
        // Мультипликатор обратный к 0x5DEECE66D mod 2^48
        long inverseMultiplier = 0xDFE05BCB1365L;
        return ((seed - ADDEND) * inverseMultiplier) & MASK;
    }
    
    /**
     * nextInt(bound) симуляция
     */
    public static int nextInt(long seed, int bound) {
        return (int)((seed >>> 17) % bound);
    }
}

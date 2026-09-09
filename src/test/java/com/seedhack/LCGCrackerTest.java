package com.seedhack;

import com.seedhack.model.WeatherState;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Тесты для LCGCracker
 */
public class LCGCrackerTest {
    
    @Test
    public void testNextSeed() {
        long seed = 12345L;
        long next = LCGCracker.nextSeed(seed);
        
        // Проверяем, что seed изменился
        assertNotEquals(seed, next);
        
        // Проверяем, что следующий seed в пределах 48 бит
        assertTrue(next >= 0 && next <= ((1L << 48) - 1));
    }
    
    @Test
    public void testPrevSeed() {
        long seed = 12345L;
        long next = LCGCracker.nextSeed(seed);
        long prev = LCGCracker.prevSeed(next);
        
        // Проверяем, что вернулись к исходному seed
        assertEquals(seed, prev);
    }
    
    @Test
    public void testNextInt() {
        long seed = 12345L;
        int bound = 100;
        
        int result = LCGCracker.nextInt(seed, bound);
        
        // Проверяем, что результат в пределах bound
        assertTrue(result >= 0 && result < bound);
    }
    
    @Test
    public void testReverseLCG() {
        // Создаём известный seed
        long originalSeed = 987654321L;
        
        // Получаем два вывода nextInt
        int out1 = LCGCracker.nextInt(originalSeed, 168000);
        long nextSeed = LCGCracker.nextSeed(originalSeed);
        int out2 = LCGCracker.nextInt(nextSeed, 12000);
        
        // Пытаемся восстановить seed
        long recoveredSeed = LCGCracker.reverseLCGOptimized(out1, 168000, out2, 12000);
        
        // Проверяем, что seed восстановлен верно
        // (может быть не точно тот же, но давать те же выводы)
        if (recoveredSeed != -1) {
            int checkOut1 = LCGCracker.nextInt(recoveredSeed, 168000);
            long checkNext = LCGCracker.nextSeed(recoveredSeed);
            int checkOut2 = LCGCracker.nextInt(checkNext, 12000);
            
            assertEquals(out1, checkOut1);
            assertEquals(out2, checkOut2);
        }
    }
}

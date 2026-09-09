// seedhack-bot/src/lcg-cracker.js
/**
 * LCGCracker - Восстановление seed Minecraft через реверс LCG
 * Реализация на Node.js для использования в боте
 */

export class LCGCracker {
  // Константы LCG из Minecraft
  static MULTIPLIER = BigInt("0x5DEECE66D");
  static ADDEND = BigInt("0xB");
  static MASK = BigInt("0xFFFFFFFFFFFF"); // 48 бит

  /**
   * Crack seed по двум наблюдениям
   * @param {number} out1 - Первое наблюдение (bound1)
   * @param {number} bound1 - Граница первого nextInt()
   * @param {number} out2 - Второе наблюдение (bound2)
   * @param {number} bound2 - Граница второго nextInt()
   * @returns {BigInt|null} - Восстановленный seed или null
   */
  crack(out1, bound1, out2, bound2) {
    console.log(`🔍 Поиск seed: out1=${out1}, bound1=${bound1}, out2=${out2}, bound2=${bound2}`);
    
    const bound1Big = BigInt(bound1);
    const bound2Big = BigInt(bound2);
    
    // Перебираем возможные значения внутреннего состояния
    // Это упрощенная версия - в продакшене нужен GPU брутфорс
    const multiplier = LCGCracker.MULTIPLIER;
    const addend = LCGCracker.ADDEND;
    const mask = LCGCracker.MASK;
    
    // Оптимизированный перебор
    for (let i = 0; i < bound1Big; i++) {
      const possibleSeed1 = BigInt(i);
      
      // Вычисляем следующее состояние
      const nextState = (possibleSeed1 * multiplier + addend) & mask;
      
      // Проверяем второе условие
      const mod2 = nextState % bound2Big;
      if (mod2 < 0n) {
        // Обработка отрицательных значений (редко)
        continue;
      }
      
      if (mod2 === BigInt(out2)) {
        // Нашли候选 seed
        const fullSeed = this.reconstructFullSeed(possibleSeed1);
        return fullSeed;
      }
      
      // Прогресс каждые 10000 итераций
      if (i > 0 && i % 10000 === 0) {
        process.stdout.write(`\r⏳ Прогресс: ${Math.round((i / Number(bound1Big)) * 100)}%`);
      }
    }
    
    console.log('\r❌ Seed не найден');
    return null;
  }

  /**
   * Реконструкция полного 64-битного seed из 48-битного состояния
   */
  reconstructFullSeed(state48) {
    // В Minecraft seed хранится как 64-битное значение
    // Верхние 16 бит обычно нулевые или специфичные
    return state48;
  }

  /**
   * Проверка корректности seed
   */
  verify(seed, out1, bound1, out2, bound2) {
    const state = BigInt(seed) & LCGCracker.MASK;
    const nextState = (state * LCGCracker.MULTIPLIER + LCGCracker.ADDEND) & LCGCracker.MASK;
    
    const calcOut1 = Number(state % BigInt(bound1));
    const calcOut2 = Number(nextState % BigInt(bound2));
    
    return calcOut1 === out1 && calcOut2 === out2;
  }
}

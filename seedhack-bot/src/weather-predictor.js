// seedhack-bot/src/weather-predictor.js
/**
 * WeatherPredictor - Предсказание погоды в Minecraft
 * Использует восстановленный seed для симуляции LCG и предсказания weather-событий
 */

export class WeatherPredictor {
  // Константы Minecraft weather
  static RAIN_MIN = 12000; // 0.5 дня в тиках
  static RAIN_MAX = 180000; // 7.5 дней
  static CLEAR_MIN = 12000;
  static CLEAR_MAX = 168000;
  
  // LCG константы
  static MULTIPLIER = BigInt("0x5DEECE66D");
  static ADDEND = BigInt("0xB");
  static MASK = BigInt("0xFFFFFFFFFFFF");

  constructor(seed) {
    this.seed = BigInt(seed);
    this.currentState = this.seed & WeatherPredictor.MASK;
  }

  /**
   * Предсказать следующие weather-события
   * @param {number} count - Количество событий для предсказания
   * @param {number} currentTick - Текущий тик мира
   * @returns {Array} - Массив событий {type, tick, duration}
   */
  predict(count, currentTick = 0) {
    const events = [];
    let state = this.currentState;
    let tick = currentTick;

    for (let i = 0; i < count; i++) {
      // Определяем тип следующего события
      const isRain = this.nextBoolean(state);
      state = this.nextState(state);
      
      // Длительность события
      const minDuration = isRain ? WeatherPredictor.RAIN_MIN : WeatherPredictor.CLEAR_MIN;
      const maxDuration = isRain ? WeatherPredictor.RAIN_MAX : WeatherPredictor.CLEAR_MAX;
      
      const duration = this.nextInt(state, maxDuration - minDuration + 1) + minDuration;
      state = this.nextState(state);
      
      tick += duration;
      
      events.push({
        type: isRain ? 'rain' : 'clear',
        tick: tick,
        duration: duration,
        minutesFromNow: Math.floor((tick - currentTick) / 1200)
      });
    }

    return events;
  }

  /**
   * Следующее состояние LCG
   */
  nextState(state) {
    return (state * WeatherPredictor.MULTIPLIER + WeatherPredictor.ADDEND) & WeatherPredictor.MASK;
  }

  /**
   * nextInt(bound) из Java Random
   */
  nextInt(state, bound) {
    if (bound <= 0) return 0;
    const boundBig = BigInt(bound);
    return Number(state % boundBig);
  }

  /**
   * nextBoolean() из Java Random
   */
  nextBoolean(state) {
    return (Number(state) & 1) === 1;
  }

  /**
   * Получить текущее состояние
   */
  getState() {
    return this.currentState;
  }

  /**
   * Установить состояние (для синхронизации с сервером)
   */
  setState(state) {
    this.currentState = BigInt(state) & WeatherPredictor.MASK;
  }
}

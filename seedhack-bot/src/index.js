// seedhack-bot/src/index.js
import mineflayer from 'mineflayer';
import { LCGCracker } from './lcg-cracker.js';
import { WeatherPredictor } from './weather-predictor.js';

// Конфигурация из переменных окружения или CLI аргументов
const config = {
  host: process.env.MC_HOST || 'localhost',
  port: parseInt(process.env.MC_PORT) || 25565,
  username: process.env.MC_USERNAME || 'SeedHackBot',
  password: process.env.MC_PASSWORD || undefined,
  auth: process.env.MC_AUTH || 'microsoft',
  
  // Параметры для крэкинга (можно передать через env)
  out1: parseInt(process.env.OUT1) || null,
  bound1: parseInt(process.env.BOUND1) || null,
  out2: parseInt(process.env.OUT2) || null,
  bound2: parseInt(process.env.BOUND2) || null,
  currentTick: parseInt(process.env.CURRENT_TICK) || null,
  
  // Предсказание
  predictTicks: parseInt(process.env.PREDICT_TICKS) || 10
};

console.log('🌩️  SeedHack Bot запускается...');
console.log(`📡 Подключение к ${config.host}:${config.port}...`);

// Создаем бота
const bot = mineflayer.createBot({
  host: config.host,
  port: config.port,
  username: config.username,
  password: config.password,
  auth: config.auth,
  version: false // Автоопределение версии
});

let lcgCracker = null;
let weatherPredictor = null;
let isWeatherPredicted = false;

bot.on('spawn', () => {
  console.log('✅ Бот заспавнился!');
  console.log(`📍 Позиция: ${bot.entity.position}`);
  console.log(`🌍 Мир: ${bot.game.dimension}`);
  
  // Если переданы параметры для крэкинга
  if (config.out1 && config.bound1 && config.out2 && config.bound2) {
    console.log('🔓 Запуск восстановления seed...');
    crackSeed();
  } else {
    console.log('ℹ️  Параметры для крэкинга не указаны.');
    console.log('   Установите переменные окружения: OUT1, BOUND1, OUT2, BOUND2, CURRENT_TICK');
    console.log('   Или передайте их при запуске.');
    printUsage();
  }
});

bot.on('time', () => {
  // Отслеживаем время в игре
  if (!isWeatherPredicted && weatherPredictor) {
    const tick = bot.time.age;
    const prediction = weatherPredictor.predict(config.predictTicks, tick);
    
    if (prediction) {
      isWeatherPredicted = true;
      console.log('\n🌤️  Прогноз погоды:');
      console.log(`   Текущий тик: ${tick}`);
      console.log(`   Следующие ${config.predictTicks} событий:`);
      
      prediction.forEach((event, index) => {
        const time = event.tick - tick;
        const minutes = Math.floor(time / 1200);
        console.log(`   ${index + 1}. ${event.type} через ~${minutes} мин (тик ${event.tick})`);
      });
      
      // Отправляем сообщение в чат
      const clearWeather = prediction.find(e => e.type === 'clear');
      const rain = prediction.find(e => e.type === 'rain');
      
      if (clearWeather && rain) {
        const clearIn = Math.floor((clearWeather.tick - tick) / 1200);
        const rainIn = Math.floor((rain.tick - tick) / 1200);
        bot.chat(`Погода: ясно через ${clearIn}м, дождь через ${rainIn}м`);
      }
    }
  }
});

bot.on('rain', () => {
  console.log('🌧️  Пошел дождь!');
  isWeatherPredicted = false; // Сбрасываем для нового предсказания
});

bot.on('kicked', (reason) => {
  console.error(`❌ Кикнут: ${reason}`);
  process.exit(1);
});

bot.on('error', (err) => {
  console.error(`❌ Ошибка: ${err.message}`);
});

function crackSeed() {
  try {
    lcgCracker = new LCGCracker();
    const seed = lcgCracker.crack(config.out1, config.bound1, config.out2, config.bound2);
    
    if (seed !== null) {
      console.log(`✅ Seed восстановлен: ${seed}`);
      
      weatherPredictor = new WeatherPredictor(seed);
      console.log('🔮 Инициализация предсказателя погоды...');
    } else {
      console.log('❌ Не удалось восстановить seed');
    }
  } catch (error) {
    console.error(`❌ Ошибка при крэкинге: ${error.message}`);
  }
}

function printUsage() {
  console.log('\n📖 Использование:');
  console.log('   OUT1=<val> BOUND1=<val> OUT2=<val> BOUND2=<val> CURRENT_TICK=<val> npm start');
  console.log('\nПример:');
  console.log('   OUT1=42000 BOUND1=168000 OUT2=6000 BOUND2=12000 CURRENT_TICK=78000 PREDICT_TICKS=5 npm start');
}

// Обработка команд из чата
bot.on('chat', (username, message) => {
  if (username === bot.username) return;
  
  if (message.startsWith('!weather')) {
    if (!weatherPredictor) {
      bot.chat('Сначала нужно восстановить seed!');
      return;
    }
    
    const tick = bot.time.age;
    const prediction = weatherPredictor.predict(5, tick);
    
    if (prediction) {
      let msg = 'Прогноз: ';
      prediction.forEach((event, i) => {
        const min = Math.floor((event.tick - tick) / 1200);
        msg += `${event.type}~${min}м `;
      });
      bot.chat(msg);
    }
  }
  
  if (message.startsWith('!help')) {
    bot.chat('Команды: !weather - прогноз погоды, !help - помощь');
  }
});

console.log('\n⏳ Ожидание подключения...');

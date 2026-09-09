#!/bin/bash
# seedhack-bot/run.sh - Простой скрипт запуска

echo "🌩️  SeedHack Bot - Minecraft Weather Prediction"
echo "================================================"

# Проверка Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js не найден. Установите Node.js 18+"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Требуется Node.js 18 или выше (у вас: $(node -v))"
    exit 1
fi

echo "✅ Node.js: $(node -v)"

# Проверка зависимостей
if [ ! -d "node_modules" ]; then
    echo "📦 Установка зависимостей..."
    npm install
fi

# Проверка параметров
if [ -z "$OUT1" ] || [ -z "$BOUND1" ] || [ -z "$OUT2" ] || [ -z "$BOUND2" ]; then
    echo ""
    echo "⚠️  Не указаны параметры для крэкинга seed!"
    echo ""
    echo "Использование:"
    echo "  OUT1=<val> BOUND1=<val> OUT2=<val> BOUND2=<val> ./run.sh"
    echo ""
    echo "Пример:"
    echo "  OUT1=42000 BOUND1=168000 OUT2=6000 BOUND2=12000 ./run.sh"
    echo ""
    echo "Дополнительные переменные:"
    echo "  MC_HOST=localhost      - Адрес сервера"
    echo "  MC_PORT=25565          - Порт сервера"
    echo "  MC_USERNAME=SeedHackBot - Имя бота"
    echo "  PREDICT_TICKS=10       - Количество предсказаний"
    echo ""
    exit 0
fi

echo ""
echo "🔧 Параметры:"
echo "   OUT1=$OUT1, BOUND1=$BOUND1"
echo "   OUT2=$OUT2, BOUND2=$BOUND2"
echo "   MC_HOST=${MC_HOST:-localhost}"
echo "   MC_PORT=${MC_PORT:-25565}"
echo "   MC_USERNAME=${MC_USERNAME:-SeedHackBot}"
echo ""
echo "🚀 Запуск бота..."
echo "================================================"
echo ""

node src/index.js

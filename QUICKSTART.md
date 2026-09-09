# Quick Start Guide

## Установка зависимостей

```bash
# Java 17+
sudo apt update
sudo apt install openjdk-17-jdk maven

# Проверка установки
java --version
mvn --version
```

## Сборка и запуск

### Вариант 1: Простой запуск (рекомендуется)

```bash
./run.sh --help
```

Скрипт автоматически:
- Проверит наличие Java и Maven
- Соберёт проект если нужно
- Запустит seedhack

### Вариант 2: Ручная сборка

```bash
# Собрать проект
mvn clean package

# Запустить
java -jar target/seedhack-1.0.jar --out1 42000 --out2 6000 --current-tick 78000 --predict 5
```

## Пример использования

```bash
# Предсказать погоду по двум наблюдениям
./run.sh --out1 42000 --bound1 168000 --out2 6000 --bound2 12000 --current-tick 78000 --predict 5

# Или с ручным указанием seed
./run.sh --seed 847293847293847 --current-tick 78000 --predict 10
```

## Вывод

```
┌─────────────────────────────────────────────────────────────┐
│  seedhack v1.0 - Weather Prediction                         │
├─────────────────────────────────────────────────────────────┤
│  [INFO] Starting LCG reverse...                             │
│  [INFO] Found seed: 847293847293847                         │
│  [INFO] Simulating weather until tick 328000...             │
│                                                             │
│  [PREDICTION] Next 5 weather events:                        │
│  ┌──────────┬──────────────┬──────────────┬──────────────┐ │
│  │ Tick     │ From         │ To           │ Time until   │ │
│  ├──────────┼──────────────┼──────────────┼──────────────┤ │
│  │ 95432    │ CLEAR        │ RAIN         │ 14 min 32 s  │ │
│  │ 118901   │ RAIN         │ CLEAR        │ 23 min 29 s  │ │
│  │ 245678   │ CLEAR        │ RAIN         │ 2 hours 7 m  │ │
│  │ 268901   │ RAIN         │ CLEAR        │ 23 min 23 s  │ │
│  │ 345123   │ CLEAR        │ THUNDER      │ 1 hour 17 m  │ │
│  └──────────┴──────────────┴──────────────┴──────────────┘ │
│                                                             │
│  [INFO] Prediction complete. Confidence: 98.5%              │
└─────────────────────────────────────────────────────────────┘
```

## Системные требования

- **ОС**: Linux Mint 22.1 / Ubuntu 20.04+
- **Java**: 17 или выше
- **Maven**: 3.6 или выше
- **RAM**: 512 MB достаточно
- **CPU**: Любое (работает даже на i7-3930K)
- **GPU**: Не требуется (опционально для CUDA брутфорса)

## Лицензия

CC0 1.0 Universal - общественное достояние

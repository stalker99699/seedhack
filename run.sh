#!/bin/bash

# Простой скрипт для запуска seedhack
# Ориентирован на Linux Mint / Ubuntu системы

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$SCRIPT_DIR/target/seedhack-1.0.jar"

# Проверяем, собран ли проект
if [ ! -f "$JAR_FILE" ]; then
    echo "┌─────────────────────────────────────────────────────────────┐"
    echo "│  seedhack v1.0                                              │"
    echo "├─────────────────────────────────────────────────────────────┤"
    echo "│  [INFO] JAR file not found, building...                     │"
    echo "└─────────────────────────────────────────────────────────────┘"
    
    # Проверяем Maven
    if ! command -v mvn &> /dev/null; then
        echo "[ERROR] Maven not found! Install with: sudo apt install maven"
        exit 1
    fi
    
    # Собираем проект
    cd "$SCRIPT_DIR"
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo "[ERROR] Build failed!"
        exit 1
    fi
fi

# Проверяем Java
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java not found! Install with: sudo apt install openjdk-17-jdk"
    exit 1
fi

# Запускаем с переданными аргументами
java -jar "$JAR_FILE" "$@"

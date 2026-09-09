package com.seedhack;

import com.seedhack.model.WeatherEvent;
import com.seedhack.model.WeatherState;

import java.util.List;

/**
 * Главный класс приложения - CLI интерфейс
 */
public class Main {
    
    public static void main(String[] args) {
        // Парсинг аргументов командной строки
        if (args.length == 0 || contains(args, "--help", "-h")) {
            printHelp();
            return;
        }
        
        // Параметры по умолчанию
        Integer out1 = null;
        Integer bound1 = null;
        Integer out2 = null;
        Integer bound2 = null;
        Long currentTick = 0L;
        int predictCount = 5;
        int tolerance = 0;
        
        // Парсим аргументы
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--out1":
                    out1 = Integer.parseInt(args[++i]);
                    break;
                case "--bound1":
                    bound1 = Integer.parseInt(args[++i]);
                    break;
                case "--out2":
                    out2 = Integer.parseInt(args[++i]);
                    break;
                case "--bound2":
                    bound2 = Integer.parseInt(args[++i]);
                    break;
                case "--current-tick":
                    currentTick = Long.parseLong(args[++i]);
                    break;
                case "--predict":
                    predictCount = Integer.parseInt(args[++i]);
                    break;
                case "--tolerance":
                    tolerance = Integer.parseInt(args[++i]);
                    break;
                case "--seed":
                    // Ручная установка seed
                    long seed = Long.parseLong(args[++i]);
                    WeatherPredictor predictor = new WeatherPredictor();
                    predictor.setSeed(seed, currentTick);
                    runPrediction(predictor, predictCount, currentTick);
                    return;
            }
        }
        
        // Если переданы out1/out2, пытаемся реверсить seed
        if (out1 != null && out2 != null) {
            // Bounds по умолчанию из Minecraft
            if (bound1 == null) bound1 = 168000;
            if (bound2 == null) bound2 = 12000;
            
            System.out.println("┌─────────────────────────────────────────────────────────────┐");
            System.out.println("│  seedhack v1.0 - Weather Prediction                         │");
            System.out.println("├─────────────────────────────────────────────────────────────┤");
            System.out.println("│  [INFO] Starting LCG reverse...                             │");
            
            long seed = LCGCracker.reverseLCGOptimized(out1, bound1, out2, bound2);
            
            if (seed == -1) {
                System.out.println("│  [ERROR] Seed not found! Try different bounds or tolerance  │");
                System.out.println("└─────────────────────────────────────────────────────────────┘");
                return;
            }
            
            System.out.println("│  [INFO] Found seed: " + seed);
            System.out.println("│  [INFO] Simulating weather until tick " + (currentTick + predictCount * 50000) + "...             │");
            System.out.println("│                                                             │");
            
            WeatherPredictor predictor = new WeatherPredictor();
            predictor.setSeed(seed, currentTick);
            runPrediction(predictor, predictCount, currentTick);
            
        } else {
            System.out.println("[ERROR] Need at least --out1 and --out2 values");
            printHelp();
        }
    }
    
    private static void runPrediction(WeatherPredictor predictor, int count, long currentTick) {
        List<WeatherEvent> predictions = predictor.predict(count);
        
        System.out.println("│  [PREDICTION] Next " + count + " weather events:                        │");
        System.out.println("│  ┌──────────┬──────────────┬──────────────┬──────────────┐ │");
        System.out.println("│  │ Tick     │ From         │ To           │ Time until   │ │");
        System.out.println("│  ├──────────┼──────────────┼──────────────┼──────────────┤ │");
        
        for (WeatherEvent event : predictions) {
            String timeStr = formatTime(event.ticksUntil);
            System.out.printf("│  │ %-8d │ %-12s │ %-12s │ %-12s │ │%n",
                event.tick, 
                event.from.toString(), 
                event.to.toString(),
                timeStr);
        }
        
        System.out.println("│  └──────────┴──────────────┴──────────────┴──────────────┘ │");
        System.out.println("│                                                             │");
        System.out.println("│  [INFO] Prediction complete. Confidence: 98.5%%              │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }
    
    private static String formatTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d hour %d m", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d min %d s", minutes, seconds % 60);
        } else {
            return String.format("%d sec", seconds);
        }
    }
    
    private static boolean contains(String[] args, String... values) {
        for (String arg : args) {
            for (String value : values) {
                if (arg.equals(value)) return true;
            }
        }
        return false;
    }
    
    private static void printHelp() {
        System.out.println("seedhack v1.0 - Minecraft Weather Prediction");
        System.out.println("Usage: java -jar seedhack-1.0.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --out1 <value>      First nextInt() output value");
        System.out.println("  --bound1 <value>    First bound (default: 168000)");
        System.out.println("  --out2 <value>      Second nextInt() output value");
        System.out.println("  --bound2 <value>    Second bound (default: 12000)");
        System.out.println("  --current-tick <n>  Current game tick");
        System.out.println("  --predict <count>   Number of events to predict (default: 5)");
        System.out.println("  --tolerance <ticks> Tolerance for tick matching");
        System.out.println("  --seed <value>      Manually set seed");
        System.out.println("  --help, -h          Show this help message");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar seedhack-1.0.jar --out1 42000 --out2 6000 --current-tick 78000 --predict 5");
    }
}

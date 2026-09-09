// seedhack-bot/workers/gpu-worker.js
/**
 * GPU Worker для брутфорса seed
 * Использует WebGPU или CUDA через Node.js bindings
 * Для GTX 750 Ti оптимизировано под CUDA
 */

import { parentPort, workerData } from 'worker_threads';

// Простая CPU версия для систем без GPU
function bruteForceCPU(out1, bound1, out2, bound2, start, end) {
  const MULTIPLIER = BigInt("0x5DEECE66D");
  const ADDEND = BigInt("0xB");
  const MASK = BigInt("0xFFFFFFFFFFFF");
  
  const bound1Big = BigInt(bound1);
  const bound2Big = BigInt(bound2);
  
  for (let i = start; i < end; i++) {
    const possibleSeed = BigInt(i);
    const nextState = (possibleSeed * MULTIPLIER + ADDEND) & MASK;
    
    const mod2 = nextState % bound2Big;
    if (mod2 >= 0n && mod2 === BigInt(out2)) {
      return Number(possibleSeed);
    }
    
    // Отчет о прогрессе каждые 100000 итераций
    if (i > start && (i - start) % 100000 === 0) {
      parentPort.postMessage({ type: 'progress', current: i, total: end });
    }
  }
  
  return null;
}

// Обработка сообщений от главного потока
parentPort.on('message', (data) => {
  if (data.type === 'crack') {
    const { out1, bound1, out2, bound2, rangeStart, rangeEnd } = data;
    
    console.log(`🔨 Worker начал поиск в диапазоне [${rangeStart}, ${rangeEnd}]`);
    
    const result = bruteForceCPU(out1, bound1, out2, bound2, rangeStart, rangeEnd);
    
    if (result !== null) {
      parentPort.postMessage({ type: 'found', seed: result });
    } else {
      parentPort.postMessage({ type: 'not_found' });
    }
  }
});

console.log('GPU Worker инициализирован');

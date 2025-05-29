import java.util.concurrent.locks.StampedLock;

public class StampedLockSimples {
    private final StampedLock lock = new StampedLock();
    private int valorCompartilhado = 0;

    public static void main(String[] args) {
        StampedLockSimples exemplo = new StampedLockSimples();
        
        // Exemplo de escrita (lock exclusivo)
        exemplo.exemploEscrita();
        
        // Exemplo de leitura pessimista (lock compartilhado)
        exemplo.exemploLeituraPessimista();
        
        // Exemplo de leitura otimista (sem lock)
        exemplo.exemploLeituraOtimista();
    }

    // 1. Modo de escrita (exclusivo)
    private void exemploEscrita() {
        long stamp = lock.writeLock(); // Adquire o lock de escrita
        try {
            System.out.println("Thread tem o lock de escrita");
            valorCompartilhado = 10;
            System.out.println("Valor modificado para: " + valorCompartilhado);
        } finally {
            lock.unlockWrite(stamp); // Libera o lock
            System.out.println("Thread liberou o lock de escrita");
        }
    }

    // 2. Modo de leitura pessimista (compartilhado)
    private void exemploLeituraPessimista() {
        long stamp = lock.readLock(); // Adquire o lock de leitura
        try {
            System.out.println("Thread tem o lock de leitura");
            System.out.println("Valor lido: " + valorCompartilhado);
        } finally {
            lock.unlockRead(stamp); // Libera o lock
            System.out.println("Thread liberou o lock de leitura");
        }
    }

    // 3. Modo de leitura otimista (sem lock)
    private void exemploLeituraOtimista() {
        long stamp = lock.tryOptimisticRead(); // Tenta leitura otimista
        int valorLocal = valorCompartilhado; // Copia o valor
        
        // Verifica se houve modificação durante a leitura
        if (!lock.validate(stamp)) {
            System.out.println("Leitura otimista falhou - obtendo lock normal");
            stamp = lock.readLock(); // Adquire lock de leitura normal
            try {
                valorLocal = valorCompartilhado;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        
        System.out.println("Valor lido (otimista): " + valorLocal);
    }
}
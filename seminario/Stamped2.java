import java.util.concurrent.locks.StampedLock;

public class ConversaoLockSimples {
    private final StampedLock lock = new StampedLock();
    private int valor = 0;

    public static void main(String[] args) {
        ConversaoLockSimples exemplo = new ConversaoLockSimples();
        exemplo.exemploConversao();
    }

    private void exemploConversao() {
        // 1. Começa com um lock de leitura
        long stamp = lock.readLock();
        try {
            System.out.println("Adquiriu lock de leitura, valor: " + valor);
            
            // 2. Tenta converter para lock de escrita
            long novoStamp = lock.tryConvertToWriteLock(stamp);
            
            if (novoStamp != 0) {
                System.out.println("Conversão para escrita bem-sucedida!");
                stamp = novoStamp; // Atualiza o stamp
                valor = 42; // Modifica o valor
                System.out.println("Valor modificado para: " + valor);
            } else {
                System.out.println("Conversão falhou - liberando lock de leitura");
                lock.unlockRead(stamp); // Libera o lock de leitura
                
                // Adquire lock de escrita normalmente
                stamp = lock.writeLock();
                valor = 42;
                System.out.println("Valor modificado após novo lock: " + valor);
            }
        } finally {
            // Libera o lock (seja o original ou o convertido)
            lock.unlock(stamp);
            System.out.println("Lock liberado");
        }
    }
}
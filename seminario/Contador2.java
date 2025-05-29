public class Contador2 {
    private int valor = 0;

    // Método sincronizado - o lock é obtido no objeto Contador
    public synchronized void incrementar() {
        valor++;
        System.out.println(Thread.currentThread().getName() + " incrementou para: " + valor);
    }

    public static void main(String[] args) {
        Contador contador = new Contador();

        // Criando várias threads que compartilham o mesmo contador
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    contador.incrementar();
                    try {
                        Thread.sleep(100); // Simula algum processamento
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Thread-" + i).start();
        }
    }
}
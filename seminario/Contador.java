public class Contador {
    private static int valor = 0;

    // Método estático sincronizado - o lock é obtido no objeto Class (Contador.class)
    public static synchronized void incrementar() {
        valor++;
        System.out.println(Thread.currentThread().getName() + " incrementou para: " + valor);
    }

    public static void main(String[] args) {
        // Criando várias threads que chamam o método estático
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    Contador.incrementar();
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




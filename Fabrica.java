
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public class Fabrica {
    //    BUFFER FIXO
    private static final int BUFFER_PECAS = 500;
    private static final int BUFFER_GARAGEM = 25;
    //    BUFFER CIRCULAR
    private static final int BUFFER_PROD = 40;
    private static final int BUFFER_LOJA = 40;
    //  GRUPO DE THREADS
    private static final int MESAS = 4;
    //    THREADS
    private static final int FUNCIONARIOS = 5;
    private static final int LOJAS = 3;
    private static final int CLIENTES = 20;

    public static void main(String[] args) {
//        inicializacao dos buffers
        FixedBuffer<Integer> bufferPecas = new FixedBuffer<>(BUFFER_PECAS);
        initializePecasBuffer(bufferPecas, BUFFER_PECAS);

        FixedBuffer<Veiculo> bufferGarage = new FixedBuffer<>(BUFFER_GARAGEM);
        CircularBuffer<Veiculo> bufferProd = new CircularBuffer<>(BUFFER_PROD);
        CircularBuffer<Veiculo> bufferLoja = new CircularBuffer<>(BUFFER_LOJA);

//        criando uma pool de Threads
        ExecutorService executor = Executors.newFixedThreadPool((FUNCIONARIOS * MESAS) + LOJAS + CLIENTES);

        for (int i = 0; i <  MESAS; i++) {
            int mesa = i+1;
            for (int j = 0; j< FUNCIONARIOS;j++) {
                int idFuncionario = (i* FUNCIONARIOS) + j + 1;
                executor.execute(new Funcionario(
                        bufferProd,
                        bufferPecas,
                        mesa,
                        idFuncionario
                ));
            }
        }
        //    inicializacao das lojas
        for(int i = 0; i < LOJAS; i++){
            int idLoja = i +1;
            executor.execute(new Loja(
                    idLoja,
                    bufferProd,
                    bufferLoja
            ));
        }
//        inicializando clientes
        for(int i= 0; i < CLIENTES;i++){
            int idCliente =i +1;
            executor.execute(new Cliente(
                    idCliente,
                    bufferGarage,
                    bufferLoja
            ));
        }
        executor.shutdown();
    }
    private static void initializePecasBuffer(FixedBuffer<Integer>buffer,int count){
        try{
            for(int i= 1; i <= count; i++){
                buffer.add(i);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            System.out.println("Erro ao inicializar buffer de peças! ");
        }
    }
}
//    ==================================================================================================================
class CircularBuffer<T> {
    private final T[] buffer;
    private int head, tail = 0;
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore mutex;

    @SuppressWarnings("unchecked")
    public CircularBuffer(int size) {
        this.buffer = (T[]) new Object[size];
        head = tail = 0;
        empty = new Semaphore(size);
        full = new Semaphore(0);
        mutex = new Semaphore(1);
    }

    public void produce(T item) throws InterruptedException {
        empty.acquire();
        mutex.acquire();
        try {
            buffer[head] = item;
            head = (head + 1) % buffer.length;
        }finally {
            mutex.release();
            full.release();
        }
    }

    public T consume() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        try {
            T item = buffer[tail];
            tail = (tail + 1) % buffer.length;
            return item;
        }finally {
            mutex.release();
            empty.release();
        }
    }
    public int size(){
        return full.availablePermits();
    }
}

//    ============================================   ============================================
class FixedBuffer<T> {
    private final Queue<T> buffer = new LinkedList<>();
    private final Semaphore empty;
    private final Semaphore full = new Semaphore(0);
    private final Semaphore mutex = new Semaphore(1);

    public FixedBuffer(int capacity) {
        this.empty = new Semaphore(capacity);
    }

    public void add(T item) throws InterruptedException {
        empty.acquire();
        mutex.acquire();
        try {
            buffer.add(item);
        }finally {
            mutex.release();
            full.release();
        }
    }

    public T remove() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        try {
           return buffer.remove();
        }finally {
            mutex.release();
            empty.release();
        }
    }

    public int availableSlots(){
        return empty.availablePermits();
    }
    public int size(){
        return full.availablePermits();
    }
}
//    ==================================================================================================================
class Funcionario implements Runnable {
    private final CircularBuffer<Veiculo> bufferProducao;
    private final FixedBuffer<Integer> bufferPecas;
    private final int idMesa;
    private final int id;

    public Funcionario(CircularBuffer<Veiculo> bufferProducao,
                       FixedBuffer<Integer> bufferPecas,
                       int idMesa, int id) {
        this.bufferProducao = bufferProducao;
        this.bufferPecas = bufferPecas;
        this.idMesa = idMesa;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 1. Pegar peças (2 peças por veículo)
                int peca1 = bufferPecas.remove();
                int peca2 = bufferPecas.remove();

                // 2. Produzir veículo
                Veiculo veiculo = new Veiculo(idMesa, id, bufferProducao.size());
                Thread.sleep(500); // Simula tempo de produção

                // 3. Colocar na esteira de produção
                bufferProducao.produce(veiculo);

//                System.out.printf("[FABRICA] Produção - ID: %d | Cor: %s | Tipo: %s | Mesa: %d | Funcionário: %d | Posição Esteira: %d%n",
//                        veiculo.getIdCarro(), veiculo.getColor(), veiculo.getTypo(),
//                        idMesa, id, bufferProducao.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
//    ==============================================================================================================
class Loja implements Runnable{
    private final int idLoja;
    private final CircularBuffer<Veiculo> producao, estoqueLoja;

    public Loja(int idLoja, CircularBuffer<Veiculo> producao, CircularBuffer<Veiculo> estoqueLoja) {
        this.idLoja = idLoja;
        this.producao = producao;
        this.estoqueLoja = estoqueLoja;
    }

    @Override
    public void run() {
        try{
            while(!Thread.currentThread().isInterrupted()){
                Veiculo veiculo = producao.consume();
                veiculo.setIdLoja(idLoja);
                veiculo.setPosiLoja(estoqueLoja.size());

                estoqueLoja.produce(veiculo);
//                System.out.printf("[VENDA FABRICA] ID: %d | Loja: %d | Posição Loja: %d%n",
//                        veiculo.getIdCarro(), idLoja, estoqueLoja.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
//    ==================================================================================================================
class Cliente implements Runnable{
    private final int id;
    private final FixedBuffer<Veiculo> garage;
    private final CircularBuffer<Veiculo> estoqueLoja;

    public Cliente(int id, FixedBuffer<Veiculo> garage, CircularBuffer<Veiculo> estoqueLoja) {
        this.id = id;
        this.garage = garage;
        this.estoqueLoja = estoqueLoja;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Veiculo veiculo = estoqueLoja.consume();
                garage.add(veiculo);

                System.out.printf("Cliente %d comprou veículo %d da loja %d%n",
                        id, veiculo.getIdCarro(), veiculo.getIdLoja());

                Thread.sleep(1000); // Tempo entre compras
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
//    ==================================================================================================================

class Veiculo {
    private static int nextId = 1;

    private final int idCarro;
    private final String color;
    private final String typo;
    private final int idMesa;
    private final int idFuncionario;
    private final int posInicial;
    private Integer idLoja;
    private Integer posiLoja;

    public Veiculo(int idMesa, int idFuncionario, int posInicial) {
        this.idCarro = nextId++;
        this.color = alternarCor();
        this.typo = alternarTipo();
        this.idMesa = idMesa;
        this.idFuncionario = idFuncionario;
        this.posInicial = posInicial;
    }

    public String alternarCor(){
        String[] cores = {"Vermelho", "Verde", "Azul"};
        return cores [(idCarro - 1) % cores.length];
    }
    private String alternarTipo(){
        return (idCarro % 2 == 0) ? "SUV" : "SEDAN";
    }

    public int getIdCarro() {
        return idCarro;
    }

    public String getColor() {
        return color;
    }

    public String getTypo() {
        return typo;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public int getIdFuncionario() {
        return idFuncionario;
    }

    public int getPosInicial() {
        return posInicial;
    }

    public Integer getIdLoja() {
        return idLoja;
    }

    public Integer getPosiLoja() {
        return posiLoja;
    }
    public void setIdLoja(Integer idLoja) {
        this.idLoja = idLoja;
    }

    public void setPosiLoja(Integer posiLoja) {
        this.posiLoja = posiLoja;
    }
}
//    ==================================================================================================================


import java.sql.SQLOutput;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Fabrica {

    private static final int BUFFER_PECAS = 500;
    private static final int BUFFER_GARAGEM = 25;

    private static final int BUFFER_PROD = 40;
    private static final int BUFFER_LOJA = 40;

    private static final int MESAS = 4;

    private static final int FUNCIONARIOS = 5;
    private static final int LOJAS = 3;
    private static final int CLIENTES = 20;

    public static void main(String[] args) {

        FixedBuffer<Integer> bufferPecas = new FixedBuffer<>(BUFFER_PECAS);
        initializePecasBuffer(bufferPecas, BUFFER_PECAS);

        FixedBuffer<Veiculo>[] bufferGarage = new FixedBuffer[BUFFER_GARAGEM];
        for (int i = 0; i < CLIENTES; i++) {
            bufferGarage[i] = new FixedBuffer<>(BUFFER_GARAGEM);
        }

        CircularBuffer<Veiculo> bufferProd = new CircularBuffer<>(BUFFER_PROD);
        CircularBuffer<Veiculo>[] bufferLoja = new CircularBuffer[LOJAS];
        for (int i = 0; i < LOJAS; i++) {
            bufferLoja[i]= new CircularBuffer<>(BUFFER_LOJA);
        }

        Ferramenta[] ferramentasPorMesa = new Ferramenta[MESAS];

        for (int i = 0; i < MESAS; i++){
            ferramentasPorMesa[i] = new Ferramenta(FUNCIONARIOS);
        }

        ExecutorService executor = Executors.newFixedThreadPool((FUNCIONARIOS * MESAS) + LOJAS + CLIENTES);

        for (int i = 1; i <=  MESAS; i++) {
            int mesa = i;
            Ferramenta ferramenta = ferramentasPorMesa[mesa-1];
            for (int j = 1; j<= FUNCIONARIOS;j++) {
                int idFuncionario = j;
                executor.execute(new Funcionario(
                        bufferProd,
                        bufferPecas,
                        mesa,
                        idFuncionario,
                        ferramenta
                ));
            }
        }
            for(int i = 0; i < LOJAS; i++){
                int idLoja = i ;
                executor.execute(new Loja(
                        idLoja,
                        bufferProd,
                        bufferLoja[i]
                ));
            }
        for(int i = 0; i < CLIENTES;i++){
            int idCliente = i;
            executor.execute(new Cliente(
                    idCliente,
                    bufferGarage[i],
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
            System.out.println("Erro ao inicializar o buffer de peças! ");
        }
    }
}
class CircularBuffer<T> {
    private T[] buffer;
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
class FixedBuffer<T> {
    private  Queue<T> buffer = new LinkedList<>();
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
class Funcionario implements Runnable {
    private final CircularBuffer<Veiculo> bufferProducao;
    private final FixedBuffer<Integer> bufferPecas;
    private final int idMesa;
    private final int id;
    private final Ferramenta ferramenta;

    public Funcionario(CircularBuffer<Veiculo> bufferProducao,
                       FixedBuffer<Integer> bufferPecas,
                       int idMesa, int id, Ferramenta ferramenta) {
        this.bufferProducao = bufferProducao;
        this.bufferPecas = bufferPecas;
        this.idMesa = idMesa;
        this.id = id;
        this.ferramenta = ferramenta;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {

                ferramenta.pegar(id-1);

                int peca1 = bufferPecas.remove();

                Veiculo veiculo = new Veiculo(idMesa, id, bufferProducao.size());
                Thread.sleep(2000);
                ferramenta.soltar(id-1);
                bufferProducao.produce(veiculo);

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
class Ferramenta {

    private final Semaphore[] ferramentas;

    public Ferramenta(int numFuncionarios){
        ferramentas = new Semaphore[numFuncionarios];
        for ( int i = 0; i < numFuncionarios; i++){
            ferramentas[i] = new Semaphore(1);
        }
    }

    public void pegar(int idFuncionario) throws InterruptedException {
        int ferramentaEsquerda = idFuncionario;
        int ferramentaDireita = (idFuncionario + 1) % ferramentas.length;

        if(ferramentaEsquerda < ferramentaDireita){
            ferramentas[ferramentaEsquerda].acquire();
            ferramentas[ferramentaDireita].acquire();
        }else {
            ferramentas[ferramentaDireita].acquire();
            ferramentas[ferramentaEsquerda].acquire();
        }
    }

    public void soltar(int idFuncionario){
        int ferramentaEsquerda = idFuncionario;
        int ferramentaDireita = (idFuncionario + 1) % ferramentas.length;

        ferramentas[ferramentaEsquerda].release();
        ferramentas[ferramentaDireita].release();
    }
}

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
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
class Cliente implements Runnable{
    private final int id;
    private final FixedBuffer<Veiculo> garage;

    private final CircularBuffer<Veiculo>[] buffersLojas;
    private static final int TOTAL_PECAS = 500;
    private final Random random = new Random();

    public Cliente(int id, FixedBuffer<Veiculo> garage, CircularBuffer<Veiculo>[] buffersLojas) {
        this.id = id;
        this.garage = garage;
        this.buffersLojas = buffersLojas;

    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int escolha = random.nextInt(buffersLojas.length);
                CircularBuffer<Veiculo> lojaEscolhida = buffersLojas[escolha];
                Veiculo veiculo = lojaEscolhida.consume();
                garage.add(veiculo);
                System.out.printf(
                        "O cliente [%d] " +
                                "comprou o carro de numero [%d] " +
                                "cor [%s] " +
                                "modelo [%s] " +
                                "armazenado na posição [%d] do estoque " +
                                "da loja número [%d] " +
                                "que foi pego da posição [%d] do buffer de produção " +
                                "e fabricado pelo funcionario [%d] " +
                                "da mesa [%d] " +
                                "que usou a peça [%d] do buffer de peças%n%n",
                        id,
                        veiculo.getIdCarro() + 1,
                        veiculo.getColor(),
                        veiculo.getTypo(),
                        veiculo.getPosiLoja(),
                        veiculo.getIdLoja(),
                        veiculo.getPosInicial(),
                        veiculo.getIdFuncionario(),
                        veiculo.getIdMesa(),
                        (veiculo.getIdCarro() % TOTAL_PECAS) + 1
                );
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
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


//ainda falta.
//limite de coleta de pecas por vez.
//arrumar garagem do cliente e talvez lojas.
//socket


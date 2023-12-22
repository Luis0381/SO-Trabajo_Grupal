package so2023_p12_t4;
import java.util.concurrent.Semaphore;

public class SO2023_P12_T4 {
    static final int N = 10; // Tamaño del buffer
    static final int NCONS = 15; // Cantidad de consumidores
    static Semaphore huecos = new Semaphore(N); // Semaforo para los espacios disponibles en el buffer
    static Semaphore mutex = new Semaphore(1); // Semaforo para garantizar la exclusión mutua
    static Semaphore[] vacios = new Semaphore[NCONS];
    static Tipobuffer[] buffer = new Tipobuffer[N]; // El buffer
    static int[] cola = new int[NCONS]; // Cola de consumidores
    static int frente = 0; // Puntero al frente del buffer
    static int elemId = 1; //Id incremental de procesos
    
    static class Tipobuffer {
        String elem;
        int ncon; // Número de consumidores que deben recibir el mensaje
    }
    
    static String producir(String elem) {
        // Simula la producción de un elemento
        return elem;
    }
    
    static void consumir(String elem, int id) {
        // Simula el consumo de un elemento
        System.out.println("Consumido por " + id + ": " + elem);  
    }
    
    static class Productor implements Runnable {
        @Override
        public void run() {
            while (true) {
                String elem = producir("E." + elemId);
                elemId++;
                try {
                    huecos.acquire(); // Espera si no hay espacio en el buffer
                    mutex.acquire(); // Entra en la región crítica
                    System.out.println("Elemento enviado: " + elem); //Muestra cuando se introduce el elemento en el buffer
                    buffer[frente].elem = elem; // Coloca el elemento en el buffer
                    buffer[frente].ncon = NCONS; // Inicializa el número de consumidores
                    frente = (frente + 1) % N; // Actualiza el puntero al frente del buffer
                    for (int i = 0; i < NCONS; i++) {
                        vacios[i].release(); // Despierta a los consumidores
                    }
                    mutex.release(); // Sale de la región crítica
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    static class Consumidor implements Runnable {
        private int id;
        
        public Consumidor(int id) {
            this.id = id;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    vacios[id].acquire(); // Espera a que el consumidor tenga algo que consumir
                    mutex.acquire(); // Entra en la región crítica
                    String elem = buffer[cola[id]].elem; // Obtiene el elemento del buffer
                    buffer[cola[id]].ncon--; // Reduce el número de consumidores que deben recibir el mensaje
                    System.out.println(elem + " fue copiado por " + id);
                    if (buffer[cola[id]].ncon == 0) {
                        huecos.release(); // Si todos los consumidores han recibido el mensaje, libera un espacio
                        System.out.println(buffer[cola[id]].elem + " fue sacado del buffer ");
                    }
                    cola[id] = (cola[id] + 1) % N; // Actualiza la cola
                    mutex.release(); // Sale de la región crítica
                    consumir(elem, id); // Procesa o muestra el elemento
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
        }
    }
    
    public static void main(String[] args) {
        for (int i = 0; i < NCONS; i++) {
            vacios[i] = new Semaphore(0); // Inicializo los semaforos de vacios
        }
        
        for (int i = 0; i < N; i++) {
            buffer[i] = new Tipobuffer(); // Inicializo el buffer
        }
        
        Thread productor = new Thread(new Productor()); // Inicializo el productor
        Thread[] consumidores = new Thread[NCONS]; // NCONS es el número de consumidores
        
        for (int i = 0; i < NCONS; i++) {
            consumidores[i] = new Thread(new Consumidor(i)); // Inicializo los consumidores
        }
        
        productor.start(); // Corro el proceso productor
        for (Thread consumidor : consumidores) {
            consumidor.start(); // Corro los procesos consumidores
        }
    }  
}
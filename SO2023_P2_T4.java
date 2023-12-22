package so2023_p2_t4;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class SO2023_P2_T4 {
    static final int NUM_AUTOS = 30;
    static final int NUM_BARCOS = 30;

    static Semaphore prioridadBarcos = new Semaphore(1); // Semaforo de prioridad a los barcos
    static Semaphore mutexAutos = new Semaphore(1); // Semaforo de mutex para autos
    static Semaphore mutexBarcos = new Semaphore(1); // Semaforo de mutex para barcos
    static Semaphore puenteLibre = new Semaphore(1); // Semaforo que indica si el puente esta libre

    static int barcos = 0; // Cantidad de barcos pasando
    static int autos = 0; // Cantidad de autos pasando

    public static void bajarPuente() {
        // Simula el hecho de bajar el puente para que pasen los autos
        System.out.println("Se baja el puente");
    }

    public static void pasarAuto(int id) {
        // Simul el hecho de que un auto pase por el puente
        System.out.print("Auto " + id + " pasa\n");
    }

    public static void pasarBarco(int id) {
        // Simul el hecho de que un barco pase por el puente
        System.out.print("Barco " + id + " pasa\n");
    }

    public static void levantarPuente() {
        // Simula el hecho de levantar el puente para que pasen los barcos
        System.out.println("Se levanta el puente");
    }

    public static class Auto implements Runnable {
        private final int id;

        public Auto(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                prioridadBarcos.acquire(); // Fijarse si no hay algun barco esperando o cruzando
                prioridadBarcos.release(); // Retorna ese semaforo a su estado anterior

                mutexAutos.acquire(); // Sección crítica de variable compartida autos
                autos++; // Incrementa en 1 la cantidad de autos que estan por cruzar el puente
                if (autos == 1) {
                    puenteLibre.acquire(); // Si el auto es el primero en cruzar, bloquea el puente para que no pasen
                                           // barcos en el medio
                    bajarPuente();
                }
                System.out.println("El auto " + this.id + " está por cruzar");
                mutexAutos.release(); // Fin de sección crítica de variable compartida autos

                // Cruzar puente
                pasarAuto(this.id);
                // Fin cruzr puente

                mutexAutos.acquire(); // Sección crítica de variable compartida autos
                autos--; // Decrementa en 1 la cantidad de autos que estan por cruzar el puente
                if (autos == 0) {
                    puenteLibre.release(); // Si es el último auto en cruzar, vuelve a liberar el puente para que pasen
                                           // barcos
                }
                System.out.println("El auto " + this.id + " terminó de cruzar");
                mutexAutos.release(); // Fin de sección crítica de variable compartida autos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static class Barco implements Runnable {
        private final int id;

        public Barco(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                mutexBarcos.acquire(); // Sección crítica de variable compartida barcos
                barcos++; // Incremento en 1 la cantidad de barcos que estan por pasar
                if (barcos == 1) {
                    prioridadBarcos.acquire(); // Si es el primer barco en llegar, bloquear el acceso a autos nuevos
                    puenteLibre.acquire(); // Si es el primer barco en llegar, esperar que el puente se libere y
                                           // ocuparlo
                    levantarPuente();
                }
                System.out.println("El barco " + this.id + " está por cruzar");
                mutexBarcos.release(); // Fin de sección crítica de variable compartida barcos

                // Cruzar puente
                pasarBarco(this.id);
                // Fin de cruzar puente

                mutexBarcos.acquire(); // Sección crítica de variable compartida barcos
                barcos--; // Decremento en 1 la cantidad de barcos que estan por pasar
                if (barcos == 0) {
                    prioridadBarcos.release(); // Si es el último barco en cruzar habilitar la llegada de nuevos autos
                    puenteLibre.release(); // Si es el último barco en cruzar, liberar el puente
                }
                System.out.println("El barco " + this.id + " termió de cruzar");
                mutexBarcos.release(); // Fin de sección crítica de variable compartida barcos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        double tipo;

        Thread[] arrayAutos = new Thread[NUM_AUTOS];
        Thread[] arrayBarcos = new Thread[NUM_BARCOS];

        for (int i = 0; i < NUM_AUTOS; i++) {
            arrayAutos[i] = new Thread(new Auto(i + 1)); // Inicializo los autoss
        }

        for (int i = 0; i < NUM_BARCOS; i++) {
            arrayBarcos[i] = new Thread(new Barco(i + 1)); // Inicializo los barcos
        }

        for (int i = 0; i < NUM_BARCOS; i++) {
            tipo = Math.ceil(Math.random() * 10);
            Random rand = new Random();
            if (tipo <= 5) {
                arrayAutos[i].start();
                arrayAutos[i].sleep(rand.nextInt(1500));
            } else if (tipo > 4) {
                arrayBarcos[i].start();
                arrayBarcos[i].sleep(rand.nextInt(1500));
            }
        }
    }

}

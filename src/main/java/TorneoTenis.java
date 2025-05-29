
import java.util.*;
import java.util.concurrent.*;

public class TorneoTenis {

    private static final int NUM_JUGADORES = 16;
    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        List<Integer> jugadores = new ArrayList<>();
        for (int i = 1; i <= NUM_JUGADORES; i++) {
            jugadores.add(i);
        }

        String[] rondas = {"OCTAVOS", "CUARTOS", "SEMIFINAL", "FINAL"};
        ExecutorService executor = Executors.newFixedThreadPool(4); // Simular partidos en paralelo

        int rondaActual = 0;
        while (jugadores.size() > 1) {
            System.out.println("\n===== " + rondas[rondaActual] + " =====");

            List<Future<ResultadoPartido>> resultados = new ArrayList<>();
            for (int i = 0; i < jugadores.size(); i += 2) {
                int jugador1 = jugadores.get(i);
                int jugador2 = jugadores.get(i + 1);
                Partido partido = new Partido(jugador1, jugador2);
                Future<ResultadoPartido> futuro = executor.submit(partido);
                resultados.add(futuro);
            }

            jugadores.clear();
            for (Future<ResultadoPartido> futuro : resultados) {
                try {
                    ResultadoPartido resultado = futuro.get(); // Esperar que el partido termine
                    jugadores.add(resultado.ganador);
                    resultado.imprimirResultado();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            rondaActual++;
        }

        System.out.println("\n===== CAMPEÓN DEL TORNEO =====");
        System.out.println("¡¡¡Jugador " + jugadores.get(0) + "!!!");

        executor.shutdown();
    }

    static class Partido implements Callable<ResultadoPartido> {
        private final int jugador1;
        private final int jugador2;

        public Partido(int jugador1, int jugador2) {
            this.jugador1 = jugador1;
            this.jugador2 = jugador2;
        }

        @Override
        public ResultadoPartido call() throws Exception {
            List<Integer> setsGanados = new ArrayList<>(Arrays.asList(0, 0));
            List<String> ganadoresSet = new ArrayList<>();

            for (int i = 1; i <= 3; i++) {
                int ganadorSet = random.nextBoolean() ? jugador1 : jugador2;
                ganadoresSet.add("Set " + i + ": Jugador " + ganadorSet);

                if (ganadorSet == jugador1) {
                    setsGanados.set(0, setsGanados.get(0) + 1);
                } else {
                    setsGanados.set(1, setsGanados.get(1) + 1);
                }

                if (setsGanados.get(0) == 2 || setsGanados.get(1) == 2) {
                    break; // Partido terminado (mejor de 3)
                }
            }

            int ganador = setsGanados.get(0) > setsGanados.get(1) ? jugador1 : jugador2;

            // Simular duración del partido entre 1.5 y 2 segundos
            double tiempo = 1.5 + random.nextDouble() * 0.5;
            Thread.sleep((long) (tiempo * 1000));

            return new ResultadoPartido(jugador1, jugador2, ganadoresSet, ganador);
        }
    }

    static class ResultadoPartido {
        int jugador1, jugador2, ganador;
        List<String> ganadoresSet;

        public ResultadoPartido(int jugador1, int jugador2, List<String> ganadoresSet, int ganador) {
            this.jugador1 = jugador1;
            this.jugador2 = jugador2;
            this.ganadoresSet = ganadoresSet;
            this.ganador = ganador;
        }

        public void imprimirResultado() {
            System.out.println("Jugador " + jugador1 + " vs Jugador " + jugador2);
            for (String s : ganadoresSet) {
                System.out.println(s);
            }
            System.out.println("Ganador del partido: Jugador " + ganador + "\n");
        }
    }
}

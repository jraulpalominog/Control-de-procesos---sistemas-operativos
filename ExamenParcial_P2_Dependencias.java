
public class ExamenParcial_P2_Dependencias {

    public static final int N = 4; // Tamaño de matrices (N x N)
    
    // Recursos compartidos de las tareas
    public static double[][] MatrixA = new double[N+1][N+1];
    public static double[][] MatrixB = new double[N+1][N+1];
    public static double[][] MatrixC = new double[N+1][N+1]; // C = A x B

    // Banderas Booleanas para sincronización (Estilo Cátedra: Sw1, Sw2...)
    public static volatile boolean SwA = false;
    public static volatile boolean SwB = false;
    public static volatile boolean SwC = false;
    public static volatile boolean SwD = false;
    public static volatile boolean SwE = false;
    public static volatile boolean SwF = false;
    public static volatile boolean SwG = false;
    public static volatile boolean SwH = false;

    // -------------------------------------------------------------------------
    // Definición funcional de los 8 Procesos indicados en el examen
    // -------------------------------------------------------------------------
    public static void ReadMatrix_A() {
        System.out.println("[Proceso A] Iniciando ReadMatrix_A()...");
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                MatrixA[i][j] = 10 + (int)(Math.random() * 50);
            }
        }
        delay(150);
        System.out.println("[Proceso A] Matriz A leída exitosamente.");
    }

    public static void ReadMatrix_B() {
        System.out.println("[Proceso B] Iniciando ReadMatrix_B()...");
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                MatrixB[i][j] = 5 + (int)(Math.random() * 40);
            }
        }
        delay(150);
        System.out.println("[Proceso B] Matriz B leída exitosamente.");
    }

    public static void Producto_AxB() {
        System.out.println("[Proceso C] Iniciando Producto_AxB() [Depende de A y B]...");
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                double suma = 0;
                for (int k = 1; k <= N; k++) {
                    suma += MatrixA[i][k] * MatrixB[k][j];
                }
                MatrixC[i][j] = suma;
            }
        }
        delay(120);
        System.out.println("[Proceso C] Producto A x B calculado.");
    }

    public static void Descomposicion_LU() {
        System.out.println("  [Proceso D] Descomposicion_LU() [Rama 1, depende de C]...");
        delay(100);
        System.out.println("  [Proceso D] Factorización LU completada.");
    }

    public static void Descomposicion_QR() {
        System.out.println("    [Proceso E] Descomposicion_QR() [Rama 2, depende de C]...");
        delay(130);
        System.out.println("    [Proceso E] Factorización QR completada.");
    }

    public static void Print_HTML_LU() {
        System.out.println("  [Proceso F] Print_HTML_LU() [Depende de D]...");
        delay(80);
        System.out.println("  [Proceso F] Reporte HTML de LU generado.");
    }

    public static void Print_HTML_QR() {
        System.out.println("    [Proceso G] Print_HTML_QR() [Depende de E]...");
        delay(90);
        System.out.println("    [Proceso G] Reporte HTML de QR generado.");
    }

    public static void Resumen() {
        System.out.println("[Proceso H] Generando Resumen() [Convergencia final de F y G]...");
        delay(50);
        System.out.println("[Proceso H] Proceso final completado exitosamente.");
    }

    public static void delay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // =========================================================================
    // 1. EJECUCIÓN SERIAL: A -> B -> C -> D -> E -> F -> G -> H
    // =========================================================================
    public static void EjecutarSerial() {
        System.out.println("\n=============================================");
        System.out.println("      EJECUCIÓN SERIAL (A -> B -> ... -> H)  ");
        System.out.println("=============================================");
        long INI = System.currentTimeMillis();

        ReadMatrix_A();
        ReadMatrix_B();
        Producto_AxB();
        Descomposicion_LU();
        Descomposicion_QR();
        Print_HTML_LU();
        Print_HTML_QR();
        Resumen();

        long FIN = System.currentTimeMillis();
        System.out.println("Tiempo Total Serial: " + (FIN - INI) + " ms");
    }

    // =========================================================================
    // 2. EJECUCIÓN PARALELA CON DEPENDENCIAS FUNCIONALES
    // =========================================================================
    public static void EjecutarParalelo() throws InterruptedException {
        System.out.println("\n=============================================");
        System.out.println("    EJECUCIÓN PARALELA MULTITHREADING (DAG)  ");
        System.out.println("=============================================");
        long INI = System.currentTimeMillis();

        SwA = false; SwB = false; SwC = false; SwD = false;
        SwE = false; SwF = false; SwG = false; SwH = false;

        // --- NIVEL 1: Proceso A y Proceso B en Paralelo ---
        new Thread(new Runnable() {
            public void run() {
                ReadMatrix_A();
                SwA = true;
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                ReadMatrix_B();
                SwB = true;
            }
        }).start();

        // Espera de dependencias: C espera a A y B
        do {
            Thread.sleep(10);
        } while (!SwA || !SwB);

        // --- NIVEL 2: Proceso C ---
        Producto_AxB();
        SwC = true;

        // --- NIVEL 3 y 4: Ramas Concurrentes (D -> F) y (E -> G) ---
        // Rama 1: D -> F
        new Thread(new Runnable() {
            public void run() {
                Descomposicion_LU();
                SwD = true;
                Print_HTML_LU();
                SwF = true;
            }
        }).start();

        // Rama 2: E -> G
        new Thread(new Runnable() {
            public void run() {
                Descomposicion_QR();
                SwE = true;
                Print_HTML_QR();
                SwG = true;
            }
        }).start();

        // Espera de convergencia: H espera a F y G
        do {
            Thread.sleep(10);
        } while (!SwF || !SwG);

        // --- NIVEL 5: Proceso H ---
        Resumen();
        SwH = true;

        long FIN = System.currentTimeMillis();
        System.out.println("Tiempo Total Paralelo: " + (FIN - INI) + " ms");
    }

    public static void main(String[] args) throws InterruptedException {
        EjecutarSerial();
        EjecutarParalelo();
    }
}

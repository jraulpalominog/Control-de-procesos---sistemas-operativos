
public class ExamenParcial_P1_TimeSharing {

    // Estados del ciclo de vida del proceso
    public enum Status { READY, RUN, WAIT, CANCEL, FINISH }

    // Estructura del Proceso / PCB (Process Control Block)
    public static class NodoProceso {
        int id;             // PID
        String name;        // Nombre (ej: Task 1)
        int byteSize;       // Tamaño en Memoria RAM
        Status status;      // Estado actual
        int timeQuotaMs;    // Tiempo restante de ejecución (ms)
        int priority;       // Prioridad (a mayor valor, mayor prioridad)
        NodoProceso next;   // PTR^.NEXT (Lista enlazada)

        public NodoProceso(int id, String name, int byteSize, int timeQuotaMs, int priority) {
            this.id = id;
            this.name = name;
            this.byteSize = byteSize;
            this.status = Status.READY;
            this.timeQuotaMs = timeQuotaMs;
            this.priority = priority;
            this.next = null;
        }

        @Override
        public String toString() {
            return String.format("[PID: %d | %-8s | %6d Bytes | Estado: %-6s | Restante: %3d ms | Prio: %d]",
                    id, name, byteSize, status, timeQuotaMs, priority);
        }
    }

    // =========================================================================
    // RECURSOS DEL SISTEMA: 1 MEMORIA Y 1 PROCESADOR
    // =========================================================================
    public static int MEMORIA_TOTAL = 1024 * 1024; // 1 MB de Memoria Física
    public static int MEMORIA_DISPONIBLE = MEMORIA_TOTAL;
    public static final int QUANTUM_MS = 25; // Cuota Time-Sharing por turno (ms)

    // Punteros del Sistema Operativo
    public static NodoProceso INI = null;
    public static NodoProceso FIN = null;

    // Componente Timer del SO
    public static long timerStartMs = 0;

    // -------------------------------------------------------------------------
    // Funciones del Cronómetro / Timer del SO solicitadas en el examen
    // -------------------------------------------------------------------------
    public static void InitialTimer() {
        timerStartMs = System.currentTimeMillis();
    }

    public static void SetTime(int H, int M, int S, int d, int c, int m) {
        // Reinicia o calibra el temporizador base del sistema
        timerStartMs = System.currentTimeMillis();
    }

    public static String GetTime() {
        long elapsed = System.currentTimeMillis() - timerStartMs;
        long s = (elapsed / 1000) % 60;
        long m = (elapsed / (1000 * 60)) % 60;
        long h = (elapsed / (1000 * 60 * 60));
        long ms = elapsed % 1000;
        return String.format("%02d:%02d:%02d.%03d", h, m, s, ms);
    }

    // -------------------------------------------------------------------------
    // Gestión de Memoria y Tabla de Procesos
    // -------------------------------------------------------------------------
    public static boolean AssignMemoryToProcess(NodoProceso p) {
        if (p.byteSize <= MEMORIA_DISPONIBLE) {
            MEMORIA_DISPONIBLE -= p.byteSize;
            return true;
        }
        System.out.println("  [ERROR DE MEMORIA] RAM insuficiente para el proceso: " + p.name);
        return false;
    }

    public static void RemoveProcessFromMemory(NodoProceso p) {
        MEMORIA_DISPONIBLE += p.byteSize;
        if (MEMORIA_DISPONIBLE > MEMORIA_TOTAL) MEMORIA_DISPONIBLE = MEMORIA_TOTAL;
        System.out.println("  [MEMORIA] Memoria liberada por " + p.name + " (" + p.byteSize + " Bytes).");
    }

    public static void AdicionarProceso(NodoProceso nuevo) {
        if (!AssignMemoryToProcess(nuevo)) {
            nuevo.status = Status.CANCEL;
            return;
        }
        // Inserción en la lista dinámica
        if (INI == null) {
            INI = nuevo;
            FIN = nuevo;
        } else {
            FIN.next = nuevo;
            FIN = nuevo;
        }
    }

    // GetProcessFromTable(): Obtiene el proceso READY con mayor prioridad (PTR.priority)
    public static NodoProceso GetProcessFromTable() {
        NodoProceso PTR = INI;
        NodoProceso AUX = null;

        while (PTR != null) {
            if (PTR.status == Status.READY) {
                if (AUX == null || PTR.priority > AUX.priority) {
                    AUX = PTR;
                }
            }
            PTR = PTR.next;
        }
        return AUX;
    }

    public static void FinishProcess(NodoProceso p) {
        p.status = Status.FINISH;
        p.timeQuotaMs = 0;
        RemoveProcessFromMemory(p);
        System.out.println("  [SO] Proceso " + p.name + " finalizó su ciclo con éxito.");
    }

    public static void CancelProcess(NodoProceso p) {
        p.status = Status.CANCEL;
        RemoveProcessFromMemory(p);
        System.out.println("  [SO] Proceso " + p.name + " ha sido cancelado.");
    }

    // -------------------------------------------------------------------------
    // Despachador del Procesador (Time-Sharing Scheduler)
    // -------------------------------------------------------------------------
    public static void ExecuteProcess() {
        System.out.println("\n=================================================================");
        System.out.println("          INICIO DE PLANIFICACIÓN TIME SHARING (CPU ÚNICO)       ");
        System.out.println("=================================================================");
        InitialTimer();

        int ciclo = 1;
        while (true) {
            NodoProceso actual = GetProcessFromTable();
            if (actual == null) {
                // Verificar si todos terminaron
                boolean pendientes = false;
                NodoProceso ptr = INI;
                while (ptr != null) {
                    if (ptr.status == Status.READY || ptr.status == Status.RUN) {
                        pendientes = true;
                        break;
                    }
                    ptr = ptr.next;
                }
                if (!pendientes) break;
            }

            // Asignar el procesador (READY -> RUN)
            actual.status = Status.RUN;
            System.out.println("\n[Ciclo " + ciclo + " | Timer: " + GetTime() + "] CPU ejecutando: " + actual.name + " (Prioridad: " + actual.priority + ")");

            // Simular consumo de cuanto (Quantum)
            int tiempoEjecutado = Math.min(actual.timeQuotaMs, QUANTUM_MS);
            actual.timeQuotaMs -= tiempoEjecutado;

            try {
                Thread.sleep(15); // Simulación de tiempo real del SO
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Evaluar condición de salida de la ráfaga
            if (actual.timeQuotaMs <= 0) {
                FinishProcess(actual);
            } else {
                // Time Sharing: Se agota la cuota y retorna a READY para dar paso al siguiente
                actual.status = Status.READY;
                System.out.println("  [TIME SHARING] Cuota agotada para " + actual.name + ". Regresa a READY (Resta: " + actual.timeQuotaMs + " ms).");
            }

            MostrarTablaProcesos();
            ciclo++;
        }
        System.out.println("\n[FIN] Todas las tareas han finalizado. Memoria disponible: " + MEMORIA_DISPONIBLE + " Bytes.");
    }

    public static void MostrarTablaProcesos() {
        System.out.println("---------------- TABLA DE PROCESOS (RAM DISP: " + MEMORIA_DISPONIBLE + " Bytes) ----------------");
        NodoProceso PTR = INI;
        while (PTR != null) {
            System.out.println("  " + PTR.toString());
            PTR = PTR.next;
        }
        System.out.println("---------------------------------------------------------------------------------");
    }

    public static void main(String[] args) {
        // Cargamos 4 tareas de ejemplo con diferentes cuotas de tiempo y prioridades
        AdicionarProceso(new NodoProceso(101, "Task 1", 120000, 75, 2));
        AdicionarProceso(new NodoProceso(102, "Task 2", 250000, 150, 4));
        AdicionarProceso(new NodoProceso(103, "Task 3", 80000, 50, 1));
        AdicionarProceso(new NodoProceso(104, "Task 4", 180000, 100, 3));

        MostrarTablaProcesos();
        ExecuteProcess();
    }
}

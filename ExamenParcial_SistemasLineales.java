
public class ExamenParcial_SistemasLineales {

    public static volatile boolean SwGauss = false;
    public static volatile boolean SwJordan = false;
    public static volatile boolean SwCramer = false;

    // Matriz y vector aumentados de prueba (3x3)
    public static double[][] A_orig = {
        {0, 0, 0, 0},
        {0, 2, 1, -1},
        {0, -3, -1, 2},
        {0, -2, 1, 2}
    };
    public static double[] B_orig = {0, 8, -11, -3};

    // Método 1: Eliminación Gaussiana
    public static void MetodoGauss(double[][] A, double[] B) {
        long ini = System.currentTimeMillis();
        int n = B.length - 1;
        double[][] a = clonarMatriz(A);
        double[] b = B.clone();
        double[] x = new double[n + 1];

        for (int k = 1; k <= n - 1; k++) {
            for (int i = k + 1; i <= n; i++) {
                double factor = a[i][k] / a[k][k];
                for (int j = k; j <= n; j++) a[i][j] -= factor * a[k][j];
                b[i] -= factor * b[k];
            }
        }
        for (int i = n; i >= 1; i--) {
            double sum = b[i];
            for (int j = i + 1; j <= n; j++) sum -= a[i][j] * x[j];
            x[i] = sum / a[i][i];
        }
        long fin = System.currentTimeMillis();
        System.out.println("[Hilo Gauss] Solución: x1=" + x[1] + ", x2=" + x[2] + ", x3=" + x[3] + " | Tiempo: " + (fin - ini) + " ms");
    }

    // Método 2: Gauss-Jordan
    public static void MetodoGaussJordan(double[][] A, double[] B) {
        long ini = System.currentTimeMillis();
        int n = B.length - 1;
        double[][] m = new double[n + 1][n + 2];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) m[i][j] = A[i][j];
            m[i][n + 1] = B[i];
        }

        for (int i = 1; i <= n; i++) {
            double pvt = m[i][i];
            for (int j = 1; j <= n + 1; j++) m[i][j] /= pvt;
            for (int k = 1; k <= n; k++) {
                if (k != i) {
                    double factor = m[k][i];
                    for (int j = 1; j <= n + 1; j++) m[k][j] -= factor * m[i][j];
                }
            }
        }
        long fin = System.currentTimeMillis();
        System.out.println("[Hilo Gauss-Jordan] Solución: x1=" + m[1][n+1] + ", x2=" + m[2][n+1] + ", x3=" + m[3][n+1] + " | Tiempo: " + (fin - ini) + " ms");
    }

    // Método 3: Regla de Cramer (determinantes 3x3)
    public static void MetodoCramer(double[][] A, double[] B) {
        long ini = System.currentTimeMillis();
        double detA = det3(A[1][1], A[1][2], A[1][3], A[2][1], A[2][2], A[2][3], A[3][1], A[3][2], A[3][3]);
        double detX1 = det3(B[1], A[1][2], A[1][3], B[2], A[2][2], A[2][3], B[3], A[3][2], A[3][3]);
        double detX2 = det3(A[1][1], B[1], A[1][3], A[2][1], B[2], A[2][3], A[3][1], B[3], A[3][3]);
        double detX3 = det3(A[1][1], A[1][2], B[1], A[2][1], A[2][2], B[2], A[3][1], A[3][2], B[3]);

        double x1 = detX1 / detA;
        double x2 = detX2 / detA;
        double x3 = detX3 / detA;
        long fin = System.currentTimeMillis();
        System.out.println("[Hilo Cramer] Solución: x1=" + x1 + ", x2=" + x2 + ", x3=" + x3 + " | Tiempo: " + (fin - ini) + " ms");
    }

    private static double det3(double a, double b, double c, double d, double e, double f, double g, double h, double i) {
        return a*(e*i - f*h) - b*(d*i - f*g) + c*(d*h - e*g);
    }

    private static double[][] clonarMatriz(double[][] m) {
        double[][] c = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) c[i] = m[i].clone();
        return c;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- RESOLUCIÓN PARALELA DE SISTEMAS DE ECUACIONES (MULTITHREADING) ---");
        new Thread(() -> { MetodoGauss(A_orig, B_orig); SwGauss = true; }).start();
        new Thread(() -> { MetodoGaussJordan(A_orig, B_orig); SwJordan = true; }).start();
        new Thread(() -> { MetodoCramer(A_orig, B_orig); SwCramer = true; }).start();

        do { Thread.sleep(10); } while (!SwGauss || !SwJordan || !SwCramer);
        System.out.println("Todos los hilos matemáticos han convergido.");
    }
}

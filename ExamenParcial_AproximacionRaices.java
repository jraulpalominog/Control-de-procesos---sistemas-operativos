
public class ExamenParcial_AproximacionRaices {

    public static double f(double x) { return Math.pow(x, 3) - x - 2; }
    public static double df(double x) { return 3 * Math.pow(x, 2) - 1; }

    public static void Biseccion(double a, double b, double tol) {
        long ini = System.currentTimeMillis();
        double c = a;
        while ((b - a) / 2.0 > tol) {
            c = (a + b) / 2.0;
            if (f(c) == 0.0) break;
            else if (f(a) * f(c) < 0) b = c;
            else a = c;
        }
        long fin = System.currentTimeMillis();
        System.out.println("[Bisección] Raíz: " + c + " | Tiempo: " + (fin - ini) + " ms");
    }

    public static void RegulaFalsi(double a, double b, double tol) {
        long ini = System.currentTimeMillis();
        double c = a;
        for (int i = 0; i < 1000; i++) {
            c = (a * f(b) - b * f(a)) / (f(b) - f(a));
            if (Math.abs(f(c)) < tol) break;
            if (f(a) * f(c) < 0) b = c;
            else a = c;
        }
        long fin = System.currentTimeMillis();
        System.out.println("[Regula Falsi] Raíz: " + c + " | Tiempo: " + (fin - ini) + " ms");
    }

    public static void NewtonRaphson(double x0, double tol) {
        long ini = System.currentTimeMillis();
        double x = x0;
        for (int i = 0; i < 1000; i++) {
            double nextX = x - f(x) / df(x);
            if (Math.abs(nextX - x) < tol) {
                x = nextX;
                break;
            }
            x = nextX;
        }
        long fin = System.currentTimeMillis();
        System.out.println("[Newton-Raphson] Raíz: " + x + " | Tiempo: " + (fin - ini) + " ms");
    }

    public static void main(String[] args) {
        System.out.println("--- RESOLUCIÓN PARALELA DE RAÍCES NUMÉRICAS ---");
        new Thread(() -> Biseccion(1.0, 2.0, 1e-6)).start();
        new Thread(() -> RegulaFalsi(1.0, 2.0, 1e-6)).start();
        new Thread(() -> NewtonRaphson(1.5, 1e-6)).start();
    }
}

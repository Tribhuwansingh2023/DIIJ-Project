package tribhuwansingh_2341019538.util;

public class BenchmarkUtil {
    private BenchmarkUtil() {
    }

    public static double measureMillis(CheckedRunnable runnable) throws Exception {
        long start = System.nanoTime();
        runnable.run();
        long end = System.nanoTime();
        return (end - start) / 1_000_000.0;
    }

    public static double average(double[] values) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return values.length == 0 ? 0.0 : total / values.length;
    }

    public static double throughput(int records, double millis) {
        if (millis <= 0.0) {
            return 0.0;
        }
        return records / (millis / 1000.0);
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }
}

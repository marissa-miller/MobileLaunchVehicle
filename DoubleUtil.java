public class DoubleUtil {
    public static final double EPSILON = 1e-9;

    // Approximate ==
    public static boolean equals(double a, double b) {
        return Math.abs(a - b) <= EPSILON;
    }

    // Approximate >=
    public static boolean greaterThanOrEqual(double a, double b) {
        return a >= (b - EPSILON);
    }

    // Approximate <=
    public static boolean lessThanOrEqual(double a, double b) {
        return a <= (b + EPSILON);
    }
}
package ar.edu.itba.cripto.secret_image.math_utils;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class implementing methods to operate with polynomial.
 */
public class PolynomialUtils {


    /**
     * Calculate coefficients of a modulus polynomial that contains the given {@code points}.
     *
     * @param points A {@link Map} containing the points included in the polynomial
     *               (i.e the keys are 'x' values, and the values are 'y' values).
     * @param mod    The modulus to apply.
     * @return A list containing the coefficients of the polynomial, sorted by degree.
     */
    public static List<Integer> getCoefficients(Map<Integer, Integer> points, int mod) {

        final PrimeField field = new PrimeField(mod);
        final int degree = points.size();
        final Matrix<Integer> coefficientsMatrix = new Matrix<>(points.entrySet().stream()
                .parallel()
                .mapToInt(Map.Entry::getKey)
                .mapToObj(base -> IntStream.range(0, degree)
                        .map(exp -> auxPow( base, exp, mod))
                        .map(each -> each % mod)
                        .mapToObj(each -> each)
                        .toArray(Integer[]::new))
                .toArray(Integer[][]::new), field);

        final Matrix<Integer> independentTermsMatrix = new Matrix<>(points.entrySet().stream()
                .parallel()
                .mapToInt(Map.Entry::getValue)
                .map(each -> each % mod)
                .mapToObj(each -> Stream.of(each).toArray(Integer[]::new))
                .toArray(Integer[][]::new), field);


        final Matrix<Integer> equationMatrix = coefficientsMatrix.appendColumns(independentTermsMatrix)
                .reducedRowEchelonForm();
        return Arrays.asList(equationMatrix.getColumn(degree));

    }

    private static int auxPow(int base, int exp, int mod) {
            int result = 1;
            for(int pow = 0; pow<exp; pow++){
                result *= base;
                result %= mod;
            }
            return result;
    }
}

package ar.edu.itba.cripto.secret_image.math_utils;

import ar.edu.itba.cripto.secret_image.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Perform tests over {@link PolynomialUtils} methods.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class PolynomialUtilsTest {

    private static final String GET_COEFFICIENTS_ERROR_MESSAGE =
            "PolynomialUtils#getCoefficients did not return as expected.";

    @Test
    public void testCoefficientsAreOk() {
        final Map<Integer, Integer> points = new HashMap<>();
        points.put(2, 5);
        points.put(3, 1);
        points.put(7, 10);
        points.put(9, 21);

        List<Integer> coefficients = PolynomialUtils.getCoefficients(points, 257);

        Assert.assertNotNull(GET_COEFFICIENTS_ERROR_MESSAGE + " Expecting a List of integers. Got null", coefficients);
        Assert.assertTrue(GET_COEFFICIENTS_ERROR_MESSAGE + " Expecting to contain 89", coefficients.contains(89));
        Assert.assertTrue(GET_COEFFICIENTS_ERROR_MESSAGE + " Expecting to contain 186", coefficients.contains(186));
        Assert.assertTrue(GET_COEFFICIENTS_ERROR_MESSAGE + " Expecting to contain 30", coefficients.contains(30));
        Assert.assertTrue(GET_COEFFICIENTS_ERROR_MESSAGE + " Expecting to contain 185", coefficients.contains(185));

        IntStream.range(0, 256)
                .filter(each -> each != 89 && each != 186 && each != 30 && each != 185)
                .forEach(each ->
                        Assert.assertFalse(GET_COEFFICIENTS_ERROR_MESSAGE + " Expecting not to contain " + each,
                                coefficients.contains(each)));
    }
}

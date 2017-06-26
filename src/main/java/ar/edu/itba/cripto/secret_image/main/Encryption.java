package ar.edu.itba.cripto.secret_image.main;


import ar.edu.itba.cripto.secret_image.bmp.BmpEditor;
import ar.edu.itba.cripto.secret_image.bmp.BmpUtils;
import ar.edu.itba.cripto.secret_image.main.util.PseudoTable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class implementing logic to perform encryption.
 */
public class Encryption {

    /**
     * The minimum amount of shadow images where the secret will be hidden.
     */
    private final int k;
    /**
     * The amount of shadows to be created.
     */
    private final int n;
    /**
     * The path to the secret image (i.e that one to be hidden).
     */
    private final String secretImagePath;
    /**
     * A {@link List} containing the path to each shadow image.
     */
    private final List<String> shadowPaths;

    /**
     * Constructor.
     *
     * @param k               The minimum amount of shadow images where the secret will be hidden.
     * @param n               The amount of shadows to be created.
     * @param secretImagePath The path to the secret image (i.e that one to be hidden).
     * @param directory       The path to the directory holding the images to be used as shadows.
     */
    public Encryption(int k, Integer n, String secretImagePath, String directory) {
        if (k < 2) {
            throw new IllegalArgumentException("Number of shadows must be at least 2");
        }
        if (k > 257) {
            throw new IllegalArgumentException("The maximum amount of shadows is 257");
        }
        if (secretImagePath == null) {
            throw new IllegalArgumentException("Null secret image path");
        }
        if (directory == null) {
            throw new IllegalArgumentException("Null directory");
        }

        //noinspection ConstantConditions
        this.shadowPaths =
                Arrays.stream(Optional.of(new File(directory).listFiles((dir, name) -> name.endsWith(".bmp")))
                        .orElse(new File[0]))
                        .map(File::getPath)
                        .collect(Collectors.toList());
        this.k = k;
        this.n = n == null ? shadowPaths.size() : n;
        this.secretImagePath = secretImagePath;

        if (shadowPaths.size() < k) {
            throw new IllegalArgumentException("More shadows are needed");
        }
    }

    /**
     * Performs the encryption process according to the set parameters.
     */
    public void encrypt() {

        // Mocking BMPUtil
        BmpUtils bmpUtil = null;
        try {
            bmpUtil = new BmpUtils(secretImagePath);
            bmpUtil.setBytesFromIterator(k);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        int imageSize = (int) bmpUtil.getImageSize();

        if (imageSize % k != 0) {
            throw new IllegalStateException("Image to encrypt need to be of a size divisible by k");
        }


        Random random = new Random();

        int seed = random.nextInt(65536);

        List<Integer> pseudoTable = PseudoTable.generatePseudoTable(imageSize, seed);

        List<List<Integer>> evalsList = new ArrayList<>();

        int i = 0;
        for (List<Integer> coefficients : bmpUtil) {
            List<Integer> newCoefficients = new ArrayList<>();
            for (int j = 0; j < coefficients.size(); j++) {
                newCoefficients.add(coefficients.get(j) ^ pseudoTable.get(i * coefficients.size() + j));
            }
            i++;
            List<Integer> evals = evalPolynomial(newCoefficients, n);
            evalsList.add(evals);
        }

        for (int j = 0; j < n; j++) {
            BmpUtils shadow = null;
            try {
                shadow = new BmpUtils(shadowPaths.get(j));
                shadow.setBytesFromIterator(k);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (shadow.getImageSize() != (imageSize / k) * 8) {
                throw new IllegalStateException("shadow size is not of correct size");
            }

            BmpEditor editor = shadow.edit();
            editor.editSeed(seed);
            editor.editShadow(j + 1);
            for (List<Integer> anEvalsList : evalsList) {
                editor.insertSecret(anEvalsList.get(j));
            }
            editor.saveImage();
        }
    }


    /**
     * Returns a {@link List} of values that are the polynomial whose coefficients are the given {@code newCoefficients}
     * using x all values between 1 and {@code n}.
     *
     * @param newCoefficients The polynomial coefficients.
     * @param n               The range of values in which polynomial will be evaluated
     * @return The evaluations of the polynomial.
     */
    private List<Integer> evalPolynomial(List<Integer> newCoefficients, int n) {
        boolean overflow = true;
        List<Integer> evals = null;

        while (overflow) {
            evals = new ArrayList<>();
            overflow = false;
            for (int x = 1; x <= n && !overflow; x++) {
                int eval = 0;
                for (int i = 0; i < newCoefficients.size(); i++) {
                    int powerX = 1;
                    for (int pow = 0; pow < i; pow++) {
                        powerX *= x;
                        powerX %= 257;
                    }
                    eval += newCoefficients.get(i) * powerX;
                    eval %= 257;
                }
                if (eval == 256) {
                    overflow = true;
                    boolean flag = true;
                    for (int i = 0; i < newCoefficients.size() && flag; i++) {
                        if (newCoefficients.get(i) != 0) {
                            newCoefficients.set(i, newCoefficients.get(i) - 1);
                            flag = false;
                        }
                    }
                } else {
                    evals.add(x - 1, eval);
                }
            }
        }
        return evals;
    }
}

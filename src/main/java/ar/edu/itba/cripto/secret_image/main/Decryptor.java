package ar.edu.itba.cripto.secret_image.main;

import ar.edu.itba.cripto.secret_image.bmp.BmpEditor;
import ar.edu.itba.cripto.secret_image.bmp.BmpUtils;
import ar.edu.itba.cripto.secret_image.main.util.PseudoTable;
import ar.edu.itba.cripto.secret_image.math_utils.PolynomialUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class implementing logic to perform decryption.
 */
public class Decryptor {

    /**
     * The amount of shadow images needed to recover the secret image.
     */
    private final int k;
    /**
     * The path to the secret image (i.e where it must be saved).
     */
    private final String secretImagePath;
    /**
     * A {@link List} containing the path to each shadow image.
     */
    private final List<String> shadowPaths;

    /**
     * Constructor.
     *
     * @param k               The amount of shadow images needed to recover the secret image.
     * @param secretImagePath The path to the secret image (i.e where it must be saved).
     * @param directory       A {@link List} containing the path to each shadow image.
     */
    public Decryptor(int k, String secretImagePath, String directory) {
        if (k < 2) {
            throw new IllegalArgumentException("Number of shadows must be at least 2");
        }
        if (directory == null) {
            throw new IllegalArgumentException("Null directory");
        }
        this.k = k;
        this.secretImagePath = secretImagePath;
        //noinspection ConstantConditions
        this.shadowPaths =
                Arrays.stream(Optional.of(new File(directory).listFiles((dir, name) -> name.endsWith(".bmp")))
                        .orElse(new File[0]))
                        .map(File::getPath)
                        .collect(Collectors.toList()).subList(0, k);

        if (shadowPaths.size() < k) {
            throw new IllegalArgumentException("More shadows are needed");
        }
    }


    /**
     * Performs the encryption process according to the set parameters.
     */
    public void decrypt() {


        if (k < 2) {
            throw new IllegalArgumentException("Number of shadows must be at least 2");
        }
        ArrayList<BmpUtils> images = new ArrayList<>();

        int size = -1;
        int seed = -1;

        Set<Integer> shadowNumbers = new HashSet<>();
        for (int i = 0; i < k; i++) {
            String path = shadowPaths.get(i);
            BmpUtils bmpUtils;
            try {
                bmpUtils = new BmpUtils(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            shadowNumbers.add(bmpUtils.getShadow());
            if (i == 0) {
                size = (int) bmpUtils.getImageSize();
                if (size % 8 != 0) {
                    //TODO
//                    throw new IllegalArgumentException("Shadow image size is not divisible by 8");
                }
                seed = bmpUtils.getSeed();
            } else {
                if (size != (int) bmpUtils.getImageSize()) {
                    throw new IllegalArgumentException("Size of shadows is not consistent");
                }
                if (seed != bmpUtils.getSeed()) {
                    throw new IllegalArgumentException("Seed of shadows is not consistent");
                }
            }
            images.add(bmpUtils);
        }
        if (shadowNumbers.size() != k) {
            throw new IllegalArgumentException("Repeated shadow numbers");
        }

        int numPolynomes = size / 8;

        ArrayList<Map<Integer, Integer>> evaluatedPolynomesMap = new ArrayList<>();
        for (int i = 0; i < numPolynomes; i++) {
            evaluatedPolynomesMap.add(new HashMap<>());
        }

        /* Get all bytes hiden in all the shadows */
        for (int i = 0; i < k; i++) {
            BmpUtils bmpUtils = images.get(i);
            int shadowNumber = bmpUtils.getShadow();
            int m = 0;
            bmpUtils.setBytesFromIterator(8);
            for (List<Integer> byteArray : bmpUtils) {
                int secretByte = 0;
                for (int j = 0; j < 8; j++) {
                    int b = byteArray.get(j);
                    secretByte <<= 1;
                    int secretBit = b & 0x01;
                    secretByte |= secretBit;
                }
                evaluatedPolynomesMap.get(m).put(shadowNumber, secretByte);
                m++;
            }
        }
        /* Use hidden bytes to form the polynomes */
        List<Integer> resultBytes = new ArrayList<>();
        List<Integer> permutationTable = PseudoTable.generatePseudoTable(k * numPolynomes, seed);
        for (int polynomeNumber = 0; polynomeNumber < numPolynomes; polynomeNumber++) {
            List<Integer> coefficients = PolynomialUtils.getCoefficients(evaluatedPolynomesMap.get(polynomeNumber), 257);
            if (coefficients.size() != k) {
                throw new IllegalStateException("coefficients != k");
            }
            for (int coefficientNumber = 0; coefficientNumber < k; coefficientNumber++) {
                resultBytes.add(coefficients.get(coefficientNumber) ^ permutationTable.get(polynomeNumber * k + coefficientNumber));
            }
        }

        BmpEditor secret = new BmpEditor(secretImagePath, resultBytes, images.get(0), k);
        secret.saveImage();
    }
}

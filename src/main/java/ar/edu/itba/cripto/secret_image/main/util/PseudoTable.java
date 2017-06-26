package ar.edu.itba.cripto.secret_image.main.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class implements a method to create a list of random numbers.
 */
public class PseudoTable {

    /**
     * Private constructor to avoid instantiation.
     */
    private PseudoTable() {
    }


    /**
     * Creates a list of random numbers of size {@code imageSize}, using the given {@code seed}.
     *
     * @param imageSize The size of image raw data
     * @param seed      The seed to be use in {@link Random} creation.
     * @return A {@link List} of random numbers.
     */
    public static List<Integer> generatePseudoTable(int imageSize, int seed) {
        Random random = new Random(seed);
        List<Integer> pseudoTable = new ArrayList<>();
        for (int i = 0; i < imageSize; i++) {
            pseudoTable.add(random.nextInt(256));
        }
        return pseudoTable;
    }
}

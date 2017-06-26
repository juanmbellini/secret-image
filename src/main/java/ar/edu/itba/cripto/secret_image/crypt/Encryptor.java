package ar.edu.itba.cripto.secret_image.crypt;


import ar.edu.itba.cripto.secret_image.bmp.BmpImage;
import ar.edu.itba.cripto.secret_image.bmp.WritableBmpImage;
import ar.edu.itba.cripto.secret_image.io.BmpFileIO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Class implementing logic to perform encryption.
 */
public class Encryptor {

    private final int k;
    private final BmpImage secretImage;
    private final List<WritableBmpImage> shadows;

    public Encryptor(int k, int n, String secretImagePath, String directory) {
        // TODO: check values

        this.k = k;
        this.secretImage = BmpFileIO.getSecretImage(secretImagePath, k);
        this.shadows = BmpFileIO.getShadowImages(directory, k);
    }


    /**
     * Performs the encryption according to the set parameters.
     *
     * @return a {@link List} containing the {@link WritableBmpImage} with the hidden secret.
     */
    public List<WritableBmpImage> encrypt() {
        final int imageSize = (int) secretImage.getImageSize();
        if (imageSize % k != 0) {
            throw new IllegalStateException("Image to encrypt need to be of a size divisible by k");
        }
        final Random random = new Random();
        final int seed = random.nextInt(0xFFFF + 1);
        final PseudoTable pseudoTable = new PseudoTable(imageSize, seed, k);

        final TableAndImageWrapper wrapper = new TableAndImageWrapper(pseudoTable, secretImage);

        final List<List<Integer>> evalList = StreamSupport.stream(wrapper.spliterator(), false)
                .map(Block::applyXor)
                .map(each -> evalPolynomial(each, shadows.size()))
                .collect(Collectors.toList());


        // Repeat for each shadow that must be created.
        IntStream.range(0, shadows.size())
                .parallel()
                .forEach(j -> {
                    WritableBmpImage shadow = shadows.get(j);
                    if (shadow.getImageSize() != (imageSize / k) * 8) {
                        throw new IllegalStateException("shadow size is not of correct size");
                    }
                    shadow.setSeed(seed);
                    shadow.setShadow(j + 1);
                    evalList.stream().map(each -> each.get(j))
                            .sequential() // Not thread safe
                            .forEach(shadow::insertSecret);
                });


        return shadows;
    }


    /**
     * @param newCoefficients coefficients of polynomial
     * @param n               range of values in which polynomial will be evaluated
     * @return evaluations of polynomial with x from 1 to n
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
//                    eval += newCoefficients.get(i) * Math.pow(x, i);
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


    /**
     * Object wrapping a {@link PseudoTable} and a {@link BmpImage}
     * in order to perform operations at the same time using streams.
     */
    private static class TableAndImageWrapper implements Iterable<Block> {
        /**
         * The wrapped {@link PseudoTable}.
         */
        private final PseudoTable pseudoTable;
        /**
         * The wrapped {@link BmpImage}.
         */
        private final BmpImage bmpImage;

        /**
         * Private constructor.
         *
         * @param pseudoTable The {@link PseudoTable} to be wrapped.
         * @param bmpImage    The {@link BmpImage} to be wrapped.
         */
        private TableAndImageWrapper(PseudoTable pseudoTable, BmpImage bmpImage) {
            if (bmpImage.getImageSize() != pseudoTable.getSize()) {
                throw new IllegalArgumentException("There must be the same amount of image data" +
                        " and entries in the pseudo table");
            }
            this.pseudoTable = pseudoTable;
            this.bmpImage = bmpImage;
        }

        @Override
        public Iterator<Block> iterator() {
            return new Iterator<Block>() {

                /**
                 * The {@link PseudoTable}'s {@link Iterator}.
                 */
                private final Iterator<List<Integer>> randomsIterator = pseudoTable.iterator();
                /**
                 * The {@link BmpImage}'s {@link Iterator}.
                 */
                private final Iterator<List<Integer>> imageIterator = bmpImage.iterator();

                @Override
                public boolean hasNext() {
                    return randomsIterator.hasNext() && imageIterator.hasNext();
                }

                @Override
                public Block next() {
                    return new Block(randomsIterator.next(), imageIterator.next());
                }
            };
        }
    }

    /**
     * Wraps a block of {@link BmpImage} data, together with a block of {@link PseudoTable} data of the same size.
     */
    private static class Block {
        /**
         * Block of a {@link PseudoTable} data.
         */
        private final List<Integer> randomList;
        /**
         * Block of a {@link BmpImage} data.
         */
        private final List<Integer> coefficients;
        /**
         * Size of blocks.
         */
        private final int amount;

        /**
         * Private constructor.
         *
         * @param randomList   Block of a {@link PseudoTable} data.
         * @param coefficients Block of a {@link BmpImage} data.
         */
        private Block(List<Integer> randomList, List<Integer> coefficients) {
            if (randomList == null || coefficients == null) {
                throw new IllegalArgumentException(); // TODO message
            }
            if (randomList.size() != coefficients.size()) {
                throw new IllegalArgumentException(); // TODO message
            }
            this.randomList = randomList;
            this.coefficients = coefficients;
            this.amount = randomList.size();
        }

        /**
         * @return A {@link List} of {@link Integer} got by applying XOR operations between the {@link BmpImage} data
         * and the {@link PseudoTable} data, one by one.
         */
        private List<Integer> applyXor() {
            return IntStream.range(0, amount)
                    .parallel()
                    .mapToObj(each -> coefficients.get(each) ^ randomList.get(each))
                    .collect(Collectors.toList());
        }
    }
}

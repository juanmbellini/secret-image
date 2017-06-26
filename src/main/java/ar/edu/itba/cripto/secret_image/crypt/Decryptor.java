package ar.edu.itba.cripto.secret_image.crypt;

import ar.edu.itba.cripto.secret_image.bmp.BmpImage;
import ar.edu.itba.cripto.secret_image.bmp.WritableBmpImage;
import ar.edu.itba.cripto.secret_image.math_utils.PolynomialUtils;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Class implementing logic to perform decryption.
 */
public class Decryptor {

    /**
     * Holds the amount of bytes needed to form a byte using some bits of those bytes.
     */
    private static final int BYTES_NEEDED = 8;

    private final int k;
    private final String secretImagePath;
    private final List<BmpImage> shadows;


    public Decryptor(int k, String secretImagePath, List<BmpImage> shadows) {
        // TODO: check values (size, seed, shadow numbers, etc.)
        this.k = k;
        this.secretImagePath = secretImagePath;
        this.shadows = shadows;
    }

    /**
     * Performs the decryption according to the set parameters.
     *
     * @return The {@link WritableBmpImage} resultant from the decruption process.
     */
    public WritableBmpImage decrypt() {

        final BmpImage firstShadow = shadows.get(0);

        final int imagesSize = firstShadow.getImageSize();
        final int seed = firstShadow.getSeed();
        final int polynomialAmount = imagesSize / BYTES_NEEDED;

        final Map<Integer, List<Integer>> secretBytes = shadows.stream().limit(k) // Just k shadows are needed
                // Collect in a map with shadow number as key and list of secret bytes as value
                .collect(Collectors.toMap(BmpImage::getShadow,
                        bmpImage -> StreamSupport.stream(bmpImage.spliterator(), false)
                                // Transforms each block into a secret byte
                                .map(block -> IntStream.range(0, BYTES_NEEDED)
                                        .map(position -> (block.get(position) & 0x01) << BYTES_NEEDED - position - 1)
                                        .reduce(0x00, (byte1, byte2) -> byte1 | byte2))
                                .collect(Collectors.toList())));

        final List<Map<Integer, Integer>> evaluatedPolynomialsMaps = IntStream.range(0, polynomialAmount)
                .mapToObj(polynomial -> secretBytes.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(polynomial))))
                .collect(Collectors.toList());

        final TableAndCoefficientsWrapper wrapper =
                new TableAndCoefficientsWrapper(new PseudoTable(imagesSize, seed, k),
                        evaluatedPolynomialsMaps.stream()
                                .sequential() // Just in case...
                                .map(Decryptor::getCoefficients)
                                .collect(Collectors.toList()));

        List<Integer> resultBytes = StreamSupport.stream(wrapper.spliterator(), false)
                .map(Block::applyXor)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return createImage(secretImagePath, resultBytes, firstShadow, k);

    }

    /**
     * Returns a list of coefficients according to the given {@code points}, using 257 as modulus
     *
     * @param points The points through which the polynomial passes.
     * @return The list of coefficients of the polynomial.
     */
    private static List<Integer> getCoefficients(Map<Integer, Integer> points) {
        return PolynomialUtils.getCoefficients(points, 257);
    }

    /**
     * Creates a new {@link WritableBmpImage} using the given {@code imageName}, the given {@code imageData},
     * and the given {@link BmpImage} as a referecen for header values.
     *
     * @param imageName      The name for the new {@link WritableBmpImage}
     * @param imageData      The {@link List} containing the bytes of the new {@link WritableBmpImage}.
     * @param referenceImage The {@link BmpImage} to use as a reference for headers.
     * @param k              The number indicating the amount of shadows used for creating this {@link WritableBmpImage}.
     * @return The created {@link WritableBmpImage}.
     */
    private static WritableBmpImage createImage(String imageName, List<Integer> imageData, BmpImage referenceImage,
                                                int k) {
        final File file = new File(imageName);
        final int fileSize = imageData.size() + referenceImage.getOffset();
        final int shadow = 0x00;
        final int seed = 0x00;
        final int width = referenceImage.getWidth();
        final int rowSize = Math.floorDiv(referenceImage.getWidth() * 8 + 31, 32) * 4;
        final int height = Math.floorDiv(imageData.size(), rowSize);
        final int offset = referenceImage.getOffset();
        final byte[] data = new byte[fileSize];
        final byte[] referenceImageData = referenceImage.getRawBytes();
        IntStream.range(0, offset).forEach(idx -> data[idx] = referenceImageData[idx]);
        IntStream.range(0, imageData.size()).forEach(idx -> data[idx + offset] = imageData.get(idx).byteValue());

        final WritableBmpImage image =
                new WritableBmpImage(file, fileSize, shadow, seed, width, height, offset, data, k);

        if (k != 8) {
            image.setFileSize(fileSize);
            image.setPicSize(imageData.size());
            image.setHeight(height);
        }
        return image;

    }

    /**
     * Object wrapping a {@link PseudoTable} and a {@link List} of coefficients lists
     * in order to perform operations at the same time using streams.
     */
    private static class TableAndCoefficientsWrapper implements Iterable<Block> {
        /**
         * The wrapped {@link PseudoTable}.
         */
        private final PseudoTable pseudoTable;
        /**
         * The wrapped {@link List} of coefficients lists.
         */
        private final List<List<Integer>> coefficients;

        /**
         * Private constructor.
         *
         * @param pseudoTable  The {@link PseudoTable} to be wrapped.
         * @param coefficients The {@link List} of coefficients lists to be wrapped.
         */
        private TableAndCoefficientsWrapper(PseudoTable pseudoTable, List<List<Integer>> coefficients) {
            if ((int) coefficients.stream().flatMap(Collection::stream).count() != pseudoTable.getSize()) {
                throw new IllegalArgumentException("There must be the same amount of image data" +
                        " and entries in the pseudo table");
            }
            this.pseudoTable = pseudoTable;
            this.coefficients = coefficients;
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
                private final Iterator<List<Integer>> coefficientsIterator = coefficients.iterator();

                @Override
                public boolean hasNext() {
                    return randomsIterator.hasNext() && coefficientsIterator.hasNext();
                }

                @Override
                public Block next() {
                    return new Block(randomsIterator.next(), coefficientsIterator.next());
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

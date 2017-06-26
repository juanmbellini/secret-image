package ar.edu.itba.cripto.secret_image.crypt;


import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class represents a table used for XOR operations with image's pixels.
 */
/* package */ class PseudoTable implements Iterable<List<Integer>> {

    /**
     * Contains the random numbers that are part of this table.
     */
    private final List<Integer> randomNumbers;
    /**
     * Holds the amount of number returned each time this table is iterated.
     */
    private final int blockSize;

    /**
     * @param imageSize The size of image raw data
     * @param seed      The seed used to create the random table.
     * @param blockSize The amount of number returned each time this table is iterated.
     */
    /* package */ PseudoTable(int imageSize, int seed, int blockSize) {
        final Random random = new Random(seed);
        this.randomNumbers = IntStream.generate(() -> random.nextInt(256))
                .sequential() // Just in case...
                .limit(imageSize)
                .mapToObj(each -> each)
                .collect(Collectors.toList());
        this.blockSize = blockSize;
    }

    /**
     * @return The table's size.
     */
    /* package */ int getSize() {
        return randomNumbers.size();
    }


    @Override
    public Iterator<List<Integer>> iterator() {
        return new Iterator<List<Integer>>() {

            private int index;

            @Override
            public boolean hasNext() {
                return index + blockSize <= randomNumbers.size();
            }

            @Override
            public List<Integer> next() {
                List<Integer> list = randomNumbers.stream().skip(index).limit(blockSize).collect(Collectors.toList());
                index += blockSize;
                return list;
            }
        };
    }

}

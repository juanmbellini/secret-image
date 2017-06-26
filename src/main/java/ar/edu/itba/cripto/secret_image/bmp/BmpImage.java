package ar.edu.itba.cripto.secret_image.bmp;


import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class represents a readable but non writable bmp image.
 */
public class BmpImage implements Iterable<List<Integer>> {

    // ========================================================
    // Metadata
    // ========================================================

    /**
     * The file size.
     */
    private final int fileSize;
    /**
     * The shadow.
     */
    private final int shadow;
    /**
     * The seed.
     */
    private final int seed;
    /**
     * The image width.
     */
    private final int width;
    /**
     * The image height.
     */
    private final int height;
    /**
     * The image offset (i.e where the real data starts).
     */
    private final int offset;

    // ========================================================
    // Internal data
    // ========================================================
    /**
     * The real {@link File} that represents this {@link BmpImage}.
     */
    /*package*/ final File file;
    /**
     * The array of bytes representing the image's raw data.
     */
    /*package*/ final byte[] fileBytes;
    /**
     * The amount of bytes that will be returned each time this image is iterated.
     */
    private final int bytesFromIterator;


    /**
     * Constructor.
     *
     * @param file              The real {@link File} that represents this {@link BmpImage}.
     * @param fileSize          The file size.
     * @param shadow            The shadow.
     * @param seed              The seed.
     * @param width             The image width.
     * @param height            The image height.
     * @param offset            The image offset (i.e where the real data starts).
     * @param fileBytes         The array of bytes representing the image's raw data.
     * @param bytesFromIterator The amount of bytes that will be returned each time this image is iterated.
     */
    public BmpImage(File file, int fileSize, int shadow, int seed, int width, int height, int offset,
                    byte[] fileBytes, int bytesFromIterator) {
        this.file = file;
        this.shadow = shadow;
        this.fileSize = fileSize;
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.offset = offset;
        this.fileBytes = fileBytes;
        this.bytesFromIterator = bytesFromIterator;
    }


    /**
     * @return The file size.
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * @return The real image size (i.e the amount of pixels).
     */
    public int getImageSize() {
        return fileSize - offset;
    }

    /**
     * @return The shadow.
     */
    public int getShadow() {
        return shadow;
    }

    /**
     * @return The seed.
     */
    public int getSeed() {
        return seed;
    }

    /**
     * @return The image width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return The image height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return The image offset (i.e where the real data starts).
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return The amount of bytes that will be returned each time this image is iterated.
     */
    public int getBytesFromIterator() {
        return bytesFromIterator;
    }

    /**
     * @return The array of bytes representing the image's real raw data.
     */
    public byte[] getImageData() {
        return Arrays.copyOfRange(fileBytes, offset, fileSize);
    }

    /**
     * @return The array of bytes representing the image's raw data.
     */
    public byte[] getRawBytes() {
        return Arrays.copyOf(fileBytes, fileBytes.length);
    }


    @Override
    public Iterator<List<Integer>> iterator() {
        return new Iterator<List<Integer>>() {

            /**
             * The actual index from which data must be get.
             */
            private int index = offset;

            @Override
            public boolean hasNext() {
                return index + bytesFromIterator <= fileSize;
            }

            @Override
            public List<Integer> next() {
                List<Integer> list = IntStream.range(index, index + bytesFromIterator)
                        .mapToObj(idx -> Byte.toUnsignedInt(fileBytes[idx]))
                        .collect(Collectors.toList());
                this.index += bytesFromIterator;
                return list;
            }
        };
    }


}

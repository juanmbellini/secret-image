package ar.edu.itba.cripto.secret_image.bmp;


import org.apache.commons.io.IOUtils;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class representing a bmp image.
 */
public class BmpUtils implements Iterable<List<Integer>> {

    // ================================
    // Metadata
    // ================================
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
    /*package*/ final int offset;

    // ================================
    // Internal data
    // ================================
    /**
     * The real {@link File} that represents this image.
     */
    /*package*/ final File file;
    /**
     * The array of bytes representing the image's raw data.
     */
    /*package*/ final byte[] fileBytes;

    /**
     * The amount of bytes that will be returned each time this image is iterated.
     */
    private int bytesFromIterator = 8;

    /**
     * Constructor
     *
     * @param path Path where the image must be opened from.
     * @throws IOException If any IO error occurs while opening/reading file.
     */
    public BmpUtils(String path) throws IOException {
        this.file = new File(path);

        ImageInputStream imageStream = new FileImageInputStream(file);
        imageStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        //Skip ID
        imageStream.skipBytes(2);
        //Size of whole fileBytes
        this.fileSize = (int) imageStream.readUnsignedInt();
        //Reserved: seed + shadow
        this.seed = imageStream.readUnsignedShort(); //seed
        this.shadow = imageStream.readUnsignedShort(); //shadow
        //Offset to image start
        this.offset = (int) imageStream.readUnsignedInt();
        //Skip Length of BitMapInfoHeader
        imageStream.skipBytes(4);
        //Width & height
        this.width = (int) imageStream.readUnsignedInt();
        this.height = (int) imageStream.readUnsignedInt();

        imageStream.close();

        InputStream inputStream = new FileInputStream(file);
        this.fileBytes = IOUtils.toByteArray(inputStream);
        inputStream.close();

    }

    /**
     * Constructor.
     *
     * @param file   The real {@link File} that represents this image.
     * @param bytes  The array of bytes representing the image's raw data.
     * @param shadow A {@link BmpUtils} used as a reference (i.e data is taken from here).
     * @param height The image height.
     */
    /* package */ BmpUtils(File file, byte[] bytes, BmpUtils shadow, int height) {
        this.file = file;
        this.fileBytes = bytes;
        this.fileSize = bytes.length;
        this.offset = shadow.offset;
        this.seed = 0;
        this.shadow = 0;
        this.width = shadow.width;
        this.height = height;
    }

    /**
     * @return The file size.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @return The real image size (i.e the amount of pixels).
     */
    public long getImageSize() {
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
     * Sets the amount of bytes that will be returned each time this image is iterated.
     *
     * @param bytesFromIterator The amount of bytes that will be returned each time this image is iterated.
     */
    public void setBytesFromIterator(int bytesFromIterator) {
        this.bytesFromIterator = bytesFromIterator;
    }

    /**
     * @return A {@link BmpEditor} to edit this {@link BmpUtils}.
     */
    public BmpEditor edit() {
        return new BmpEditor(this);
    }

    @Override
    public Iterator<List<Integer>> iterator() {
        return new KByteIterator();
    }


    /**
     * An {@link Iterator} for a {@link BmpUtils}, iterating through the image real data, by blocks.
     */
    private class KByteIterator implements Iterator<List<Integer>> {

        /**
         * The actual start of block.
         */
        int index = offset;

        @Override
        public boolean hasNext() {
            return index + bytesFromIterator <= fileSize;
        }

        @Override
        public List<Integer> next() {
            List<Integer> list = new ArrayList();
            for (int i = 0; i < bytesFromIterator; i++) {
                list.add(Byte.toUnsignedInt(fileBytes[index++]));
            }
            return list;
        }
    }


}

package ar.edu.itba.cripto.secret_image.bmp;

import java.io.File;

/**
 * This class represents a readable and writable bmp image.
 */
public class WritableBmpImage extends BmpImage {

    /**
     * First reserved position (used for storing the seed).
     */
    private final static int RESERVED_ONE = 0x6;
    /**
     * Second reserved position (used for storing the shadow).
     */
    private final static int RESERVED_TWO = 0x8;
    /**
     * Height header position.
     */
    private final static int HEIGHT = 0x16;
    /**
     * Height header position.
     */
    private final static int FILE_SIZE = 0x2;
    /**
     * Height header position.
     */
    private final static int PIC_SIZE = 0x22;


    /**
     * Pointer used for writing the secret.
     */
    private int pointer;

    /**
     * Constructor.
     *
     * @param file              The real {@link File} that represents this {@link BmpImage}.
     * @param fileSize          The file size.
     * @param shadow            The shadow
     * @param seed              The seed.
     * @param width             The image width.
     * @param height            The image height.
     * @param offset            The image offset (i.e where the real data starts).
     * @param fileBytes         The array of bytes representing the image's raw data.
     * @param bytesFromIterator The amount of bytes that will be returned each time this image is iterated.
     */
    public WritableBmpImage(File file, int fileSize, int shadow, int seed, int width, int height, int offset,
                            byte[] fileBytes, int bytesFromIterator) {
        super(file, fileSize, shadow, seed, width, height, offset, fileBytes, bytesFromIterator);
    }

    /**
     * Constructor. Makes a new {@link WritableBmpImage} using the given {@link BmpImage} as a reference
     * (i.e the same image, but writable).
     *
     * @param bmpImage The {@link BmpImage} used as a reference.
     */
    public WritableBmpImage(BmpImage bmpImage) {
        this(bmpImage.file, bmpImage.getFileSize(), bmpImage.getShadow(), bmpImage.getSeed(),
                bmpImage.getWidth(), bmpImage.getHeight(), bmpImage.getOffset(), bmpImage.fileBytes,
                bmpImage.getBytesFromIterator());
    }

    /**
     * @return The real {@link File} that represents this {@link BmpImage}.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Sets the seed for this image.
     *
     * @param num The new seed.
     */
    public void setSeed(int num) {
        setMetadata(RESERVED_ONE, num);
    }

    /**
     * Sets the shadow for this image.
     *
     * @param num The new shadow.
     */
    public void setShadow(int num) {
        setMetadata(RESERVED_TWO, num);
    }


    /**
     * Sets the file size for this image.
     *
     * @param num The new file size.
     */
    public void setFileSize(int num) {
        setMetadata(FILE_SIZE, num);
    }

    /**
     * Sets the height for this image.
     *
     * @param num The new height.
     */
    public void setHeight(int num) {
        setMetadata(HEIGHT, num);
    }

    /**
     * Sets the picture size for this image.
     *
     * @param num The new picture size.
     */
    public void setPicSize(int num) {
        setMetadata(PIC_SIZE, num);
    }


    /**
     * Sets the given {@code data} in the given metadata header {@code position}.
     *
     * @param position The header's position.
     * @param data     The data to be written.
     */
    private void setMetadata(int position, int data) {
        this.fileBytes[position] = (byte) (data & 0x00FF);
        this.fileBytes[position + 1] = (byte) ((data & 0xFF00) >> 8);
    }

    /**
     * Sets the given {@code data} in the given {@code position}.
     *
     * @param position The position to be written.
     * @param data     The data to be written.
     */
    private void setData(int position, byte data) {
        this.fileBytes[position] = data;
    }


    /**
     * Writes the secret in this image.
     *
     * @param secret The secret to be written.
     * @return {@code true} if the secret could be written, or {@code false} otherwise.
     */
    // TODO: should go into another class.
    public boolean insertSecret(int secret) {
        if (pointer + 8 > this.getFileSize()) {
            return false;
        }

        for (int i = 1; i <= 8; i++) {
            int aux = (secret >> (8 - i)) & 1;

            if (aux == 1) {
                this.fileBytes[pointer] = (byte) (this.fileBytes[pointer] | 0x01);
            } else {
                this.fileBytes[pointer] = (byte) (this.fileBytes[pointer] & 0xFE);
            }
            pointer++;
        }
        return true;
    }
}

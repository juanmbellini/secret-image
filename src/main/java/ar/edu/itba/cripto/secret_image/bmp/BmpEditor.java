package ar.edu.itba.cripto.secret_image.bmp;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Class implementing logic to edit a {@link BmpUtils}.
 */
public class BmpEditor {

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
     * The {@link BmpUtils} being edited.
     */
    private final BmpUtils bmpUtils;

    /**
     * Pointer used for writing the secret.
     */
    private int pointer;

    /**
     * Constructor.
     *
     * @param bmpUtils The {@link BmpUtils} being edited.
     */
    /*package*/ BmpEditor(BmpUtils bmpUtils) {
        this.bmpUtils = bmpUtils;
        this.pointer = bmpUtils.offset;
    }

    /**
     * Constructor.
     *
     * @param name   The name of a new {@link BmpUtils} (i.e the name used to save a new image).
     * @param image  The real data of a new image.
     * @param shadow A {@link BmpUtils} used as a reference for getting data for the new image.
     * @param k      The k value.
     */
    public BmpEditor(String name, List<Integer> image, BmpUtils shadow, int k) {
        File newFile = new File(name);
        int imageSize;
        int height;

        if (k == 8) {
            imageSize = image.size();
            height = shadow.getHeight();
        } else {
            int rowSize = Math.floorDiv(shadow.getWidth() * 8 + 31, 32) * 4;
            height = shadow.getHeight() * k / 8;
            imageSize = rowSize * height;
        }

        int total = shadow.offset + imageSize;
        total += total % 4;

        byte[] newImage = new byte[total];
        for (int i = 0; i < shadow.offset; i++) {
            newImage[i] = shadow.fileBytes[i];
        }
        for (int i = 0; i < imageSize; i++) {
            newImage[shadow.offset + i] = (byte) image.get(i).intValue();
        }

        this.bmpUtils = new BmpUtils(newFile, newImage, shadow, height);

        if (k != 8) {
            editFileSize(total);
            editPicSize(total - shadow.offset);
            editHeight(height);
        }


    }

    /**
     * Changes the seed of the {@link BmpUtils}.
     *
     * @param num The new value for the seed.
     */
    public void editSeed(int num) {
        editMetadataShort(RESERVED_ONE, num);
    }

    /**
     * Changes the shadow of the {@link BmpUtils}.
     *
     * @param num The new value for the shadow.
     */
    public void editShadow(int num) {
        editMetadataShort(RESERVED_TWO, num);
    }

    /**
     * Changes the file size of the {@link BmpUtils}.
     *
     * @param num The new value for the file size.
     */
    public void editFileSize(int num) {
        editMetadataInt(FILE_SIZE, num);
    }

    /**
     * Changes the height of the {@link BmpUtils}.
     *
     * @param num The new value for the height.
     */
    public void editHeight(int num) {
        editMetadataInt(HEIGHT, num);
    }

    /**
     * Changes the picture size of the {@link BmpUtils}.
     *
     * @param num The new value for the picture size.
     */
    public void editPicSize(int num) {
        editMetadataInt(PIC_SIZE, num);
    }

    /**
     * Edits metadata headers (2 bytes).
     *
     * @param position Position of header.
     * @param data     Data to be written.
     */
    private void editMetadataShort(int position, int data) {
        bmpUtils.fileBytes[position] = (byte) (data & 0x00FF);
        bmpUtils.fileBytes[position + 1] = (byte) ((data & 0xFF00) >> 8);
    }

    /**
     * Edits metadata headers (4 bytes).
     *
     * @param position Position of header.
     * @param data     Data to be written.
     */
    private void editMetadataInt(int position, int data) {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(data);
        byte[] aux = buffer.array();

        bmpUtils.fileBytes[position] = aux[0];
        bmpUtils.fileBytes[position + 1] = aux[1];

        bmpUtils.fileBytes[position + 2] = aux[2];
        bmpUtils.fileBytes[position + 3] = aux[3];
    }

    /**
     * Saves the secret in the {@link BmpUtils} being edited.
     *
     * @param secret The secret to be saved.
     * @return {@code true} if the secret was saved, or {@code false} otherwise.
     */
    public boolean insertSecret(int secret) {
        if (pointer + 8 > bmpUtils.getFileSize()) {
            return false;
        }

        for (int i = 1; i <= 8; i++) {
            int aux = (secret >> (8 - i)) & 1;

            if (aux == 1) {
                bmpUtils.fileBytes[pointer] = (byte) (bmpUtils.fileBytes[pointer] | 0x01);
            } else {
                bmpUtils.fileBytes[pointer] = (byte) (bmpUtils.fileBytes[pointer] & 0xFE);
            }
            pointer++;
        }
        return true;
    }

    /**
     * Saves the image.
     *
     * @return {@code true} if the image was saved, or {@code false} otherwise.
     */
    public boolean saveImage() {
        try {
            FileUtils.writeByteArrayToFile(bmpUtils.file, bmpUtils.fileBytes);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

package ar.edu.itba.cripto.secret_image.io;

import ar.edu.itba.cripto.secret_image.bmp.BmpImage;
import ar.edu.itba.cripto.secret_image.bmp.WritableBmpImage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class implementing logic to perform IO operations with {@link BmpImage}
 */
public class BmpFileIO {


    /**
     * Returns a {@link List} of {@link WritableBmpImage} to be used as shadows.
     *
     * @param dirPath The path where the shadow images are.
     * @return The {@link List} containing the shadows.
     */
    public static List<WritableBmpImage> getShadowImages(String dirPath, int iteratorAmount) {
        //noinspection ConstantConditions
        return Arrays.stream(Optional.of(new File(dirPath).listFiles((dir, name) -> name.endsWith(".bmp")))
                .orElse(new File[0]))
                .map(each -> openWritableBmpImage(each, iteratorAmount))
                .collect(Collectors.toList());
    }

    /**
     * Returns a {@link BmpImage} to be used as secret.
     *
     * @param secretImagePath The path to the secret image.
     * @return The secret image.
     */
    public static BmpImage getSecretImage(String secretImagePath, int iteratorAmount) {
        return openWritableBmpImage(secretImagePath, iteratorAmount);

    }


    public static void saveShadows(List<WritableBmpImage> bmpImages) {
        bmpImages.forEach(BmpFileIO::saveImage);
    }


    public static BmpImage openBmpImage(String path, int bytesFromIterator) {
        return openBmpImage(new File(path), bytesFromIterator);
    }

    public static BmpImage openBmpImage(File file, int bytesFromIterator) {
        try {
            final ImageInputStream imageStream = new FileImageInputStream(file);
            imageStream.setByteOrder(ByteOrder.LITTLE_ENDIAN); // Set little endian
            imageStream.skipBytes(2); // Skip ID
            final int fileSize = (int) imageStream.readUnsignedInt(); // Size of whole fileBytes
            final int seed = imageStream.readUnsignedShort(); // Reserved: Seed
            final int shadow = imageStream.readUnsignedShort(); // Reserved: Shadow
            final int offset = (int) imageStream.readUnsignedInt(); // Offset to start of image
            imageStream.skipBytes(4); // Skip Length of BitMapInfoHeader
            final int width = (int) imageStream.readUnsignedInt();
            final int height = (int) imageStream.readUnsignedInt();
            imageStream.close();

            // Copy data from bmp file
            final InputStream inputStream = new FileInputStream(file);
            final byte[] fileBytes = IOUtils.toByteArray(inputStream); // Raw data.
            inputStream.close();
            return new BmpImage(file, fileSize, shadow, seed, width, height, offset, fileBytes, bytesFromIterator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static WritableBmpImage openWritableBmpImage(String path, int bytesFromIterator) {
        return openWritableBmpImage(new File(path), bytesFromIterator);
    }

    public static WritableBmpImage openWritableBmpImage(File file, int bytesFromIterator) {
        return new WritableBmpImage(openBmpImage(file, bytesFromIterator));
    }


    private static void saveImage(WritableBmpImage bmpImage) {
        try {
            FileUtils.writeByteArrayToFile(bmpImage.getFile(), bmpImage.getRawBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}

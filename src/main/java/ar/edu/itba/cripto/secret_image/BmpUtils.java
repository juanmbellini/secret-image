package ar.edu.itba.cripto.secret_image;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Iterator;

public class BmpUtils implements Iterable<Integer> {

    //Metadata
    private final long fileSize;
    private final long offset;

    //Constructor
    private final File file;
    private final byte[] fileBytes;

    public BmpUtils(String path) throws IOException {
        this.file = new File(path);

        ImageInputStream imageStream = new FileImageInputStream(file);
        imageStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        //Skip ID
        imageStream.skipBytes(2);
        //Size of whole fileBytes
        this.fileSize = imageStream.readUnsignedInt();
        //Skip reserved
        imageStream.skipBytes(4);
        //Offset to image start
        this.offset = imageStream.readUnsignedInt();

        imageStream.close();

        InputStream inputStream = new FileInputStream(file);
        this.fileBytes = IOUtils.toByteArray(inputStream);

    }

    public Iterator<Integer> iterator() {
        return new SimpleIterator();
    }

    public boolean updateImage() {
        try {
            FileUtils.writeByteArrayToFile(file, fileBytes);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private class SimpleIterator implements Iterator<Integer> {

        private int index = (int) offset;

        @Override
        public boolean hasNext() {
            return index < fileSize;
        }

        @Override
        public Integer next() {
            return Byte.toUnsignedInt(fileBytes[index++]);
        }
    }


}

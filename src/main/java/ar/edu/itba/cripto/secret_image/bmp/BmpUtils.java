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

public class BmpUtils implements Iterable<List<Integer>>{

    //Metadata
    private final int fileSize;
    private final int shadow;
    private final int seed;
    /*package*/ final int offset;

    //Constructor
    /*package*/ final File file;
    /*package*/ final byte[] fileBytes;
    private int bytesFromIterator = 8;

    public BmpUtils(String path) throws IOException {
        this.file = new File(path);

        ImageInputStream imageStream = new FileImageInputStream(file);
        imageStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        //Skip ID
        imageStream.skipBytes(2);
        //Size of whole fileBytes
        this.fileSize = (int) imageStream.readUnsignedInt();
        //Skip reserved
        this.seed = imageStream.readUnsignedShort(); //seed
        this.shadow = imageStream.readUnsignedShort(); //shadow
        //Offset to image start
        this.offset = (int) imageStream.readUnsignedInt();

        imageStream.close();

        InputStream inputStream = new FileInputStream(file);
        this.fileBytes = IOUtils.toByteArray(inputStream);
        inputStream.close();

    }

    public long getFileSize() {
        return fileSize;
    }

    public long getImageSize(){
        return fileSize-offset;
    }

    public int getShadow() {
        return shadow;
    }

    public int getSeed() {
        return seed;
    }

    public void setBytesFromIterator(int bytesFromIterator) {
        this.bytesFromIterator = bytesFromIterator;
    }

    public BmpEditor edit(){
        return new BmpEditor(this);
    }

    @Override
    public Iterator<List<Integer>> iterator() {
        return new KByteIterator();
    }

    private class KByteIterator implements Iterator<List<Integer>> {

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

package ar.edu.itba.cripto.secret_image.bmp;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BmpEditor {

    private final int RESERVED_ONE = 0x6;
    private final int RESERVED_TWO = 0x8;
    private final int HEIGTH = 0x16;
    private final int FILESIZE = 0x2;
    private final int PICSIZE=0x22;

    private final BmpUtils bmpUtils;

    private int pointer;

    /*package*/ BmpEditor(BmpUtils bmpUtils) {
        this.bmpUtils = bmpUtils;
        this.pointer = bmpUtils.offset;
    }

    public BmpEditor(String name, List<Integer> image, BmpUtils shadow, int k){
        File newFile = new File(name);
        int total = shadow.offset+image.size();
        byte[] newImage = new byte[total];
        for (int i = 0; i < shadow.offset; i++) {
            newImage[i]=shadow.fileBytes[i];
        }
        for (int i = 0; i < image.size(); i++) {
            newImage[shadow.offset+i] = (byte) image.get(i).intValue();
        }
        int rowSize = Math.floorDiv(shadow.getWidth()*8+31, 32)*4;
        int height = Math.floorDiv(image.size(), rowSize);

        this.bmpUtils = new BmpUtils(newFile, newImage, shadow, height);

        if(k!=8){
            editFilesize(total);
            editPicSize(image.size());

            editHeight(height);
        }
    }

    public void editSeed(int num){
       editMetadata(RESERVED_ONE, num);
    }

    public void editShadow(int num){
        editMetadata(RESERVED_TWO, num);
    }

    public void editFilesize(int num){
        editMetadata(FILESIZE, num);
    }

    public void editHeight(int num){
        editMetadata(HEIGTH, num);
    }

    public void editPicSize(int num){
        editMetadata(PICSIZE, num);
    }

    private void editMetadata(int position, int data){
        bmpUtils.fileBytes[position] = (byte) (data&0x00FF);
        bmpUtils.fileBytes[position+1] = (byte) ((data&0xFF00)>>8);
    }

    public boolean insertSecret(int secret){
        if(pointer+8 > bmpUtils.getFileSize()){
            return false;
        }

        for (int i = 1; i <= 8; i++) {
            int aux = (secret >> (8-i))&1;

            if(aux==1){
                bmpUtils.fileBytes[pointer] = (byte) (bmpUtils.fileBytes[pointer]|0x01);
            }else{
                bmpUtils.fileBytes[pointer] = (byte) (bmpUtils.fileBytes[pointer]&0xFE);
            }
            pointer++;
        }
        return true;
    }

    public boolean saveImage() {
        try {
            FileUtils.writeByteArrayToFile(bmpUtils.file, bmpUtils.fileBytes);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

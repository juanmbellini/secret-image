package ar.edu.itba.cripto.secret_image.bmp;


import org.apache.commons.io.FileUtils;

import java.io.IOException;

public class BmpEditor {

    private final int RESERVED_ONE = 0x6;
    private final int RESERVED_TWO = 0x8;

    private final BmpUtils bmpUtils;

    private int pointer;

    /*package*/ BmpEditor(BmpUtils bmpUtils) {
        this.bmpUtils = bmpUtils;
        this.pointer = (int) bmpUtils.offset;
    }

    public void editSeed(int num){
       editMetadata(RESERVED_ONE, num);
    }

    public void editShadow(int num){
        editMetadata(RESERVED_TWO, num);
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

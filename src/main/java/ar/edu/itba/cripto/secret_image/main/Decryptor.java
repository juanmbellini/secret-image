package ar.edu.itba.cripto.secret_image.main;

import ar.edu.itba.cripto.secret_image.bmp.BmpEditor;
import ar.edu.itba.cripto.secret_image.bmp.BmpUtils;
import ar.edu.itba.cripto.secret_image.main.util.PseudoTable;
import ar.edu.itba.cripto.secret_image.math_utils.PolynomialUtils;

import java.io.IOException;
import java.util.*;

public class Decryptor {
    /**
     * Creates the secret image with name "secretName" in the path "secretPath" using ALL the images from the paths "paths".
     *
     * @return
     */
    public static void decrypt(String secretPath, String secretName, ArrayList<String> paths) throws IOException {

        int k = paths.size();
        if(k<2){
            throw new IllegalArgumentException("Number of shadows must be at least 2");
        }
        ArrayList<BmpUtils> images = new ArrayList<>();

        int size = -1;
        int seed = -1;

        Set<Integer> shadowNumbers = new HashSet<>();
        for(int i=0; i<k; i++){
            String path = paths.get(i);
            BmpUtils bmpUtils;
            try {
                bmpUtils = new BmpUtils(path);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
            shadowNumbers.add(bmpUtils.getShadow());
            if(i == 0){
                size =(int)bmpUtils.getImageSize();
                if(size % 8 != 0){
                    //TODO
//                    throw new IllegalArgumentException("Shadow image size is not divisible by 8");
                }
                seed = bmpUtils.getSeed();
            }else{
                if(size != (int)bmpUtils.getImageSize()){
                    throw new IllegalArgumentException("Size of shadows is not consistent");
                }
                if(seed != bmpUtils.getSeed()){
                    throw new IllegalArgumentException("Seed of shadows is not consistent");
                }
            }
            images.add(bmpUtils);
        }
        if(shadowNumbers.size()!=k){
            throw  new IllegalArgumentException("Repeated shadow numbers");
        }

        int numPolynomes = size/8;

        ArrayList<Map<Integer,Integer>> evaluatedPolynomesMap = new ArrayList<>();
        for(int i=0; i<numPolynomes; i++){
            evaluatedPolynomesMap.add(new HashMap<>());
        }

        /* Get all bytes hiden in all the shadows */
        for(int i=0; i<k; i++){
            BmpUtils bmpUtils = images.get(i);
            int shadowNumber = bmpUtils.getShadow();
            int m=0;
            bmpUtils.setBytesFromIterator(8);
            for(List<Integer> byteArray: bmpUtils){
                int secretByte=0;
                for(int j=0; j<8; j++){
                    int b = byteArray.get(j);
                    secretByte <<= 1;
                    int secretBit=b&0x01;
                    secretByte |= secretBit;
                }
                evaluatedPolynomesMap.get(m).put(shadowNumber,secretByte);
                m++;
            }
        }
        /* Use hidden bytes to form the polynomes */
        List<Integer> resultBytes = new ArrayList<>();
        List<Integer> permutationTable = PseudoTable.generatePseudoTable(k*numPolynomes, seed);
        for(int polynomeNumber=0; polynomeNumber<numPolynomes; polynomeNumber++){
            List<Integer> coefficients = PolynomialUtils.getCoefficients(evaluatedPolynomesMap.get(polynomeNumber),257);
            //TODO integrar con el merka
            if(coefficients.size()!=k) throw  new IllegalStateException("coefficients != k");
            for(int coefficientNumber=0; coefficientNumber<k; coefficientNumber++){
                resultBytes.add(coefficients.get(coefficientNumber)^ permutationTable.get(polynomeNumber*k+coefficientNumber));
            }
        }

        BmpEditor secret = new BmpEditor(secretPath + secretName, resultBytes, images.get(0), k);
        secret.saveImage();
    }
}

package ar.edu.itba.cripto.secret_image.main;

import ar.edu.itba.cripto.secret_image.bmp.BmpUtils;

import java.io.IOException;
import java.util.*;

public class Decryptor {
    /**
     * Creates the secret image with name "secretName" in the path "secretPath" using ALL the images from the paths "paths".
     *
     * @return
     */
    public static BmpUtils decrypt(String secretName,String secretPath, ArrayList<String> paths) throws IOException {

        int k = paths.size();
        if(k<2){
            throw new IllegalArgumentException("Number of shadows must be at least 2");
        }

        ArrayList<BmpUtils> images = new ArrayList<>();


        int size = -1;
        int seed = -1;

        Set<Integer> shadowNumbers = new HashSet<>();
                //TODO check diferentes shadows. y misma seed
        for(int i=0; i<k; i++){
            String path = paths.get(i);
            BmpUtils bmpUtils;
            try {
                bmpUtils = new BmpUtils(path, 8);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
            shadowNumbers.add(bmpUtils.getShadow());
            if(i == 0){
                size =(int)bmpUtils.getImageSize();
                if(size % 8 != 0){
                    throw new IllegalArgumentException("Shadow image size is not divisible by 8");
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
            BmpUtils bmpUtils = images.get(k);


            int shadowNumber = bmpUtils.getShadow();
            int m=0;
            int secretByte=0;

            bmpUtils.setBytesFromIterator(8);
            for(List<Integer> byteArray: bmpUtils){
                for(int j=0; j<8; j++){
                    int b = byteArray.get(j);
                    secretByte <<= 1;
                    int secretBit=b^0x01;
                    secretByte |= secretBit;
                }
                evaluatedPolynomesMap.get(m).put(shadowNumber,secretByte);
                m++;
            }
        }

        /* Use hidden bytes to form the polynomes */
        ArrayList<Integer> resultBytes = new ArrayList<>();
        ArrayList<Integer> permutationTable = PseudoTable.generatePseudoTable(k*numPolynomes, seed);
//TODO integrar con el metodo de josé que tiene que mover a util

        for(int polynomeNumber=0; polynomeNumber<numPolynomes; polynomeNumber++){
            ArrayList<Integer> coefficients = getCoefficients(evaluatedPolynomesMap.get(polynomeNumber));
            //TODO integrar con el merka
            if(coefficients.size()!=k) throw  new IllegalStateException("coefficients != k");
            for(int coefficientNumber=0; coefficientNumber<k; coefficientNumber++){
                resultBytes.add(coefficients.get(coefficientNumber)^ permutationTable.get(polynomeNumber*k+coefficientNumber));
            }
        }

        BmpUtils secretImage = new BmpUtils(secretPath+secretName,resultBytes,images.get(0));
        return secretImage;
    }

}

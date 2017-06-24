package ar.edu.itba.cripto.secret_image.main;

import java.util.*;

public class Decryptor {

    public static class BmpUtils{
        //TODO integrar con Lean
        ArrayList<Integer> getBytes(){
            return null;
        }
    }


    /**
     * returns the secret image using ALL the images from the ArrayList images.
     * @param images
     * @return
     */
    BmpUtils decrypt(ArrayList<BmpUtils> images){

        int k = images.size();

        if(k<2){
            throw new IllegalArgumentException("Number of shadows must be at least 2");
        }

        int size;
        int seed;

        Set<Integer> shadowNumbers = new HashSet<>();
                //TODO check diferentes shadows. y misma seed
        for(int i=0; i<k; i++){
            BmpUtils bmpUtils = images.get(k);
            shadowNumbers.add(bmpUtils.getShadow());
            if(i == 0){
                size = bmpUtils.getImageSize();
                if(size % 8 != 0){
                    throw new IllegalArgumentException("Shadow image size is not divisible by 8");
                }
                seed = bmpUtils.getSeed();
            }else{
                if(size != bmpUtils.getImageSize()){
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
            int j=0;
            int m=0;
            int secretByte=0;
            for(int b: bmpUtils){
                secretByte <<= 1;
                int secretBit=b^0x01;
                secretByte |= secretBit;
                j++;
                if(j==8){
                    evaluatedPolynomesMap.get(m).put(shadowNumber,secretByte);
                    j=0;
                    m++;
                }
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

        return new BmpUtils(resultBytes);
    }

}

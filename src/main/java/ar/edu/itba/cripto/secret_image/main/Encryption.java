package ar.edu.itba.cripto.secret_image.main;


import ar.edu.itba.cripto.secret_image.bmp.BmpEditor;
import ar.edu.itba.cripto.secret_image.bmp.BmpUtils;
import ar.edu.itba.cripto.secret_image.main.util.PseudoTable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Encryption {

    private final int k;
    private final int n;
    private final String secretImagePath;
    private final List<String> shadowPaths;

    public Encryption(int k, int n, String secretImagePath, String directory){
        this.k = k;
        this.n = n;
//        this.secretImagePath = secretImagePath;

        this.secretImagePath = secretImagePath;


        File dir = new File(directory);
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".bmp");
            }
        });

        this.shadowPaths = new ArrayList<>();
        for (File bmpfile : files) {
            shadowPaths.add(bmpfile.getPath());
        }
        if(shadowPaths.size()!=n){
            throw new IllegalStateException("The amount of shadows is not N.");
        }
    }

    /**
     *
     * encrypts the image located in secretImagePath into n shadows
     */
    public void encrypt(){

        System.out.println("----- encrypting -----");

        //Mocking BMPUtil
        BmpUtils bmpUtil = null;
        try {
            bmpUtil = new BmpUtils(secretImagePath);
            bmpUtil.setBytesFromIterator(k);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int imageSize = (int)bmpUtil.getImageSize();

        if(imageSize%k!=0){
            throw new IllegalStateException("Image to encrypt need to be of a size divisible by k");
        }


        Random random = new Random();

        int seed = random.nextInt(65536);

        List<Integer> pseudoTable = PseudoTable.generatePseudoTable(imageSize, seed);

        List<List<Integer>> evalsList = new ArrayList<>();

        int i = 0;
        for(List<Integer> coefficients : bmpUtil){
            List<Integer> newCoefficients = new ArrayList<>();
            for (int j = 0; j < coefficients.size(); j++) {
                newCoefficients.add(coefficients.get(j) ^ pseudoTable.get(i*coefficients.size()+j));
            }
            i++;


            System.out.println("----- evaluating polynomials " + i + "  -----");

            List<Integer> evals = evalPolynomial(newCoefficients, n);

            evalsList.add(evals);
        }


        System.out.println("----- polynomials evaled -----");

        for (int j = 0; j < n; j++) {
            BmpUtils shadow = null;
            try {
                shadow = new BmpUtils(shadowPaths.get(j));
                shadow.setBytesFromIterator(k);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if( shadow.getImageSize() != (imageSize/k)*8 ){
                throw new IllegalStateException("shadow size is not of correct size");
            }

            BmpEditor editor = shadow.edit();
            editor.editSeed(seed);
            editor.editShadow(j + 1);
            for (int l = 0; l < evalsList.size(); l++) {
                editor.insertSecret(evalsList.get(l).get(j));
            }
            editor.saveImage();
        }
    }



    /**
     *
     * @param newCoefficients coefficients of polynomial
     * @param n range of values in which polynomial will be evaluated
     * @return evaluations of polynomial with x from 1 to n
     */
    private List<Integer> evalPolynomial(List<Integer> newCoefficients, int n){
        boolean overflow = true;
        List<Integer> evals = null;

        while(overflow) {
            evals = new ArrayList<>();
            overflow = false;
            for (int x = 1; x <= n && !overflow; x++) {
                int eval = 0;
                for (int i = 0; i < newCoefficients.size(); i++) {
                    int powerX = x;
                    for(int pow = 1; pow<i; pow++){
                        powerX *= x;
                        powerX %= 257;
                    }
                    eval += newCoefficients.get(i) * powerX;
                    eval %= 257;
//                    eval += newCoefficients.get(i) * Math.pow(x, i);
                }
                if(eval == 256){
                    overflow = true;
                    boolean flag = true;
                    for (int i = 0; i < newCoefficients.size() && flag; i++) {
                        if(newCoefficients.get(i) != 0){
                            newCoefficients.set(i, newCoefficients.get(i)-1);
                            flag = false;
                        }
                    }
                }
                else{
                    evals.add(x-1, eval);
                }
            }
        }
        return evals;
    }
}

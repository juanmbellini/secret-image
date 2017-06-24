package ar.edu.itba.cripto.secret_image.main;


import ar.edu.itba.cripto.secret_image.main.util.PseudoTable;

import java.io.File;
import java.io.FilenameFilter;
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
        this.secretImagePath = secretImagePath;
        this.n = n;

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
    }

    /**
     *
     * encrypts the image located in secretImagePath into n shadows
     */
    public void encrypt(){

        //Mocking BMPUtil
        List<List<Integer>> bmpUtil = new ArrayList<>();
        List<Integer> coeffs = new ArrayList<>();
        coeffs.add(1);
        coeffs.add(2);
        coeffs.add(4);
        coeffs.add(8);
        coeffs.add(16);
        coeffs.add(32);
        coeffs.add(64);
        coeffs.add(128);

        bmpUtil.add(coeffs);

        int imageSize = 64;

        Random random = new Random();

        int seed = random.nextInt(65536);

        List<Integer> pseudoTable = PseudoTable.generatePseudoTable(imageSize, seed);

        List<List<Integer>> evalsList = new ArrayList<>();

        int i = 0;
        for(List<Integer> coefficients : bmpUtil){
            List<Integer> newCoefficients = new ArrayList<>();
            for (int j = 0; j < coefficients.size(); j++) {
                newCoefficients.add(coefficients.get(i) ^ pseudoTable.get(i+j));
            }
            i++;

            List<Integer> evals = evalPolynomial(newCoefficients, n);

            evalsList.add(evals);
        }

        for (int j = 0; j < n; j++) {
            BMPUtils shadow = new BMPUtil(shadowPaths.get(j));
            shadow.editSeed(seed);
            shadow.editShadow(j + 1);
            for (int l = 0; l < evalsList.size(); l++) {
                shadow.modifyBytes(evalsList.get(l).get(j));
            }
        }
    }



    /**
     *
     * @param newCoefficients coefficients of polynomial
     * @param n range of values in which polynomial will be evaluated
     * @return evaluations of polynomial with x from 1 to n
     */
    private List<Integer> evalPolynomial(List<Integer> newCoefficients, int n){
        int eval = 0;
        boolean overflow = true;
        List<Integer> evals = new ArrayList<>();

        while(overflow) {
            overflow = false;
            for (int x = 1; x <= n && !overflow; x++) {
                for (int i = 0; i < newCoefficients.size(); i++) {
                    eval += newCoefficients.get(i) * Math.pow(x, i);
                }
                eval = eval % 257;
                if(eval == 256){
                    overflow = true;
                    boolean flag = true;
                    for (int i = 0; i < newCoefficients.size() && flag; i++) {
                        if(newCoefficients.get(i) != 0){
                            newCoefficients.add(i, newCoefficients.get(i)-1);
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

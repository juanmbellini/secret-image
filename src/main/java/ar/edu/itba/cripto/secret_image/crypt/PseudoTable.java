package ar.edu.itba.cripto.secret_image.crypt;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PseudoTable {

    private PseudoTable(){

    }



    /**
     *
     * @param imageSize size of image raw data
     * @param seed seed to use in Random
     * @return pseudorandom table used to perform XOR operation
     */
    public static List<Integer> generatePseudoTable(int imageSize, int seed){
        Random random = new Random(seed);
        List<Integer> pseudoTable = new ArrayList<>();
        for (int i = 0; i < imageSize; i++) {
            pseudoTable.add(random.nextInt(256));
        }
        return pseudoTable;
    }
}

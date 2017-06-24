package ar.edu.itba.cripto.secret_image;

import ar.edu.itba.cripto.secret_image.bmp.BmpEditor;
import ar.edu.itba.cripto.secret_image.bmp.BmpUtils;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 */
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");

        //final BufferedImage image = ImageIO.read(Main.class.getResourceAsStream("/shadows/1.bmp"));
        BmpUtils bmpUtils = new BmpUtils("/home/lelv/Documents/cripto/EjemploSinSecretoSolobmp/Facundo.bmp",1);
        BmpEditor editor = bmpUtils.edit();

        editor.insertSecret(129);
    }


}

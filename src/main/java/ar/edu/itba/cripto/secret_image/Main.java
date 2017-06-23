package ar.edu.itba.cripto.secret_image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Hello world!
 */
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");

        final BufferedImage image = ImageIO.read(Main.class.getResourceAsStream("/shadows/1.bmp"));

    }


}

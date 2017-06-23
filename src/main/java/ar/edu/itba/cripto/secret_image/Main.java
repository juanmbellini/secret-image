package ar.edu.itba.cripto.secret_image;

import com.beust.jcommander.Parameter;

import java.io.IOException;

/**
 * Entry point
 */
public class Main {

    @Parameter(names = {"-h", "--help"}, description = "Print usage.", help = true)
    private boolean usage = false;

    /**
     * Indicates whether the execution will provide shadow images.
     */
    @Parameter(names = {"-d"}, description = "Run in distribution mode.")
    private Boolean distribution = false;

    /**
     * Indicates whether the execution will recover the secret image from shadow images.
     */
    @Parameter(names = {"-r"}, description = "Run in recovery mode.")
    private Boolean recovery = false;

    /**
     * Indicates the path of the secret image.
     * In case the execution is running in distribution mode, this will be the secret image to distribute.
     * In case the execution is running in recovery mode, this will be the target path
     * (i.e where the recovered secret image will be saved)
     */
    @Parameter(names = {"-secret"}, description = "Path to secret image. In distribution mode, this must be the path" +
            " to the secret image to distribute. In recovery mode, this must be the target path (i.e where " +
            " the recovered secret image will be saved.")
    private String secretImagePath;

    /**
     * Indicates the minimum amount of shadows that are needed to recover the secret image.
     */
    @Parameter(names = {"-k"}, description = "The minimum amount of shadows needed to recover the secret image.")
    private Integer minimumShadows;

    /**
     * Indicates the amount of shadows to be created.
     * It can only be used in distribution mode.
     * If it is not set, default will be the amount of images in the shadows directory
     * (i.e {@link Main#shadowsDirectory}).
     */
    @Parameter(names = {"-n"}, description = "The amount of shadows to be created." +
            " Must only be used in distribution mode." +
            " If not set, the amount of images in the set directory (see -dir param) will be used.")
    private Integer amountOfShadows;

    @Parameter(names = {"-dir"}, description = "The path to the directory containing the shadow images." +
            " When running in distribution mode, the directory must contain the images in which the secret" +
            " will be saved." +
            " When running in recovery mode, the directory must contain the images with the hidden secret." +
            " If not set, the current working directory will be used.")
    private String shadowsDirectory = "./";


    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");
    }


}

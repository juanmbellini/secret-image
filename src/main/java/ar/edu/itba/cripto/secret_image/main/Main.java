package ar.edu.itba.cripto.secret_image.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Entry point class.
 */
public class Main implements Runnable {


    // ========================================================
    // Running parameters
    // ========================================================

    @Parameter(names = {"-h", "--help"},
            description = "Print usage.",
            help = true)
    private boolean usage = false;

    /**
     * Indicates whether the execution will provide shadow images.
     */
    @Parameter(names = {"-d"},
            description = "Run in distribution mode.")
    private boolean distribution;

    /**
     * Indicates whether the execution will recover the secret image from shadow images.
     */
    @Parameter(names = {"-r"},
            description = "Run in recovery mode.")
    private boolean recovery;

    /**
     * Indicates the path of the secret image.
     * In case the execution is running in distribution mode, this will be the secret image to distribute.
     * In case the execution is running in recovery mode, this will be the target path
     * (i.e where the recovered secret image will be saved)
     */
    @Parameter(names = {"-secret"},
            required = true,
            description = "Path to secret image. In distribution mode, this must be the path" +
                    " to the secret image to distribute. In recovery mode, this must be the target path (i.e where " +
                    " the recovered secret image will be saved).")
    private String secretImagePath;

    /**
     * Indicates the minimum amount of shadows that are needed to recover the secret image.
     */
    @Parameter(names = {"-k"},
            required = true,
            description = "The minimum amount of shadows needed to recover the secret image.",
            validateWith = PositiveIntegerValidator.class)
    private int minimumShadows;

    /**
     * Indicates the amount of shadows to be created.
     * It can only be used in distribution mode.
     * If it is not set, default will be the amount of images in the shadows directory
     * (i.e {@link Main#shadowsDirectory}).
     */
    @Parameter(names = {"-n"},
            description = "Optional. The amount of shadows to be created." +
                    " Must be greater or equal than the minimum amount of shadows." +
                    " Must only be used in distribution mode." +
                    " If not set, the amount of images in the set directory (see -dir param) will be used.",
            validateWith = PositiveIntegerValidator.class)
    private Integer amountOfShadows;

    @Parameter(names = {"-dir"},
            description = "Optional. The path to the directory containing the shadow images." +
                    " When running in distribution mode, the directory must contain the images in which the secret" +
                    " will be saved." +
                    " When running in recovery mode, the directory must contain the images with the hidden secret." +
                    " If not set, the current working directory will be used.")
    private String shadowsDirectory = "./";


    // ========================================================
    // Main class instance variables
    // ========================================================


    /**
     * A {@link JCommander} instance for parsing execution parameters.
     */
    private final JCommander jCommander;


    // ========================================================
    // Constructor and methods
    // ========================================================


    /**
     * Constructor.
     */
    private Main() {
        this.jCommander = new JCommander(this);
        this.jCommander.setProgramName("java -jar <path-to-jar>");
    }

    /**
     * Prints usage message.
     */
    private void printUsage() {
        this.jCommander.usage();
        System.out.flush();
    }

    /**
     * Performs parameters parsing.
     */
    private void parseParameters(String[] args) {
        this.jCommander.parse(args);
    }

    @Override
    public void run() {
        if (this.usage) {
            this.jCommander.usage();
            return;
        }
        this.validateParameters();
        if (distribution) {
            final Encryption encryptor =
                    new Encryption(minimumShadows, amountOfShadows, secretImagePath, shadowsDirectory);
            encryptor.encrypt();
            return;
        }

        final Decryptor decryptor =
                new Decryptor(minimumShadows, secretImagePath, shadowsDirectory);
        decryptor.decrypt();
    }

    /**
     * Performs global parameters validation.
     */
    private void validateParameters() {
        // Check that at least one execution mode is specified.
        if (!distribution && !recovery) {
            throw new ParameterException("Fatal. No execution mode was specified.");
        }
        // Check that only one execution mode is specified.
        if (distribution && recovery) {
            throw new ParameterException("Fatal. Only one execution mode must be specified.");
        }
        if (minimumShadows < 2) {
            throw new ParameterException("Fatal. At least 2 shadows are needed.");
        }
        // Check that the amount of shadows is not smaller than the minimum amount of shadows needed
        // to recover the secret image.
        if (amountOfShadows != null && minimumShadows > amountOfShadows) {
            throw new ParameterException("Fatal. " +
                    "The amount of shadows must be greater or equal than the minimum amount of shadows needed." +
                    " Hint: n >= k");
        }
    }


    // ========================================================
    // Main execution
    // ========================================================


    /**
     * Entry point.
     *
     * @param args Execution arguments.
     */
    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.parseParameters(args);
            main.run();
        } catch (Throwable e) {
            System.err.println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
            System.err.flush();
            main.printUsage();
            System.exit(1);
        }
    }

}

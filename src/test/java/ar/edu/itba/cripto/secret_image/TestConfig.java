package ar.edu.itba.cripto.secret_image;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Test {@link Configuration} class.
 */
@Configuration
@ComponentScan({"ar.edu.itba.cripto.secret_image.math_utils",})
public class TestConfig {
    // Nothing here... Just to initialize Spring test runner.
}

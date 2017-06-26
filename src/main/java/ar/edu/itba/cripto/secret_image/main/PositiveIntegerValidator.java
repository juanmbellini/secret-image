package ar.edu.itba.cripto.secret_image.main;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.validators.PositiveInteger;

/**
 * An extension of {@link PositiveInteger} parameter validator,
 * in which the {@link ParameterException} thrown has its message changed.
 */
public class PositiveIntegerValidator extends PositiveInteger {

    @Override
    public void validate(String name, String value) throws ParameterException {
        try {
            super.validate(name, value);
        } catch (NumberFormatException e) {
            throw new ParameterException("Fatal. Parameter " + name + " must be a positive integer.");
        } catch (ParameterException e) {
            throw new ParameterException("Fatal. " + e.getMessage());
        }
    }
}

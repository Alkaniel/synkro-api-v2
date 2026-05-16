package fr.enzogiardinelli.synkro.validation;

import fr.enzogiardinelli.synkro.annotations.ValidEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Stream;

public class EnumValidator implements ConstraintValidator<ValidEnum, CharSequence> {
    private List<String> acceptedValues;

    @Override
    public void initialize(ValidEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) return true; // null values are valid, use @NotNull to enforce non-null

        String sanitizedValue = value.toString().toUpperCase();

        return acceptedValues.contains(sanitizedValue) || acceptedValues.contains("ROLE" + sanitizedValue);
    }
}

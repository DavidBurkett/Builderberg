package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.enums.BuilderConstraintOption;
import com.github.davidburkett.builderberg.model.BuilderConstraintOptionAndValue;
import com.github.davidburkett.builderberg.utilities.ValidationUtility;
import com.intellij.psi.PsiField;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ValidationJavadocGenerator {

    private ValidationJavadocGenerator() {
    }

    public static List<String> generateValidationComments(final PsiField field, final String prefix) {
        final List<String> validationComments = new ArrayList<>();

        final List<BuilderConstraintOptionAndValue> builderConstraints =
                ValidationUtility.getBuilderConstraintsForField(field);

        for (final BuilderConstraintOptionAndValue builderConstraintOptionAndValue : builderConstraints) {
            final Optional<String> optionalValidationComment = generateValidationComment(
                    prefix,
                    builderConstraintOptionAndValue
            );
            optionalValidationComment.ifPresent(validationComments::add);
        }

        return validationComments;
    }

    private static Optional<String> generateValidationComment(
            final String prefix,
            final BuilderConstraintOptionAndValue builderConstraintOptionAndValue
    ) {
        final BuilderConstraintOption builderContraintOption = builderConstraintOptionAndValue.getOption();

        switch (builderContraintOption) {
            case NOT_NULL:
                return Optional.of(String.format("%s not be null.", prefix));
            case NOT_EMPTY:
                return Optional.of(String.format("%s not be null or empty.", prefix));
            case NOT_BLANK:
                return Optional.of(String.format("%s not be null, empty, or blank.", prefix));
            case NO_NULL_KEYS:
                return Optional.of(String.format("%s not contain any null keys.", prefix));
            case NO_NULL_VALUES:
                return Optional.of(String.format("%s not contain any null values.", prefix));
            case NOT_NEGATIVE:
                return Optional.of(String.format("%s not be negative.", prefix));
            case NOT_POSITIVE:
                return Optional.of(String.format("%s not be positive.", prefix));
            case NEGATIVE_ONLY:
                return Optional.of(String.format("%s be negative.", prefix));
            case POSITIVE_ONLY:
                return Optional.of(String.format("%s be positive.", prefix));
            case MIN_VALUE:
                return Optional.of(String.format("%s be >= %s.", prefix, builderConstraintOptionAndValue.getValue()));
            case MAX_VALUE:
                return Optional.of(String.format("%s be <= %s.", prefix, builderConstraintOptionAndValue.getValue()));
            case CUSTOM:
                return Optional.of("Contains custom validation.");
        }

        return Optional.empty();
    }
}

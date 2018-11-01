package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.ValidationUtility;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiNameValuePair;
import org.fest.util.Lists;

import java.util.List;

public class ValidationJavadocGenerator {
    public static List<String> generateValidationComments(final PsiField field, final String prefix) {
        final List<String> validationComments = Lists.newArrayList();

        final List<PsiNameValuePair> builderConstraints = ValidationUtility.getBuilderConstraintsForField(field);
        for (final PsiNameValuePair builderConstraint : builderConstraints) {
            final String validationComment = generateValidationComment(prefix, builderConstraint);
            validationComments.add(validationComment);
        }

        return validationComments;
    }

    // TODO: Put some more thought into this and make sure to handle combining constraints that affect each other.
    private static String generateValidationComment(final String prefix, final PsiNameValuePair builderConstraint) {
        final String attributeName = builderConstraint.getName();
        if (attributeName.equals("notNull")) {
            return prefix + " not be null.";
        } else if (attributeName.equals("notEmpty")) {
            return prefix + " not be null or empty.";
        } else if (attributeName.equals("notBlank")) {
            return prefix + " not be null, empty, or blank.";
        } else if (attributeName.equals("noNullKeys")) {
            return prefix + " not contain any null keys.";
        } else if (attributeName.equals("noNullValues")) {
            return prefix + " not contain any null values.";
        } else if (attributeName.equals("notNegative")) {
            return prefix + " not be negative.";
        } else if (attributeName.equals("notPositive")) {
            return prefix + " not be positive.";
        } else if (attributeName.equals("negativeOnly")) {
            return prefix + " be negative.";
        } else if (attributeName.equals("positiveOnly")) {
            return prefix + " be positive.";
        } else if (attributeName.equals("minValue")) {
            return prefix + " be >= " + builderConstraint.getLiteralValue() + ".";
        } else if (attributeName.equals("maxValue")) {
            return prefix + " be <= " + builderConstraint.getLiteralValue() + ".";
        } else if (attributeName.equals("customValidation")) {
            return "Contains custom validation.";
        }

        throw new IllegalArgumentException();
    }
}

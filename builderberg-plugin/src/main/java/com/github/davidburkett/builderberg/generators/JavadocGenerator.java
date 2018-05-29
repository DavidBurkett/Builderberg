package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.JavadocUtil;
import com.github.davidburkett.builderberg.utilities.ValidationUtility;
import com.intellij.psi.*;
import org.fest.util.Lists;

import java.util.List;


public class JavadocGenerator {
    private final PsiElementFactory psiElementFactory;

    public JavadocGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    /**
     * Generates the javadoc comment for the given getter methods using information from the {@link PsiField field}.
     * @param getterMethod The non-null getter {@link PsiMethod method}.
     * @param field The non-null {@link PsiField field}.
     */
    // TODO: This is broken and results in unnecessary new lines in generated javadoc.
    public void generateCommentForGetterMethod(final PsiMethod getterMethod, final PsiField field) {
        final StringBuilder getterCommentBuilder= new StringBuilder("/**\n* @return ");

        final String fieldJavaDoc = JavadocUtil.getFieldCommentText(field);
        getterCommentBuilder.append(fieldJavaDoc != null ? fieldJavaDoc : field.getName());

        final List<String> validationComments = generateValidationComments(field);
        for (final String validationComment : validationComments) {
            getterCommentBuilder.append("\n" + validationComment);
        }

        getterCommentBuilder.append("\n*/");

        final PsiComment getterComment = psiElementFactory.createCommentFromText(getterCommentBuilder.toString(), getterMethod);
        JavadocUtil.setMethodComment(getterMethod, getterComment);
    }

    private List<String> generateValidationComments(final PsiField field) {
        final List<String> validationComments = Lists.newArrayList();

        final List<PsiNameValuePair> builderConstraints = ValidationUtility.getBuilderConstraintsForField(field);
        for (final PsiNameValuePair builderConstraint : builderConstraints) {
            final String validationComment = generateValidationComment(field, builderConstraint);
            validationComments.add(validationComment);
        }

        return validationComments;
    }

    // TODO: Put some more thought into this and make sure to handle combining constraints that affect each other.
    private String generateValidationComment(final PsiField field, final PsiNameValuePair builderConstraint) {
        final String attributeName = builderConstraint.getName();
        if (attributeName.equals("notNull")) {
            return "Guaranteed to not be null.";
        } else if (attributeName.equals("notEmpty")) {
            return "Guaranteed to not be null or empty.";
        } else if (attributeName.equals("notBlank")) {
            return "Guaranteed to not be null, empty, or blank.";
        } else if (attributeName.equals("noNullKeys")) {
            return "Guaranteed to not contain any null keys.";
        } else if (attributeName.equals("noNullValues")) {
            return "Guaranteed to not contain any null values.";
        } else if (attributeName.equals("notNegative")) {
            return "Guaranteed to not be negative.";
        } else if (attributeName.equals("notPositive")) {
            return "Guaranteed to not be positive.";
        } else if (attributeName.equals("negativeOnly")) {
            return "Guaranteed to be negative.";
        } else if (attributeName.equals("positiveOnly")) {
            return "Guaranteed to be positive.";
        } else if (attributeName.equals("minValue")) {
            return "Guaranteed to be >= " + builderConstraint.getLiteralValue() + ".";
        } else if (attributeName.equals("maxValue")) {
            return "Guaranteed to be <= " + builderConstraint.getLiteralValue() + ".";
        } else if (attributeName.equals("customValidation")) {
            return "Contains custom validation.";
        }

        throw new IllegalArgumentException();
    }
}

package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.JavadocUtil;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.github.davidburkett.builderberg.utilities.ValidationUtility;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameValuePair;
import org.fest.util.Lists;

import java.util.List;

public class JavadocGenerator {
    private final PsiElementFactory psiElementFactory;
    private final MethodUtility methodUtility;

    public JavadocGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    public void generateInheritDocJavadocForMethod(final PsiMethod method) {
        methodUtility.addJavadoc(method, ImmutableList.of("{@inheritDoc}"));
    }

    /**
     * Generates the javadoc comment for the given getter methods using information from the {@link PsiField field}.
     * @param getterMethod The non-null getter {@link PsiMethod method}.
     * @param field The non-null {@link PsiField field}.
     */
    // TODO: This is broken and results in unnecessary new lines in generated javadoc.
    public void generateCommentForGetterMethod(final PsiMethod getterMethod, final PsiField field) {
        final List<String> javadocLines = Lists.newArrayList();

        final String fieldJavaDoc = JavadocUtil.getFieldCommentText(field);
        final String returnJavadoc = fieldJavaDoc != null ? fieldJavaDoc : field.getName();
        javadocLines.add("@return " + returnJavadoc);

        final List<String> validationComments = generateValidationComments(field, "Guaranteed to");
        javadocLines.addAll(validationComments);

        methodUtility.addJavadoc(getterMethod, javadocLines);
    }

    public void generateCommentForSetterMethod(final PsiMethod withMethod, final PsiField field) {
        final List<String> javadocLines = Lists.newArrayList();

        final String fieldCommentText = JavadocUtil.getFieldCommentText(field);
        final String fieldJavaDoc = fieldCommentText != null ? fieldCommentText : "";
        javadocLines.add("@param " + field.getName() + " " + fieldJavaDoc);

        final List<String> validationComments = generateValidationComments(field, "Must");
        javadocLines.addAll(validationComments);

        methodUtility.addJavadoc(withMethod, javadocLines);
    }

    private List<String> generateValidationComments(final PsiField field, final String prefix) {
        final List<String> validationComments = Lists.newArrayList();

        final List<PsiNameValuePair> builderConstraints = ValidationUtility.getBuilderConstraintsForField(field);
        for (final PsiNameValuePair builderConstraint : builderConstraints) {
            final String validationComment = generateValidationComment(prefix, builderConstraint);
            validationComments.add(validationComment);
        }

        return validationComments;
    }

    // TODO: Put some more thought into this and make sure to handle combining constraints that affect each other.
    private String generateValidationComment(final String prefix, final PsiNameValuePair builderConstraint) {
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

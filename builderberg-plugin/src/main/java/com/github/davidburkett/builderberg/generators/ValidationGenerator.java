package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;
import org.fest.util.Lists;

import java.util.List;

public class ValidationGenerator {
    private final Project project;
    private final PsiElementFactory psiElementFactory;

    public ValidationGenerator(final Project project, final PsiElementFactory psiElementFactory) {
        this.project = project;
        this.psiElementFactory = psiElementFactory;
    }

    public List<PsiElement> generateValidationForField(final PsiMethod method, final PsiField field) {
        final List<PsiElement> validationStatements = Lists.newArrayList();
        final PsiAnnotation[] annotations = field.getAnnotations();
        for (final PsiAnnotation annotation : annotations) {
            if (annotation.getQualifiedName().equals("com.github.davidburkett.builderberg.annotations.BuilderConstraint")) {
                final PsiAnnotationParameterList annotationParameterList = annotation.getParameterList();
                final PsiNameValuePair[] attributes = annotationParameterList.getAttributes();
                for (final PsiNameValuePair attribute : attributes) {
                    if (attribute.getLiteralValue().equals("true")) {
                        final PsiStatement validationStatement = generateValidationStatement(method, field, attribute);
                        validationStatements.add(validationStatement);
                    }
                }
            }
        }

        return validationStatements;
    }

    private PsiStatement generateValidationStatement(final PsiMethod method, final PsiField field, final PsiNameValuePair attribute) {
        final String attributeName = attribute.getName();
        if (attributeName.equals("notNull")) {
            return generateNotNullStatement(method, field);
        } else if (attributeName.equals("notEmpty")) {
            return generateNotEmptyStatement(method, field);
        } else if (attributeName.equals("notBlank")) {
            return generateNotBlankStatement(method, field);
        } else if (attributeName.equals("noNullKeys")) {
            return generateNoNullKeysStatement(method, field);
        } else if (attributeName.equals("noNullValues")) {
            return generateNoNullValuesStatement(method, field);
        } else if (attributeName.equals("notNegative")) {
            return generateNotNegativeStatement(method, field);
        } else if (attributeName.equals("notPositive")) {
            return generateNotPositiveStatement(method, field);
        } else if (attributeName.equals("negativeOnly")) {
            return generateNegativeOnlyStatement(method, field);
        } else if (attributeName.equals("positiveOnly")) {
            return generatePositiveOnlyStatement(method, field);
        } else if (attributeName.equals("minValue")) {
            return generateMinValueStatement(method, field, Double.parseDouble(attribute.getLiteralValue()));
        } else if (attributeName.equals("maxValue")) {
            return generateMaxValueStatement(method, field, Double.parseDouble(attribute.getLiteralValue()));
        } else if (attributeName.equals("customValidation")) {
            return generateCustomValidationStatement(method, field, attribute.getLiteralValue());
        }

        throw new IllegalArgumentException();
    }

    private PsiStatement generateNotNullStatement(final PsiMethod method, final PsiField field) {
        final String assertStatement = String.format("assert(%s != null);", field.getName());
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateNotEmptyStatement(final PsiMethod method, final PsiField field) {
        final PsiType type = field.getType();
        final String fieldName = field.getName();
        if (TypeUtility.isString(type, method) || TypeUtility.isCollection(type)) {
            final String assertStatement = String.format("assert(!%s.isEmpty());", fieldName);
            return psiElementFactory.createStatementFromText(assertStatement, method);
        } else if (type instanceof PsiArrayType) {
            final String assertStatement = String.format("assert(%s.length > 0);", fieldName);
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new IllegalArgumentException();
    }

    private PsiStatement generateNotBlankStatement(final PsiMethod method, final PsiField field) {
        final PsiType type = field.getType();
        if (type.equals(TypeUtils.getStringType(method))) {
            return psiElementFactory.createStatementFromText("assert(!" + field.getName() + ".trim().isEmpty());", method);
        }

        throw new IllegalArgumentException();
    }

    private PsiStatement generateNoNullKeysStatement(final PsiMethod method, final PsiField field) {
        final PsiType fieldType = field.getType();
        final PsiType keyType = TypeUtility.getGenericKeyType(fieldType);

        final String forLoop = String.format("for (final %s key : %s.keySet())", keyType.getCanonicalText(), field.getName());
        final String assertStatement = "assert(key != null);";
        return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
    }

    private PsiStatement generateNoNullValuesStatement(final PsiMethod method, final PsiField field) {
        final PsiType fieldType = field.getType();
        final PsiType valueType = TypeUtility.getGenericValueType(fieldType);

        final String fieldName = field.getName();
        final String valueTypeName = valueType.getCanonicalText();

        if (TypeUtility.isMap(fieldType)) {
            final String forLoop = String.format("for (final %s value : %s.values())", valueTypeName, fieldName);
            final String assertStatement = "assert(value != null);";
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        } else {
            final String forLoop = String.format("for (final %s value : %s)", valueTypeName, fieldName);
            final String assertStatement = "assert(value != null);";
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        }
    }

    private PsiStatement generateNotNegativeStatement(final PsiMethod method, final PsiField field) {
        final String assertStatement = String.format("assert(%s >= 0);", field.getName());
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateNotPositiveStatement(final PsiMethod method, final PsiField field) {
        final String assertStatement = String.format("assert(%s <= 0);", field.getName());
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateNegativeOnlyStatement(final PsiMethod method, final PsiField field) {
        final String assertStatement = String.format("assert(%s < 0);", field.getName());
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generatePositiveOnlyStatement(final PsiMethod method, final PsiField field) {
        final String assertStatement = String.format("assert(%s > 0);", field.getName());
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateMinValueStatement(final PsiMethod method, final PsiField field, final double minValue) {
        final String assertStatement = String.format("assert(%s >= %f);", field.getName(), minValue);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateMaxValueStatement(final PsiMethod method, final PsiField field, final double maxValue) {
        final String assertStatement = String.format("assert(%s <= %f);", field.getName(), maxValue);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateCustomValidationStatement(final PsiMethod method, final PsiField field, final String validationText) {
        return psiElementFactory.createStatementFromText(validationText, method);
    }
}

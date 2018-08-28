package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.BuilderOptionUtility;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.github.davidburkett.builderberg.utilities.ValidationUtility;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;
import org.fest.util.Lists;

import java.util.List;

/**
 * Generates field validation logic to handle BuilderConstraints.
 * @author David Burkett
 */
public class ValidationGenerator {
    private final Project project;
    private final PsiElementFactory psiElementFactory;

    public ValidationGenerator(final Project project, final PsiElementFactory psiElementFactory) {
        this.project = project;
        this.psiElementFactory = psiElementFactory;
    }

    /**
     * Generates validation logic for all of the BuilderConstraints the field is annotated with, within the context of the given method.
     * NOTE: This generates the validation statements, but does not add them to the method.
     * @param method The {@link PsiMethod} the validation logic will be added to.
     * @param field The {@link PsiField} to validate.
     * @return The {@link List} of generated validation {@link PsiStatement}s.
     */
    public List<PsiStatement> generateValidationForField(final PsiClass topLevelClass, final PsiMethod method, final PsiField field) {
        final List<PsiStatement> validationStatements = Lists.newArrayList();

        final PsiType exceptionType = BuilderOptionUtility.exceptionType(topLevelClass);
        final List<PsiNameValuePair> builderConstraints = ValidationUtility.getBuilderConstraintsForField(field);
        for (final PsiNameValuePair builderConstraint : builderConstraints) {
            final PsiStatement validationStatement = generateValidationStatement(method, field, builderConstraint, exceptionType);
            validationStatements.add(validationStatement);
        }

        return validationStatements;
    }

    private PsiStatement generateValidationStatement(final PsiMethod method, final PsiField field, final PsiNameValuePair attribute, final PsiType exceptionType) {
        final String attributeName = attribute.getName();
        if (attributeName.equals("notNull")) {
            return generateNotNullStatement(method, field, exceptionType);
        } else if (attributeName.equals("notEmpty")) {
            return generateNotEmptyStatement(method, field, exceptionType);
        } else if (attributeName.equals("notBlank")) {
            return generateNotBlankStatement(method, field, exceptionType);
        } else if (attributeName.equals("noNullKeys")) {
            return generateNoNullKeysStatement(method, field, exceptionType);
        } else if (attributeName.equals("noNullValues")) {
            return generateNoNullValuesStatement(method, field, exceptionType);
        } else if (attributeName.equals("notNegative")) {
            return generateNotNegativeStatement(method, field, exceptionType);
        } else if (attributeName.equals("notPositive")) {
            return generateNotPositiveStatement(method, field, exceptionType);
        } else if (attributeName.equals("negativeOnly")) {
            return generateNegativeOnlyStatement(method, field, exceptionType);
        } else if (attributeName.equals("positiveOnly")) {
            return generatePositiveOnlyStatement(method, field, exceptionType);
        } else if (attributeName.equals("minValue")) {
            return generateMinValueStatement(method, field, Double.parseDouble(attribute.getLiteralValue()), exceptionType);
        } else if (attributeName.equals("maxValue")) {
            return generateMaxValueStatement(method, field, Double.parseDouble(attribute.getLiteralValue()), exceptionType);
        } else if (attributeName.equals("customValidation")) {
            return generateCustomValidationStatement(method, field, attribute.getLiteralValue());
        }

        throw new IllegalArgumentException();
    }

    private PsiStatement generateNotNullStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final String criteria = String.format("%s != null", field.getName());
        final String assertStatement = generateAssertion(criteria, exceptionType);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateNotEmptyStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final PsiType type = field.getType();
        final String fieldName = field.getName();
        if (TypeUtility.isString(type, method) || TypeUtility.isCollection(type)) {
            final String criteria = String.format("!%s.isEmpty()", fieldName);
            final String assertStatement = generateAssertion(criteria, exceptionType);
            return psiElementFactory.createStatementFromText(assertStatement, method);
        } else if (type instanceof PsiArrayType) {
            final String criteria = String.format("%s.length > 0", fieldName);
            final String assertStatement = generateAssertion(criteria, exceptionType);
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new IllegalArgumentException();
    }

    private PsiStatement generateNotBlankStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final PsiType type = field.getType();
        if (type.equals(TypeUtils.getStringType(method))) {
            final String criteria = "!" + field.getName() + ".trim().isEmpty()";
            final String assertStatement = generateAssertion(criteria, exceptionType);
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new IllegalArgumentException();
    }

    private PsiStatement generateNoNullKeysStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final PsiType fieldType = field.getType();
        final PsiType keyType = TypeUtility.getGenericKeyType(fieldType);

        final String forLoop = String.format("for (final %s key : %s.keySet())", keyType.getCanonicalText(), field.getName());
        final String criteria = "key != null";
        final String assertStatement = generateAssertion(criteria, exceptionType);
        return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
    }

    private PsiStatement generateNoNullValuesStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final PsiType fieldType = field.getType();
        final PsiType valueType = TypeUtility.getGenericValueType(fieldType);

        final String fieldName = field.getName();
        final String valueTypeName = valueType.getCanonicalText();

        if (TypeUtility.isMap(fieldType)) {
            final String forLoop = String.format("for (final %s value : %s.values())", valueTypeName, fieldName);
            final String criteria = "value != null";
            final String assertStatement = generateAssertion(criteria, exceptionType);
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        } else {
            final String forLoop = String.format("for (final %s value : %s)", valueTypeName, fieldName);
            final String criteria = "value != null";
            final String assertStatement = generateAssertion(criteria, exceptionType);
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        }
    }

    private PsiStatement generateNotNegativeStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final String criteria = String.format("%s >= 0", field.getName());
        final String assertStatement = generateAssertion(criteria, exceptionType);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateNotPositiveStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final String criteria = String.format("%s <= 0", field.getName());
        final String assertStatement = generateAssertion(criteria, exceptionType);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateNegativeOnlyStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final String criteria = String.format("%s < 0", field.getName());
        final String assertStatement = generateAssertion(criteria, exceptionType);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generatePositiveOnlyStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) {
        final String criteria = String.format("%s > 0", field.getName());
        final String assertStatement = generateAssertion(criteria, exceptionType);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateMinValueStatement(final PsiMethod method, final PsiField field, final double minValue, final PsiType exceptionType) {
        final String criteria = String.format("%s >= %f", field.getName(), minValue);
        final String assertStatement = generateAssertion(criteria, exceptionType);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateMaxValueStatement(final PsiMethod method, final PsiField field, final double maxValue, final PsiType exceptionType) {
        final String criteria = String.format("%s <= %f", field.getName(), maxValue);
        final String assertStatement = generateAssertion(criteria, exceptionType);
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateCustomValidationStatement(final PsiMethod method, final PsiField field, final String validationText) {
        return psiElementFactory.createStatementFromText(validationText, method);
    }

    private String generateAssertion(final String criteria, final PsiType exceptionType) {
        return String.format("if (!(%s)) { throw new %s(\"Constraint not met: %s\"); }", criteria, exceptionType.getCanonicalText(), criteria);
    }
}

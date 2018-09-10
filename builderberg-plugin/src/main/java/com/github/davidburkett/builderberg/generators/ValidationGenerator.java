package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.exceptions.InvalidConstraintException;
import com.github.davidburkett.builderberg.utilities.BuilderOptionUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.github.davidburkett.builderberg.utilities.ValidationUtility;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;
import org.fest.util.Lists;

import java.util.List;

/**
 * Generates field validation logic to handle BuilderConstraints.
 * @author David Burkett
 */
public class ValidationGenerator {
    private final PsiElementFactory psiElementFactory;
    private final MethodUtility methodUtility;

    public ValidationGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    /**
     * Generates validation logic for all of the BuilderConstraints the field is annotated with, within the context of the given method.
     * NOTE: This generates the validation statements, but does not add them to the method.
     * @param method The {@link PsiMethod} the validation logic will be added to.
     * @param field The {@link PsiField} to validate.
     */
    public void generateValidationForField(final PsiClass topLevelClass, final PsiMethod method, final PsiField field) throws InvalidConstraintException {
        final List<PsiStatement> validationStatements = Lists.newArrayList();

        final PsiType exceptionType = BuilderOptionUtility.exceptionType(topLevelClass);
        final List<PsiNameValuePair> builderConstraints = ValidationUtility.getBuilderConstraintsForField(field);
        for (final PsiNameValuePair builderConstraint : builderConstraints) {
            final PsiStatement validationStatement = generateValidationStatement(method, field, builderConstraint, exceptionType);
            validationStatements.add(validationStatement);
        }

        methodUtility.addStatements(method, validationStatements);
    }

    private PsiStatement generateValidationStatement(final PsiMethod method, final PsiField field, final PsiNameValuePair attribute, final PsiType exceptionType) throws InvalidConstraintException {
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

        throw new InvalidConstraintException(field, attributeName);
    }

    private PsiStatement generateNotNullStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        if (field.getType() instanceof PsiPrimitiveType) {
            throw new InvalidConstraintException(field, "notNull");
        }

        final String criteria = String.format("%s == null", field.getName());
        final String assertStatement = generateAssertion(criteria, exceptionType, "notNull");
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateNotEmptyStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        final PsiType type = field.getType();
        final String fieldName = field.getName();
        if (TypeUtility.isString(type, method) || TypeUtility.isCollection(type)) {
            final String criteria = String.format("%s.isEmpty()", fieldName);
            final String assertStatement = generateAssertion(criteria, exceptionType, "notEmpty");
            return psiElementFactory.createStatementFromText(assertStatement, method);
        } else if (type instanceof PsiArrayType) {
            final String criteria = String.format("%s.length == 0", fieldName);
            final String assertStatement = generateAssertion(criteria, exceptionType, "notEmpty");
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "notEmpty");
    }

    private PsiStatement generateNotBlankStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        final PsiType type = field.getType();
        if (type.equals(TypeUtils.getStringType(method))) {
            final String criteria = field.getName() + ".trim().isEmpty()";
            final String assertStatement = generateAssertion(criteria, exceptionType, "notBlank");
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "notBlank");
    }

    private PsiStatement generateNoNullKeysStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        final PsiType fieldType = field.getType();

        if (TypeUtility.isMap(fieldType)) {
            final PsiType keyType = TypeUtility.getGenericKeyType(fieldType);

            final String forLoop = String.format("for (final %s key : %s.keySet())", keyType.getCanonicalText(), field.getName());
            final String criteria = "key == null";
            final String assertStatement = generateAssertion(criteria, exceptionType, "noNullKeys");
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        }

        throw new InvalidConstraintException(field, "noNullKeys");
    }

    private PsiStatement generateNoNullValuesStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        final PsiType fieldType = field.getType();
        final PsiType valueType = TypeUtility.getGenericValueType(fieldType);

        final String fieldName = field.getName();
        final String valueTypeName = valueType.getCanonicalText();

        if (TypeUtility.isMap(fieldType)) {
            final String forLoop = String.format("for (final %s value : %s.values())", valueTypeName, fieldName);
            final String criteria = "value == null";
            final String assertStatement = generateAssertion(criteria, exceptionType, "noNullValues");
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        } else if (TypeUtility.isCollection(fieldType) || fieldType instanceof PsiArrayType){
            final String forLoop = String.format("for (final %s value : %s)", valueTypeName, fieldName);
            final String criteria = "value == null";
            final String assertStatement = generateAssertion(criteria, exceptionType, "noNullValues");
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        }

        throw new InvalidConstraintException(field, "noNullValues");
    }

    private PsiStatement generateNotNegativeStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s < 0", field.getName());
            final String assertStatement = generateAssertion(criteria, exceptionType, "notNegative");
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "notNegative");
    }

    private PsiStatement generateNotPositiveStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s > 0", field.getName());
            final String assertStatement = generateAssertion(criteria, exceptionType, "notPositive");
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "notPositive");
    }

    private PsiStatement generateNegativeOnlyStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s >= 0", field.getName());
            final String assertStatement = generateAssertion(criteria, exceptionType, "negativeOnly");
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "negativeOnly");
    }

    private PsiStatement generatePositiveOnlyStatement(final PsiMethod method, final PsiField field, final PsiType exceptionType) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s <= 0", field.getName());
            final String assertStatement = generateAssertion(criteria, exceptionType, "positiveOnly");
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "positiveOnly");
    }

    private PsiStatement generateMinValueStatement(final PsiMethod method, final PsiField field, final double minValue, final PsiType exceptionType) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s < %f", field.getName(), minValue);
            final String assertStatement = generateAssertion(criteria, exceptionType, "minValue: " + minValue);
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "minValue");
    }

    private PsiStatement generateMaxValueStatement(final PsiMethod method, final PsiField field, final double maxValue, final PsiType exceptionType) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s > %f", field.getName(), maxValue);
            final String assertStatement = generateAssertion(criteria, exceptionType, "maxValue: " + maxValue);
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "maxValue");
    }

    private PsiStatement generateCustomValidationStatement(final PsiMethod method, final PsiField field, final String validationText) throws InvalidConstraintException {
        return psiElementFactory.createStatementFromText(validationText, method);
    }

    private String generateAssertion(final String criteria, final PsiType exceptionType, final String constraint) {
        return String.format("if (%s) { throw new %s(\"Constraint not met: %s\"); }", criteria, exceptionType.getCanonicalText(), constraint);
    }
}

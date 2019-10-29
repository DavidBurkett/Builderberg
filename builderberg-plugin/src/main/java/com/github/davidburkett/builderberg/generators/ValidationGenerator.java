package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.enums.BuilderConstraintOption;
import com.github.davidburkett.builderberg.exceptions.InvalidConstraintException;
import com.github.davidburkett.builderberg.model.BuilderConstraintOptionAndValue;
import com.github.davidburkett.builderberg.utilities.BuilderOptionUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.github.davidburkett.builderberg.utilities.ValidationUtility;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates field validation logic to handle BuilderConstraints.
 *
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
     *
     * @param method The {@link PsiMethod} the validation logic will be added to.
     * @param field  The {@link PsiField} to validate.
     */
    public void generateValidationForField(
            final PsiClass topLevelClass,
            final PsiMethod method,
            final PsiField field
    ) throws InvalidConstraintException {
        final List<PsiStatement> validationStatements = new ArrayList<>();

        final PsiType exceptionType = BuilderOptionUtility.exceptionType(topLevelClass);
        final List<BuilderConstraintOptionAndValue> builderConstraintOptionAndValues =
                ValidationUtility.getBuilderConstraintsForField(field);

        for (final BuilderConstraintOptionAndValue builderConstraintOptionAndValue : builderConstraintOptionAndValues) {
            final PsiStatement validationStatement = generateValidationStatement(
                    method,
                    field,
                    builderConstraintOptionAndValue,
                    exceptionType
            );
            validationStatements.add(validationStatement);
        }

        methodUtility.addStatements(method, validationStatements);
    }

    private PsiStatement generateValidationStatement(
            final PsiMethod method,
            final PsiField field,
            final BuilderConstraintOptionAndValue builderConstraintOptionAndValue,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        final BuilderConstraintOption builderConstraintOption = builderConstraintOptionAndValue.getOption();

        switch (builderConstraintOption) {
            case NOT_NULL:
                return generateNotNullStatement(method, field, exceptionType);
            case NOT_EMPTY:
                return generateNotEmptyStatement(method, field, exceptionType);
            case NOT_BLANK:
                return generateNotBlankStatement(method, field, exceptionType);
            case NO_NULL_KEYS:
                return generateNoNullKeysStatement(method, field, exceptionType);
            case NO_NULL_VALUES:
                return generateNoNullValuesStatement(method, field, exceptionType);
            case NOT_NEGATIVE:
                return generateNotNegativeStatement(method, field, exceptionType);
            case NOT_POSITIVE:
                return generateNotPositiveStatement(method, field, exceptionType);
            case NEGATIVE_ONLY:
                return generateNegativeOnlyStatement(method, field, exceptionType);
            case POSITIVE_ONLY:
                return generatePositiveOnlyStatement(method, field, exceptionType);
            case MIN_VALUE:
                return generateMinValueStatement(
                        method,
                        field,
                        (double) builderConstraintOptionAndValue.getValue(),
                        exceptionType
                );
            case MAX_VALUE:
                return generateMaxValueStatement(
                        method,
                        field,
                        (double) builderConstraintOptionAndValue.getValue(),
                        exceptionType
                );
            case CUSTOM:
                return generateCustomValidationStatement(
                        method,
                        builderConstraintOptionAndValue.getValue().toString()
                );
        }

        throw new InvalidConstraintException(field, builderConstraintOption.name());
    }

    private PsiStatement generateNotNullStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        if (field.getType() instanceof PsiPrimitiveType) {
            throw new InvalidConstraintException(field, "notNull");
        }

        final String criteria = String.format("%s == null", field.getName());
        final String assertStatement = generateAssertion(criteria, exceptionType, "notNull", field.getName());
        return psiElementFactory.createStatementFromText(assertStatement, method);
    }

    private PsiStatement generateNotEmptyStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        final PsiType type = field.getType();
        final String fieldName = field.getName();
        if (TypeUtility.isString(type, method) || TypeUtility.isCollection(type)) {
            final String criteria = String.format("%s.isEmpty()", fieldName);
            final String assertStatement = generateAssertion(criteria, exceptionType, "notEmpty", field.getName());
            return psiElementFactory.createStatementFromText(assertStatement, method);
        } else if (type instanceof PsiArrayType) {
            final String criteria = String.format("%s.length == 0", fieldName);
            final String assertStatement = generateAssertion(criteria, exceptionType, "notEmpty", field.getName());
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "notEmpty");
    }

    private PsiStatement generateNotBlankStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        final PsiType type = field.getType();
        if (type.equals(TypeUtils.getStringType(method))) {
            final String criteria = field.getName() + ".trim().isEmpty()";
            final String assertStatement = generateAssertion(criteria, exceptionType, "notBlank", field.getName());
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "notBlank");
    }

    private PsiStatement generateNoNullKeysStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        final PsiType fieldType = field.getType();

        if (TypeUtility.isMap(fieldType)) {
            final PsiType keyType = TypeUtility.getGenericKeyType(fieldType);

            final String forLoop = String.format(
                    "for (final %s key : %s.keySet())",
                    keyType.getCanonicalText(),
                    field.getName()
            );
            final String criteria = "key == null";
            final String assertStatement = generateAssertion(criteria, exceptionType, "noNullKeys", field.getName());
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        }

        throw new InvalidConstraintException(field, "noNullKeys");
    }

    private PsiStatement generateNoNullValuesStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        final PsiType fieldType = field.getType();
        final PsiType valueType = TypeUtility.getGenericValueType(fieldType);

        final String fieldName = field.getName();
        final String valueTypeName = valueType.getCanonicalText();

        if (TypeUtility.isMap(fieldType)) {
            final String forLoop = String.format("for (final %s value : %s.values())", valueTypeName, fieldName);
            final String criteria = "value == null";
            final String assertStatement = generateAssertion(criteria, exceptionType, "noNullValues", field.getName());
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        } else if (TypeUtility.isCollection(fieldType) || fieldType instanceof PsiArrayType) {
            final String forLoop = String.format("for (final %s value : %s)", valueTypeName, fieldName);
            final String criteria = "value == null";
            final String assertStatement = generateAssertion(criteria, exceptionType, "noNullValues", field.getName());
            return psiElementFactory.createStatementFromText(forLoop + "{" + assertStatement + "}", method);
        }

        throw new InvalidConstraintException(field, "noNullValues");
    }

    private PsiStatement generateNotNegativeStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s < 0", field.getName());
            final String assertStatement = generateAssertion(criteria, exceptionType, "notNegative", field.getName());
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "notNegative");
    }

    private PsiStatement generateNotPositiveStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s > 0", field.getName());
            final String assertStatement = generateAssertion(criteria, exceptionType, "notPositive", field.getName());
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "notPositive");
    }

    private PsiStatement generateNegativeOnlyStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s >= 0", field.getName());
            final String assertStatement = generateAssertion(criteria, exceptionType, "negativeOnly", field.getName());
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "negativeOnly");
    }

    private PsiStatement generatePositiveOnlyStatement(
            final PsiMethod method,
            final PsiField field,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s <= 0", field.getName());
            final String assertStatement = generateAssertion(criteria, exceptionType, "positiveOnly", field.getName());
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "positiveOnly");
    }

    private PsiStatement generateMinValueStatement(
            final PsiMethod method,
            final PsiField field,
            final double minValue,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s < %f", field.getName(), minValue);
            final String assertStatement = generateAssertion(
                    criteria,
                    exceptionType,
                    "minValue: " + minValue,
                    field.getName()
            );
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "minValue");
    }

    private PsiStatement generateMaxValueStatement(
            final PsiMethod method,
            final PsiField field,
            final double maxValue,
            final PsiType exceptionType
    ) throws InvalidConstraintException {
        if (TypeUtility.isNumeric(field.getType())) {
            final String criteria = String.format("%s > %f", field.getName(), maxValue);
            final String assertStatement = generateAssertion(criteria, exceptionType, "maxValue: " + maxValue, field.getName());
            return psiElementFactory.createStatementFromText(assertStatement, method);
        }

        throw new InvalidConstraintException(field, "maxValue");
    }

    private PsiStatement generateCustomValidationStatement(
            final PsiMethod method,
            final String validationText
    ) {
        return psiElementFactory.createStatementFromText(validationText, method);
    }

    private String generateAssertion(
            final String criteria,
            final PsiType exceptionType,
            final String constraint,
            final String fieldName
    ) {
        return String.format(
                "if (%s) { throw new %s(\"%s -> Constraint not met: %s\"); }",
                criteria,
                exceptionType.getCanonicalText(),
                fieldName,
                constraint
        );
    }
}

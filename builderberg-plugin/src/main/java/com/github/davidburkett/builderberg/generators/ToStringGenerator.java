package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.AnnotationUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;

public class ToStringGenerator {
    private static String QUOTE = "\"";
    private static String ESCAPED_QUOTE = "\\\"";

    private final PsiElementFactory psiElementFactory;
    private final JavadocGenerator javadocGenerator;
    private final MethodUtility methodUtility;

    public ToStringGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
        this.javadocGenerator = new JavadocGenerator(psiElementFactory);
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    public void generateToStringMethod(final PsiClass topLevelClass) {
        // Create toString method
        final PsiMethod toStringMethod = methodUtility.createPublicMethod("toString", TypeUtils.getStringType(topLevelClass));

        // Generate inheritDoc javadoc
        javadocGenerator.generateInheritDocJavadocForMethod(toStringMethod);

        // Add @Generated annotation
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, toStringMethod);

        // Add @Override annotation
        AnnotationUtility.addOverrideAnnotation(toStringMethod);

        addSerializedValues(topLevelClass, toStringMethod);

        // Add return statement
        addReturnStatement(topLevelClass, toStringMethod);

        topLevelClass.add(toStringMethod);
    }

    private void addSerializedValues(final PsiClass topLevelClass, final PsiMethod toStringMethod) {
        final PsiCodeBlock methodBody = toStringMethod.getBody();

        final PsiField[] fields = topLevelClass.getFields();
        for (final PsiField field : fields) {
            final String fieldName = field.getName();
            final PsiType fieldType = field.getType();

            // TODO: Need to handle maps.
            if (TypeUtility.isCollection(fieldType) || fieldType instanceof PsiArrayType) {
                final PsiType valueType = TypeUtility.getGenericValueType(fieldType);
                final String valueTypeName = valueType.getCanonicalText();

                final String serialized = "serialized_" + fieldName;
                final String precommaBoolean = "precomma_" + fieldName;

                final PsiStatement serializedStatement = psiElementFactory.createStatementFromText("String " + serialized + " = \"[\";", toStringMethod);
                final PsiStatement precommaStatement = psiElementFactory.createStatementFromText("boolean " + precommaBoolean + " = false;", toStringMethod);

                final String forLoop = String.format("for (final %s value : %s)", valueTypeName, fieldName);
                final String appendCommaStatement = serialized + " += " + precommaBoolean + " ? \",\" : \"\";";
                final String setPrecommaStatement = precommaBoolean + " = true;";

                final String value = getValueString(topLevelClass, field);
                final String appendValueStatement = serialized + " += " + value + ";";

                final PsiStatement closingStatement = psiElementFactory.createStatementFromText(serialized + " += \"]\";", toStringMethod);

                final PsiStatement forStatement = psiElementFactory.createStatementFromText(forLoop + "{" + appendCommaStatement + setPrecommaStatement + appendValueStatement + "}", toStringMethod);

                methodBody.add(serializedStatement);
                methodBody.add(precommaStatement);
                methodBody.add(forStatement);
                methodBody.add(closingStatement);
            }
        }
    }

    // TODO: This doesn't handle collections of collections.
    private String getValueString(final PsiClass topLevelClass, final PsiField field) {
        final PsiType fieldType = field.getType();

        final PsiType valueType = TypeUtility.getGenericValueType(fieldType);
        if (TypeUtility.isString(valueType, topLevelClass)) {
            return QUOTE + ESCAPED_QUOTE + QUOTE + " + value + " + QUOTE + ESCAPED_QUOTE + QUOTE;
        }

        return "value";
    }

    private void addReturnStatement(final PsiClass topLevelClass, final PsiMethod toStringMethod) {

        // Generate return statement
        final StringBuilder stringBuilder = new StringBuilder();

        final PsiField[] fields = topLevelClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                stringBuilder.append(",");
            }

            final PsiField field = fields[i];
            stringBuilder.append(createStringForField(topLevelClass, field));
        }

        final PsiCodeBlock methodBody = toStringMethod.getBody();
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText("return \"{" + stringBuilder.toString() + "}\";", toStringMethod);
        methodBody.add(returnStatement);
    }

    /*
     * The goal is to have the toString generate: "fieldName": fieldValue
     * In order to do that, the generated toString method must be escaped as: "\"fieldName\":" + fieldName
     * Since we are generating that generated code, it must be escaped once more, which explains the complexity of this method.
     */
    private String createStringForField(final PsiClass topLevelClass, final PsiField field) {
        final String fieldName = field.getName();

        final String fieldValue = getFieldValue(field);

        final String keyString =  ESCAPED_QUOTE + fieldName + ESCAPED_QUOTE + ":";
        final String valueString = QUOTE + " + " + fieldValue + " + " + QUOTE;

        if (shouldSurroundInQuotes(topLevelClass, field)) {
            return keyString + ESCAPED_QUOTE + valueString + ESCAPED_QUOTE;
        } else {
            return keyString + valueString;
        }
    }

    // TODO: Handle Map fields.
    private String getFieldValue(final PsiField field) {
        final String fieldName = field.getName();

        if (TypeUtility.isCollection(field.getType()) || field.getType() instanceof PsiArrayType) {
            return "serialized_" + fieldName;
        }

        return fieldName;
    }

    private boolean shouldSurroundInQuotes(final PsiClass topLevelClass, final PsiField field) {
        return TypeUtility.isString(field.getType(), topLevelClass);
    }
}

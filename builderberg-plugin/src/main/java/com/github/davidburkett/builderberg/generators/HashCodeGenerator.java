package com.github.davidburkett.builderberg.generators;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

public class HashCodeGenerator {
    private final PsiElementFactory psiElementFactory;

    public HashCodeGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    /**
     * Implements a hashCode method using the hash function described by Josh Bloch in "Effective Java".
     * @param topLevelClass The class to generate the hashCode method for.
     */
    public void generateHashCodeMethod(final PsiClass topLevelClass) {
        // Create hashCode method
        final PsiMethod hashCodeMethod =
                psiElementFactory.createMethod("hashCode", PsiType.INT);

        // TODO: Generate Javadoc

        // Add @Override annotation
        hashCodeMethod.getModifierList().addAnnotation("Override");

        // Add return statement
        final PsiCodeBlock methodBody = hashCodeMethod.getBody();

        final PsiStatement resultInitializationStatement =
                psiElementFactory.createStatementFromText("int result = 17;", hashCodeMethod);
        methodBody.add(resultInitializationStatement);

        createFieldStatements(topLevelClass, hashCodeMethod, methodBody);

        final String returnString = "return result;";
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText(returnString, hashCodeMethod);
        methodBody.add(returnStatement);

        topLevelClass.add(hashCodeMethod);
    }

    private void createFieldStatements(final PsiClass topLevelClass, final PsiMethod hashCodeMethod, final PsiCodeBlock methodBody) {
        final PsiField[] fields = topLevelClass.getFields();
        for (final PsiField field : fields) {
            final String fieldValue = getValueForField(field);
            final String statement = "result = 31 * result + " + fieldValue + ";";

            final PsiStatement fieldStatement = psiElementFactory.createStatementFromText(statement, hashCodeMethod);
            methodBody.add(fieldStatement);
        }
    }

    private String getValueForField(final PsiField field) {
        final String fieldName = field.getName();

        final PsiType type = field.getType();
        if (type instanceof PsiPrimitiveType) {
            if (type == PsiType.BOOLEAN) {
                return "(" + fieldName + " ? 1 : 0)";
            } else if (type == PsiType.BYTE || type == PsiType.CHAR || type == PsiType.SHORT || type == PsiType.INT) {
                return "(int)" + fieldName;
            } else if (type == PsiType.LONG) {
                return "(int) (" + fieldName + " ^ (" + fieldName + " >>> 32))";
            } else if (type == PsiType.FLOAT) {
                return "Float.floatToIntBits(" + fieldName + ")";
            } else if (type == PsiType.DOUBLE) {
                final String doubleToLong = "Double.doubleToLongBits(" + fieldName + ")";
                return "(int) (" + doubleToLong + " ^ (" + doubleToLong + " >>> 32))";
            }
        } else {
            return fieldName + ".hashCode()";
        }

        return "0";
    }
}

package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.AnnotationUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.intellij.psi.*;

public class HashCodeGenerator {
    private final PsiElementFactory psiElementFactory;
    private final JavadocGenerator javadocGenerator;
    private final MethodUtility methodUtility;

    public HashCodeGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
        this.javadocGenerator = new JavadocGenerator(psiElementFactory);
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    /**
     * Implements a hashCode method using the hash function described by Josh Bloch in "Effective Java".
     * @param topLevelClass The class to generate the hashCode method for.
     */
    public void generateHashCodeMethod(final PsiClass topLevelClass) {
        // Create hashCode method
        final PsiMethod hashCodeMethod = methodUtility.createPublicMethod("hashCode", PsiType.INT);

        // Generate inheritDoc javadoc
        javadocGenerator.generateInheritDocJavadocForMethod(hashCodeMethod);

        // Add @Generated annotation
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, hashCodeMethod);

        // Add @Override annotation
        AnnotationUtility.addOverrideAnnotation(hashCodeMethod);

        methodUtility.addStatement(hashCodeMethod, "int result = 17;");

        createFieldStatements(topLevelClass, hashCodeMethod);

        // Add return statement
        methodUtility.addReturnStatement(hashCodeMethod, "result");

        topLevelClass.add(hashCodeMethod);
    }

    private void createFieldStatements(final PsiClass topLevelClass, final PsiMethod hashCodeMethod) {
        final PsiField[] fields = topLevelClass.getFields();
        for (final PsiField field : fields) {
            final String fieldValue = getValueForField(field);
            final String statement = "result = 31 * result + " + fieldValue + ";";
            methodUtility.addStatement(hashCodeMethod, statement);
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
            return "java.util.Objects.hashCode(" + fieldName + ")";
        }

        return "0";
    }
}

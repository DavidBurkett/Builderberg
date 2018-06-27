package com.github.davidburkett.builderberg.generators;

import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;

public class ToStringGenerator {
    private final PsiElementFactory psiElementFactory;
    private final JavadocGenerator javadocGenerator;

    public ToStringGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
        this.javadocGenerator = new JavadocGenerator(psiElementFactory);
    }

    public void generateToStringMethod(final PsiClass topLevelClass) {
        // Create toString method
        final PsiMethod toStringMethod =
                psiElementFactory.createMethod("toString", TypeUtils.getStringType(topLevelClass));

        // Generate inheritDoc javadoc
        javadocGenerator.generateInheritDocJavadocForMethod(toStringMethod);

        // Add @Override annotation
        toStringMethod.getModifierList().addAnnotation("Override");

        // Add return statement
        final PsiCodeBlock methodBody = toStringMethod.getBody();
        final String returnString = createReturnStatement(topLevelClass);
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText(returnString, toStringMethod);
        methodBody.add(returnStatement);

        topLevelClass.add(toStringMethod);
    }

    private String createReturnStatement(final PsiClass topLevelClass) {
        // Generate string value
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"{");
        final PsiField[] fields = topLevelClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                stringBuilder.append(",");
            }

            final PsiField field = fields[i];
            stringBuilder.append(createStringForField(field));
        }
        stringBuilder.append("}\"");

        return "return " + stringBuilder.toString() + ";";
    }

    private String createStringForField(final PsiField field) {
        final String fieldName = field.getName();

        return "\\\"" + fieldName + "\\\": \\\"\" + " + fieldName + " + \"\\\"";
        //stringBuilder.append("\\\"").append(field.getName()).append("\\\": \\\"\" + ").append(field.getName()).append(" + \"\\\"");
    }
}

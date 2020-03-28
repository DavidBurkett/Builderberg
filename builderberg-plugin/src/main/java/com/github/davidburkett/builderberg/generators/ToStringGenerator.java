package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.AnnotationUtility;
import com.github.davidburkett.builderberg.utilities.BuilderOptionUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;

public class ToStringGenerator {
    private final PsiElementFactory psiElementFactory;
    private final MethodUtility methodUtility;

    public ToStringGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    public void generateToStringMethod(final PsiClass topLevelClass) {
        // Create toString method
        final PsiMethod toStringMethod = methodUtility.createPublicMethod("toString", TypeUtils.getStringType(topLevelClass));

        // Generate inheritDoc javadoc
        methodUtility.addJavadoc(toStringMethod, ImmutableList.of("{@inheritDoc}"));

        // Add @Generated annotation
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, toStringMethod);

        // Add @Override annotation
        AnnotationUtility.addOverrideAnnotation(toStringMethod);

        // Add return statement
        addReturnStatement(topLevelClass, toStringMethod);

        topLevelClass.add(toStringMethod);
    }

    private void addReturnStatement(final PsiClass topLevelClass, final PsiMethod toStringMethod) {
        final boolean excludeStaticFields = BuilderOptionUtility.excludeStaticFields(topLevelClass);

        // Generate string value
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"{");
        final PsiField[] fields = topLevelClass.getFields();
        boolean includedField = false;
        for (int i = 0; i < fields.length; i++) {
            final PsiField field = fields[i];

            if (excludeStaticFields && field.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }

            if (includedField) {
                stringBuilder.append(",");
            } else {
                includedField = true;
            }

            stringBuilder.append(createStringForField(field));
        }
        stringBuilder.append("}\"");

        methodUtility.addReturnStatement(toStringMethod, stringBuilder.toString());
    }

    private String createStringForField(final PsiField field) {
        final String fieldName = field.getName();
        String fieldValue = field.getName();

        if (field.getType() instanceof PsiArrayType) {
            fieldValue = String.format("java.util.Arrays.toString(%s)", fieldName);
        }

        return String.format("'%s': '\" + %s + \"'", fieldName, fieldValue);
    }
}

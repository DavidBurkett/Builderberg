package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.*;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetterGenerator {
    private final PsiElementFactory psiElementFactory;
    private final MethodUtility methodUtility;

    public GetterGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    public void generateGetters(final PsiClass topLevelClass) {
        final boolean excludeStaticFields = BuilderOptionUtility.excludeStaticFields(topLevelClass);

        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            if (excludeStaticFields && field.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }

            final String getterMethodName = MethodNameUtility.getGetterName(field);
            generateGetter(topLevelClass, field, getterMethodName);

            if (TypeUtility.isPrimitiveBoolean(field.getType())) {
                final String isMethodName = MethodNameUtility.getIsMethodName(field);
                generateGetter(topLevelClass, field, isMethodName);
            }
        }
    }

    private void generateGetter(final PsiClass topLevelClass, final PsiField field, final String getterMethodName) {
        final PsiMethod getter = methodUtility.createPublicMethod(getterMethodName, field.getType());
        generateCommentForGetterMethod(getter, field);

        methodUtility.addReturnStatement(getter, field.getName());
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, getter);

        topLevelClass.add(getter);
    }

    public void generateCommentForGetterMethod(final PsiMethod getterMethod, final PsiField field) {
        final List<String> javadocLines = new ArrayList<>();

        final String fieldJavaDoc = JavadocUtil.getCommentText(field);
        final String returnJavadoc = fieldJavaDoc != null ? fieldJavaDoc : field.getName();
        javadocLines.add("@return " + returnJavadoc);

        final List<String> validationComments = ValidationJavadocGenerator.generateValidationComments(field, "Guaranteed to");
        javadocLines.addAll(validationComments);

        methodUtility.addJavadoc(getterMethod, javadocLines);
    }
}

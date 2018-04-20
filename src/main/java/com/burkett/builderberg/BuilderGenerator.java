package com.burkett.builderberg;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.Arrays;
import java.util.List;

public class BuilderGenerator {
    private final PsiElementFactory psiElementFactory;

    public BuilderGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    public void generateInnerBuilder(final PsiClass topLevelClass) {
        final BuilderClassFactory builderClassFactory = new BuilderClassFactory(psiElementFactory);
        final PsiClass builderClass = builderClassFactory.createBuilderClass(topLevelClass);

        makeFieldsFinal(topLevelClass);
        generateConstructor(topLevelClass, builderClass);
        generateGetters(topLevelClass);

        generateToString(topLevelClass);
        // TODO: Generate hashCode
        // TODO: Generate equals
        // TODO: Generate clone

        topLevelClass.add(builderClass);
    }

    private void makeFieldsFinal(final PsiClass topLevelClass) {
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            PsiUtil.setModifierProperty(field, PsiModifier.FINAL, true);
        }
    }

    private void generateConstructor(final PsiClass topLevelClass, final PsiClass builderClass) {
        final PsiMethod constructor = psiElementFactory.createConstructor();
        PsiUtil.setModifierProperty(constructor, PsiModifier.PRIVATE, true);

        final PsiType builderType = TypeUtils.getType(builderClass);
        final PsiParameter parameter = psiElementFactory.createParameter("builder", builderType);
        PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);
        constructor.getParameterList().add(parameter);

        final PsiCodeBlock body = constructor.getBody();

        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            // Assign value
            final String fieldName = field.getName();
            final PsiStatement assignStatement =
                    psiElementFactory.createStatementFromText("this." + fieldName + " = builder." + fieldName + ";", constructor);
            body.add(assignStatement);
        }

        topLevelClass.add(constructor);
    }

    private void generateGetters(final PsiClass topLevelClass) {
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            final String fieldName = field.getName();
            final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            final PsiMethod getter = psiElementFactory.createMethod("get" + capitalizedFieldName, field.getType());

            final PsiCodeBlock body = getter.getBody();
            final PsiStatement returnStatement =
                    psiElementFactory.createStatementFromText("return " + fieldName + ";", getter);
            body.add(returnStatement);

            final CommentUtility commentUtility = new CommentUtility(psiElementFactory);
            commentUtility.generateCommentForGetterMethod(getter, field);

            topLevelClass.add(getter);
        }
    }

    private void generateToString(final PsiClass topLevelClass) {
        // Generate string value
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"{");
        final PsiField[] fields = topLevelClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                stringBuilder.append(",");
            }

            final PsiField field = fields[i];
            // TODO: This only works for primitive fields. Need Collection and Object support.
            stringBuilder.append("\\\"").append(field.getName()).append("\\\": \\\"\" + ").append(field.getName()).append(" + \"\\\"");
        }
        stringBuilder.append("}\"");

        final String toStringText = stringBuilder.toString();

        // Create toString method
        final PsiMethod toStringMethod =
                psiElementFactory.createMethod("toString", TypeUtils.getStringType(topLevelClass));

        // TODO: Generate Javadoc

        // Add @Override annotation
        final PsiAnnotation overrideAnnotation =
                psiElementFactory.createAnnotationFromText("@Override", topLevelClass);
        toStringMethod.addBefore(overrideAnnotation, toStringMethod.getFirstChild());

        // Add return statement
        final PsiCodeBlock methodBody = toStringMethod.getBody();
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText("return " + toStringText + ";", toStringMethod);
        methodBody.add(returnStatement);

        topLevelClass.add(toStringMethod);
    }
}

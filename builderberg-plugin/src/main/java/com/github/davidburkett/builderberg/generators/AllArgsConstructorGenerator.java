package com.github.davidburkett.builderberg.generators;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;

public class AllArgsConstructorGenerator {
    private static final String JSON_CREATOR = "com.fasterxml.jackson.annotation.JsonCreator";
    private static final String JSON_PROPERTY = "com.fasterxml.jackson.annotation.JsonProperty";

    private final PsiElementFactory psiElementFactory;

    public AllArgsConstructorGenerator(final Project project) {
        this.psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
    }

    public void generateAllArgsConstructor(final PsiClass topLevelClass, final PsiClass builderClass, final boolean jacksonSupport) {
        final PsiMethod constructor = psiElementFactory.createConstructor();
        if (jacksonSupport) {
            constructor.getModifierList().addAnnotation(JSON_CREATOR);
        }

        final PsiField[] fields = builderClass.getFields();

        addParameters(constructor, fields, jacksonSupport);
        generateStatement(constructor, fields);

        topLevelClass.add(constructor);
    }

    private void addParameters(final PsiMethod constructor, final PsiField[] fields, final boolean jacksonSupport) {
        for (final PsiField field : fields) {
            final PsiParameter parameter = psiElementFactory.createParameter(field.getName(), field.getType());
            PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);

            if (jacksonSupport) {
                parameter.getModifierList().addAnnotation(JSON_PROPERTY);
            }

            constructor.getParameterList().add(parameter);
        }
    }

    private void generateStatement(final PsiMethod constructor, final PsiField[] fields) {
        // TODO: Handle generics
        final StringBuilder statementText = new StringBuilder("this(builder()");
        for (final PsiField field : fields) {
            final String fieldName = field.getName();

            final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            statementText.append(".with" + capitalizedFieldName + "(" + fieldName + ")");
        }

        statementText.append(");");

        final PsiStatement constructStatement = psiElementFactory.createStatementFromText(statementText.toString(), constructor);
        constructor.getBody().add(constructStatement);
    }
}

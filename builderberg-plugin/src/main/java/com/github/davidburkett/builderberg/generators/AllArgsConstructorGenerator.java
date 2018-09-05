package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.AnnotationUtility;
import com.github.davidburkett.builderberg.utilities.QualifyingFieldsFinder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;

import java.util.List;
import java.util.Optional;

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

        final List<PsiField> fields = QualifyingFieldsFinder.findQualifyingFields(topLevelClass);

        addParameters(constructor, fields, jacksonSupport);
        generateStatement(constructor, fields);

        topLevelClass.add(constructor);
    }

    private void addParameters(final PsiMethod constructor, final List<PsiField> fields, final boolean jacksonSupport) {
        for (final PsiField field : fields) {
            final PsiParameter parameter = psiElementFactory.createParameter(field.getName(), field.getType());
            PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);

            if (jacksonSupport) {
                final String propertyName = getJsonPropertyName(field);
                parameter.getModifierList().addAnnotation(String.format("%s(%s)", JSON_PROPERTY, propertyName));
            }

            constructor.getParameterList().add(parameter);
        }
    }

    private String getJsonPropertyName(final PsiField field) {
        final Optional<PsiAnnotation> jsonPropertyAnnotation = AnnotationUtility.getJsonPropertyAnnotation(field);
        return jsonPropertyAnnotation
                .map(a -> a.findAttributeValue("value").getText())
                .orElse("\"" + field.getName() + "\"");
    }

    private void generateStatement(final PsiMethod constructor, final List<PsiField> fields) {
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

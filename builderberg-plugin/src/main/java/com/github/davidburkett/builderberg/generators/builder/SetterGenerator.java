package com.github.davidburkett.builderberg.generators.builder;

import com.github.davidburkett.builderberg.exceptions.InvalidConstraintException;
import com.github.davidburkett.builderberg.generators.ValidationGenerator;
import com.github.davidburkett.builderberg.generators.ValidationJavadocGenerator;
import com.github.davidburkett.builderberg.utilities.JavadocUtil;
import com.github.davidburkett.builderberg.utilities.MethodNameUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class SetterGenerator {
    private final MethodUtility methodUtility;
    private final ValidationGenerator validationGenerator;

    public SetterGenerator(final PsiElementFactory psiElementFactory) {
        this.methodUtility = new MethodUtility(psiElementFactory);
        this.validationGenerator = new ValidationGenerator(psiElementFactory);
    }

    public void generateSetters(final PsiClass topLevelClass, final PsiClass builderClass, final List<PsiField> fields) throws InvalidConstraintException {
        final PsiType builderType = TypeUtils.getType(builderClass);

        for (final PsiField field : fields) {
            final String fieldName = field.getName();

            // Create method
            final String setterName = MethodNameUtility.getSetterName(field);
            final PsiMethod withMethod = methodUtility.createPublicMethod(setterName, builderType);

            // Add parameter
            final PsiType parameterType = getParameterType(field);
            methodUtility.addParameter(withMethod, fieldName, parameterType);

            // Add javadoc
            generateCommentForSetterMethod(withMethod, field);

            // Validate input
            validationGenerator.generateValidationForField(topLevelClass, withMethod, field);

            // Assign value
            methodUtility.addStatement(withMethod, String.format("this.%s = %s;", fieldName, fieldName));

            // Return builder to allow method chaining
            methodUtility.addReturnStatement(withMethod, "this");

            builderClass.add(withMethod);
        }
    }

    private PsiType getParameterType(final PsiField field) {
        final PsiType parameterType = field.getType();
        if (parameterType instanceof PsiArrayType) {
            return new PsiEllipsisType(parameterType.getDeepComponentType());
        }

        return parameterType;
    }

    private void generateCommentForSetterMethod(final PsiMethod withMethod, final PsiField field) {
        final List<String> javadocLines = new ArrayList<>();

        final String fieldCommentText = JavadocUtil.getCommentText(field);
        final String fieldJavaDoc = fieldCommentText != null ? fieldCommentText : "";
        javadocLines.add("@param " + field.getName() + " " + fieldJavaDoc);

        final List<String> validationComments = ValidationJavadocGenerator.generateValidationComments(field, "Must");
        javadocLines.addAll(validationComments);

        methodUtility.addJavadoc(withMethod, javadocLines);
    }
}

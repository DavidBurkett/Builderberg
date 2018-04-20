package com.burkett.builderberg;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.Arrays;
import java.util.List;

public class BuilderClassFactory {
    private static final String BUILDER_CLASS = "Builder";

    private final PsiElementFactory psiElementFactory;

    public BuilderClassFactory(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    /**
     * Creates the inner builder class for the given {@link PsiClass topLevelClass}, but does not add it to the class.
     */
    public PsiClass createBuilderClass(final PsiClass topLevelClass) {
        final PsiClass builderClass = psiElementFactory.createClass(BUILDER_CLASS);

        PsiUtil.setModifierProperty(builderClass, PsiModifier.PUBLIC, true);
        PsiUtil.setModifierProperty(builderClass, PsiModifier.STATIC, true);
        PsiUtil.setModifierProperty(builderClass, PsiModifier.FINAL, true);

        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());

        generateFields(builderClass, fields);
        generateConstructor(builderClass);
        generateCreateMethod(builderClass);
        generateWithSetters(builderClass, fields);
        generateBuildMethod(topLevelClass, builderClass);

        return builderClass;
    }

    private void generateFields(final PsiClass builderClass, final List<PsiField> fields) {
        for (final PsiField field : fields) {
            final PsiField builderField = psiElementFactory.createField(field.getName(), field.getType());
            builderClass.add(builderField);
        }
    }

    private void generateWithSetters(final PsiClass builderClass, final List<PsiField> fields) {
        final PsiType builderType = TypeUtils.getType(builderClass);

        for (final PsiField field : fields) {
            final String fieldName = field.getName();
            final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            final PsiMethod withMethod = psiElementFactory.createMethod("with" + capitalizedFieldName, builderType);
            PsiUtil.setModifierProperty(withMethod, PsiModifier.PUBLIC, true);

            // Add parameter
            final PsiParameter parameter = psiElementFactory.createParameter(fieldName, field.getType());
            PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);
            withMethod.getParameterList().add(parameter);

            // TODO: Apply validation
            // TODO: Add method comment

            final PsiCodeBlock body = withMethod.getBody();

            // Assign value
            final PsiStatement assignStatement =
                    psiElementFactory.createStatementFromText("this." + fieldName + " = " + fieldName + ";", withMethod);
            body.add(assignStatement);

            // Return builder to allow method chaining
            final PsiStatement returnStatment = psiElementFactory.createStatementFromText("return this;", withMethod);
            body.add(returnStatment);

            builderClass.add(withMethod);
        }
    }

    private void generateCreateMethod(final PsiClass builderClass) {
        final PsiType builderType = TypeUtils.getType(builderClass);
        final PsiMethod createMethod = psiElementFactory.createMethod("create", builderType);
        PsiUtil.setModifierProperty(createMethod, PsiModifier.PUBLIC, true);
        PsiUtil.setModifierProperty(createMethod, PsiModifier.STATIC, true);

        final PsiCodeBlock body = createMethod.getBody();
        final String className = builderClass.getName();
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText("return new " + className + "();", createMethod);
        body.add(returnStatement);

        builderClass.add(createMethod);
    }

    private void generateBuildMethod(final PsiClass topLevelClass, final PsiClass builderClass) {
        final PsiType topLevelType = TypeUtils.getType(topLevelClass); // TODO: Figure out why this is fully qualified
        final PsiMethod buildMethod = psiElementFactory.createMethod("build", topLevelType);

        final PsiCodeBlock body = buildMethod.getBody();
        final String className = topLevelClass.getName();
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText("return new " + className + "(this);", buildMethod);
        body.add(returnStatement);

        builderClass.add(buildMethod);
    }

    private void generateConstructor(final PsiClass builderClass) {
        final PsiMethod constructor = psiElementFactory.createConstructor();
        PsiUtil.setModifierProperty(constructor, PsiModifier.PRIVATE, true);

        builderClass.add(constructor);
    }
}

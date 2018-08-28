package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.ClassFactory;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;
import org.fest.util.Lists;

import java.util.Arrays;
import java.util.List;

public class BuilderClassGenerator {
    private static final String BUILDER_CLASS = "Builder";

    private final Project project;
    private final ClassFactory classFactory;
    private final PsiElementFactory psiElementFactory;

    public BuilderClassGenerator(final Project project, final PsiElementFactory psiElementFactory) {
        this.project = project;
        this.classFactory = new ClassFactory(psiElementFactory);
        this.psiElementFactory = psiElementFactory;
    }

    /**
     * Creates the inner builder class for the given {@link PsiClass topLevelClass}, but does not add it to the class.
     * @param topLevelClass The non-null top-level class.
     * @return The generated inner builder class.
     */
    public PsiClass createBuilderClass(final PsiClass topLevelClass) {
        final PsiClass builderClass =
                classFactory.createClass(BUILDER_CLASS, topLevelClass.getTypeParameters(), ImmutableList.of(PsiModifier.PUBLIC, PsiModifier.STATIC, PsiModifier.FINAL));

        final List<PsiField> fields = getQualifyingFields(topLevelClass);

        generateFields(builderClass, fields);
        generateConstructor(builderClass);
        generateCreateMethod(builderClass);
        generateWithSetters(topLevelClass, builderClass, fields);
        generateBuildMethod(topLevelClass, builderClass);
        generateValidateMethod(topLevelClass, builderClass, fields);

        return builderClass;
    }

    private List<PsiField> getQualifyingFields(final PsiClass topLevelClass) {
        final List<PsiField> qualifyingFields = Lists.newArrayList();

        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (final PsiField field : fields) {

            // Skip final fields that are already initialized.
            if (field.hasModifierProperty(PsiModifier.FINAL)) {
                if (field.getInitializer() != null) {
                    continue;
                }
            }

            // Skip static fields.
            if (field.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }

            qualifyingFields.add(field);
        }

        return qualifyingFields;
    }

    private void generateFields(final PsiClass builderClass, final List<PsiField> fields) {
        for (final PsiField field : fields) {
            final PsiField builderField = psiElementFactory.createField(field.getName(), field.getType());
            builderClass.add(builderField);
        }
    }

    private void generateWithSetters(final PsiClass topLevelClass, final PsiClass builderClass, final List<PsiField> fields) {
        final PsiType builderType = TypeUtils.getType(builderClass);

        for (final PsiField field : fields) {
            final String fieldName = field.getName();
            final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            final PsiMethod withMethod = psiElementFactory.createMethod("with" + capitalizedFieldName, builderType);
            PsiUtil.setModifierProperty(withMethod, PsiModifier.PUBLIC, true);

            // Add parameter
            final PsiType psiType = getSanitizedType(field.getType());
            final PsiParameter parameter = psiElementFactory.createParameter(fieldName, psiType);
            PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);
            withMethod.getParameterList().add(parameter);

            // TODO: Add method comment

            final PsiCodeBlock body = withMethod.getBody();

            // Validate input
            final ValidationGenerator validationGenerator = new ValidationGenerator(project, psiElementFactory);
            final List<PsiStatement> validationStatments = validationGenerator.generateValidationForField(topLevelClass, withMethod, field);
            for (final PsiStatement validationStatement : validationStatments) {
                body.add(validationStatement);
            }

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

    private PsiType getSanitizedType(final PsiType psiType) {
        if (psiType instanceof PsiArrayType) {
            return new PsiEllipsisType(psiType.getDeepComponentType());
        }

        return psiType;
    }

    private void generateCreateMethod(final PsiClass builderClass) {
        final PsiType builderType = TypeUtils.getType(builderClass);
        final PsiMethod createMethod = psiElementFactory.createMethod("create", builderType);
        PsiUtil.setModifierProperty(createMethod, PsiModifier.PUBLIC, true);
        PsiUtil.setModifierProperty(createMethod, PsiModifier.STATIC, true);

        final PsiCodeBlock body = createMethod.getBody();
        final String builderClassName = builderClass.getName();
        final String generics = builderClass.hasTypeParameters() ? "<>" : "";
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText("return new " + builderClassName + generics + "();", createMethod);
        body.add(returnStatement);

        builderClass.add(createMethod);
    }

    private void generateBuildMethod(final PsiClass topLevelClass, final PsiClass builderClass) {
        final PsiType topLevelType = TypeUtility.getTypeWithGenerics(topLevelClass, topLevelClass.getTypeParameters());
        final PsiMethod buildMethod = psiElementFactory.createMethod("build", topLevelType);

        final PsiCodeBlock body = buildMethod.getBody();
        final String className = topLevelClass.getName();
        final String generics = topLevelClass.hasTypeParameters() ? "<>" : "";
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText("return new " + className + generics + "(this);", buildMethod);
        body.add(returnStatement);

        builderClass.add(buildMethod);
    }

    private void generateConstructor(final PsiClass builderClass) {
        final PsiMethod constructor = psiElementFactory.createConstructor();
        PsiUtil.setModifierProperty(constructor, PsiModifier.PRIVATE, true);

        builderClass.add(constructor);
    }

    private void generateValidateMethod(final PsiClass topLevelClass, final PsiClass builderClass, final List<PsiField> fields) {
        final ValidationGenerator validationGenerator = new ValidationGenerator(project, psiElementFactory);

        final PsiMethod validateMethod = psiElementFactory.createMethod("validate", PsiType.VOID);
        PsiUtil.setModifierProperty(validateMethod, PsiModifier.PRIVATE, true);

        final PsiCodeBlock body = validateMethod.getBody();

        for (PsiField field : fields) {
            // Validate input
            final List<PsiStatement> validationStatments = validationGenerator.generateValidationForField(topLevelClass, validateMethod, field);
            for (final PsiStatement validationStatement : validationStatments) {
                body.add(validationStatement);
            }
        }

        builderClass.add(validateMethod);
    }
}

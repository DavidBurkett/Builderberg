package com.github.davidburkett.builderberg.generators.builder;

import com.github.davidburkett.builderberg.exceptions.InvalidConstraintException;
import com.github.davidburkett.builderberg.generators.ValidationGenerator;
import com.github.davidburkett.builderberg.utilities.ClassFactory;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.github.davidburkett.builderberg.utilities.QualifyingFieldsFinder;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.List;

public class BuilderClassGenerator {
    private static final String BUILDER_CLASS = "Builder";

    private final ClassFactory classFactory;
    private final PsiElementFactory psiElementFactory;
    private final MethodUtility methodUtility;
    private final ValidationGenerator validationGenerator;
    private final SetterGenerator setterGenerator;

    public BuilderClassGenerator(final PsiElementFactory psiElementFactory) {
        this.classFactory = new ClassFactory(psiElementFactory);
        this.psiElementFactory = psiElementFactory;
        this.methodUtility = new MethodUtility(psiElementFactory);
        this.validationGenerator = new ValidationGenerator(psiElementFactory);
        this.setterGenerator = new SetterGenerator(psiElementFactory);
    }

    /**
     * Creates the inner builder class for the given {@link PsiClass topLevelClass}, but does not add it to the class.
     * @param topLevelClass The non-null top-level class.
     * @return The generated inner builder class.
     */
    public PsiClass createBuilderClass(final PsiClass topLevelClass) throws InvalidConstraintException {
        final PsiClass builderClass =
                classFactory.createClass(BUILDER_CLASS, topLevelClass.getTypeParameters(), ImmutableList.of(PsiModifier.PUBLIC, PsiModifier.STATIC, PsiModifier.FINAL));

        final List<PsiField> fields = QualifyingFieldsFinder.findQualifyingFields(topLevelClass);

        generateFields(builderClass, fields);
        generateConstructor(builderClass);
        generateCreateMethod(builderClass);
        setterGenerator.generateSetters(topLevelClass, builderClass, fields);
        generateBuildMethod(topLevelClass, builderClass);
        generateValidateMethod(topLevelClass, builderClass, fields);

        return builderClass;
    }

    private void generateFields(final PsiClass builderClass, final List<PsiField> fields) {
        for (final PsiField field : fields) {
            final PsiField builderField = psiElementFactory.createField(field.getName(), field.getType());
            builderClass.add(builderField);
        }
    }

    private void generateCreateMethod(final PsiClass builderClass) {
        final PsiType builderType = TypeUtils.getType(builderClass);
        final PsiMethod createMethod = methodUtility.createPublicStaticMethod("create", builderType);

        final String builderClassName = builderClass.getName();
        final String generics = builderClass.hasTypeParameters() ? "<>" : "";
        methodUtility.addStatement(createMethod, "return new " + builderClassName + generics + "();");

        builderClass.add(createMethod);
    }

    private void generateBuildMethod(final PsiClass topLevelClass, final PsiClass builderClass) {
        final PsiType topLevelType = TypeUtility.getTypeWithGenerics(topLevelClass, topLevelClass.getTypeParameters());
        final PsiMethod buildMethod = methodUtility.createPublicMethod("build", topLevelType);

        final String className = topLevelClass.getName();
        final String generics = topLevelClass.hasTypeParameters() ? "<>" : "";
        methodUtility.addStatement(buildMethod, "return new " + className + generics + "(this);");

        builderClass.add(buildMethod);
    }

    private void generateConstructor(final PsiClass builderClass) {
        final PsiMethod constructor = psiElementFactory.createConstructor();
        PsiUtil.setModifierProperty(constructor, PsiModifier.PRIVATE, true);

        builderClass.add(constructor);
    }

    private void generateValidateMethod(final PsiClass topLevelClass, final PsiClass builderClass, final List<PsiField> fields) throws InvalidConstraintException {
        final PsiMethod validateMethod = methodUtility.createPrivateMethod("validate", PsiType.VOID);

        for (PsiField field : fields) {
            // Validate input
            validationGenerator.generateValidationForField(topLevelClass, validateMethod, field);
        }

        builderClass.add(validateMethod);
    }
}

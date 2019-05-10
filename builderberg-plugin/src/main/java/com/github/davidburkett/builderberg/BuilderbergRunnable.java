package com.github.davidburkett.builderberg;

import com.github.davidburkett.builderberg.enums.CollectionType;
import com.github.davidburkett.builderberg.exceptions.InvalidConstraintException;
import com.github.davidburkett.builderberg.generators.*;
import com.github.davidburkett.builderberg.generators.builder.BuilderClassGenerator;
import com.github.davidburkett.builderberg.generators.builder.ParameterGenerator;
import com.github.davidburkett.builderberg.utilities.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.Optional;

/**
 * Asynchronous handler responsible for making the changes to an existing class to provide it a builder and all supporting functionality.
 */
public class BuilderbergRunnable implements Runnable {
    private final Project project;
    private final PsiClass topLevelClass;
    private final PsiElementFactory psiElementFactory;
    private final ToStringGenerator toStringGenerator;
    private final HashCodeGenerator hashCodeGenerator;
    private final EqualsGenerator equalsGenerator;
    private final BuilderClassGenerator builderClassGenerator;
    private final AllArgsConstructorGenerator allArgsConstructorGenerator;
    private final CloneGenerator cloneGenerator;
    private final MethodUtility methodUtility;
    private final GetterGenerator getterGenerator;
    private final ParameterGenerator parameterGenerator;

    public BuilderbergRunnable(final Project project, final PsiClass topLevelClass) {
        this.project = project;
        this.topLevelClass = topLevelClass;
        this.psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
        this.toStringGenerator = new ToStringGenerator(psiElementFactory);
        this.hashCodeGenerator = new HashCodeGenerator(psiElementFactory);
        this.equalsGenerator = new EqualsGenerator(project, psiElementFactory);
        this.builderClassGenerator = new BuilderClassGenerator(psiElementFactory);
        this.allArgsConstructorGenerator = new AllArgsConstructorGenerator(project);
        this.cloneGenerator = new CloneGenerator(project);
        this.methodUtility = new MethodUtility(psiElementFactory);
        this.getterGenerator = new GetterGenerator(psiElementFactory);
        this.parameterGenerator = new ParameterGenerator(psiElementFactory);
    }

    @Override
    public void run() {
        try {
            // Clean up existing builder remnants, and make all class fields final
            prepareClassForBuilder(topLevelClass);

            // Create the inner-builder class
            final PsiClass builderClass = builderClassGenerator.createBuilderClass(topLevelClass);

            generateBuilderMethod(builderClass);
            generateBuilderFromExistingObjectMethod(builderClass);
            generateConstructor(builderClass);

            final boolean jacksonSupport = BuilderOptionUtility.supportJacksonDeserialization(topLevelClass);
            if (jacksonSupport || BuilderOptionUtility.generateAllArgsConstructor(topLevelClass)) {
                allArgsConstructorGenerator.generateAllArgsConstructor(topLevelClass, jacksonSupport);
            }

            getterGenerator.generateGetters(topLevelClass);

            if (BuilderOptionUtility.generateToString(topLevelClass)) {
                toStringGenerator.generateToStringMethod(topLevelClass);
            }

            if (BuilderOptionUtility.generateHashCode(topLevelClass)) {
                hashCodeGenerator.generateHashCodeMethod(topLevelClass);
            }

            if (BuilderOptionUtility.generateEquals(topLevelClass)) {
                equalsGenerator.generateEqualsMethod(topLevelClass);
            }

            if (BuilderOptionUtility.generateClone(topLevelClass)) {
                cloneGenerator.generateClone(topLevelClass);
            }

            topLevelClass.add(builderClass);

            // Cleanup and format the generated code
            CodeFormatter.formatCode(topLevelClass, project);
        } catch (InvalidConstraintException e) {
            final ConstraintAlertDialog constraintAlertDialog = new ConstraintAlertDialog(project, e.getField(), e.getConstraint());
            constraintAlertDialog.show();
        }
    }

    private void prepareClassForBuilder(final PsiClass topLevelClass) {
        // Clean up previously-generated inner classes
        final PsiClass[] innerClasses = topLevelClass.getAllInnerClasses();
        for (final PsiClass innerClass : innerClasses) {
            if (!AnnotationUtility.hasCustomLogicAnnotation(innerClass)) {
                innerClass.delete();
            }
        }

        // Clean up previously-generated methods
        final PsiMethod[] methods = topLevelClass.getMethods();
        for (final PsiMethod method : methods) {
            if (!AnnotationUtility.hasCustomLogicAnnotation(method)) {
                method.delete();
            }
        }

        // Make all fields final
        final PsiField[] fields = topLevelClass.getFields();
        for (PsiField field : fields) {
            PsiUtil.setModifierProperty(field, PsiModifier.FINAL, true);
        }
    }

    private void generateBuilderMethod(final PsiClass builderClass) {
        final PsiMethod builderMethod = methodUtility.createPublicStaticMethod("builder", TypeUtils.getType(builderClass));
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, builderMethod);

        methodUtility.addReturnStatement(builderMethod, "Builder.create()");

        topLevelClass.add(builderMethod);
    }

    private void generateBuilderFromExistingObjectMethod(final PsiClass builderClass) {
        final PsiMethod builderMethod = methodUtility.createPublicStaticMethod("builder", TypeUtils.getType(builderClass));
        final PsiParameter parameter = parameterGenerator.buildParameter(topLevelClass);
        methodUtility.addParameter(builderMethod, parameter.getName(), parameter.getType());
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, builderMethod);

        methodUtility.addReturnStatement(builderMethod, "Builder.create(" + parameter.getName() + ")");

        topLevelClass.add(builderMethod);
    }

    private void generateConstructor(final PsiClass builderClass) {
        final PsiMethod constructor = methodUtility.createPrivateConstructor();
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, constructor);

        final PsiType builderType = TypeUtility.getTypeWithGenerics(builderClass, builderClass.getTypeParameters());
        methodUtility.addParameter(constructor, "builder", builderType);

        methodUtility.addStatement(constructor, "builder.validate();");

        final boolean makeCollectionsImmutable = BuilderOptionUtility.makeCollectionsImmutable(topLevelClass);

        // Assign values
        final PsiField[] fields = topLevelClass.getFields();
        for (PsiField field : fields) {
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                generateAssignStatement(constructor, field, makeCollectionsImmutable);
            }
        }

        topLevelClass.add(constructor);
    }

    private void generateAssignStatement(final PsiMethod constructor, final PsiField field, final boolean makeCollectionsImmutable) {
        final String fieldName = field.getName();
        final PsiType fieldType = field.getType();

        if (makeCollectionsImmutable) {
            final Optional<CollectionType> collectionTypeOptional = CollectionTypeFactory.getCollectionType(fieldType);
            if (collectionTypeOptional.isPresent() && TypeUtility.getNonGenericType(fieldType).equals(collectionTypeOptional.get().getCanonicalName())) {
                final String unmodifiableMethod = collectionTypeOptional.get().getUnmodifiableMethod();

                methodUtility.addStatement(constructor, String.format("this.%s = %s(builder.%s);", fieldName, unmodifiableMethod, fieldName));
                return;
            }
        }

        methodUtility.addStatement(constructor, String.format("this.%s = builder.%s;", fieldName, fieldName));
    }
}

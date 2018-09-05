package com.github.davidburkett.builderberg;

import com.github.davidburkett.builderberg.enums.CollectionType;
import com.github.davidburkett.builderberg.generators.*;
import com.github.davidburkett.builderberg.utilities.AnnotationUtility;
import com.github.davidburkett.builderberg.utilities.BuilderOptionUtility;
import com.github.davidburkett.builderberg.utilities.CollectionTypeFactory;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Asynchronous handler responsible for making the changes to an existing class to provide it a builder and all supporting functionality.
 */
public class BuilderbergRunnable implements Runnable {
    private final Project project;
    private final PsiElementFactory psiElementFactory;
    private final PsiClass topLevelClass;
    private final ToStringGenerator toStringGenerator;
    private final HashCodeGenerator hashCodeGenerator;
    private final EqualsGenerator equalsGenerator;
    private final BuilderClassGenerator builderClassGenerator;
    private final AllArgsConstructorGenerator allArgsConstructorGenerator;
    private final CloneGenerator cloneGenerator;

    public static BuilderbergRunnable create(final Project project, final PsiClass topLevelClass) {
        final PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
        final ToStringGenerator toStringGenerator = new ToStringGenerator(psiElementFactory);
        final HashCodeGenerator hashCodeGenerator = new HashCodeGenerator(psiElementFactory);
        final EqualsGenerator equalsGenerator = new EqualsGenerator(project, psiElementFactory);
        final BuilderClassGenerator builderClassGenerator = new BuilderClassGenerator(project, psiElementFactory);
        final AllArgsConstructorGenerator allArgsConstructorGenerator = new AllArgsConstructorGenerator(project);
        final CloneGenerator cloneGenerator = new CloneGenerator(project);

        return new BuilderbergRunnable(project, psiElementFactory, topLevelClass, toStringGenerator, hashCodeGenerator,
                equalsGenerator, builderClassGenerator, allArgsConstructorGenerator, cloneGenerator);
    }

    private BuilderbergRunnable(final Project project, final PsiElementFactory psiElementFactory, final PsiClass topLevelClass,
                                final ToStringGenerator toStringGenerator, final HashCodeGenerator hashCodeGenerator,
                                final EqualsGenerator equalsGenerator, final BuilderClassGenerator builderClassGenerator,
                                final AllArgsConstructorGenerator allArgsConstructorGenerator, final CloneGenerator cloneGenerator) {
        this.project = project;
        this.psiElementFactory = psiElementFactory;
        this.topLevelClass = topLevelClass;
        this.toStringGenerator = toStringGenerator;
        this.hashCodeGenerator = hashCodeGenerator;
        this.equalsGenerator = equalsGenerator;
        this.builderClassGenerator = builderClassGenerator;
        this.allArgsConstructorGenerator = allArgsConstructorGenerator;
        this.cloneGenerator = cloneGenerator;
    }

    @Override
    public void run() {
        // Clean up existing builder remnants, and make all class fields final
        prepareClassForBuilder(topLevelClass);

        // Create the inner-builder class
        final PsiClass builderClass = builderClassGenerator.createBuilderClass(topLevelClass);

        final boolean jacksonSupport = BuilderOptionUtility.supportJacksonDeserialization(topLevelClass);

        generateBuilderMethod(builderClass);
        generateConstructor(builderClass);
        if (jacksonSupport || BuilderOptionUtility.generateAllArgsConstructor(topLevelClass)) {
            allArgsConstructorGenerator.generateAllArgsConstructor(topLevelClass, builderClass, jacksonSupport);
        }

        generateGetters();

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
            // Add implements Cloneable
            final PsiClassType[] implementsClassTypes = topLevelClass.getImplementsList().getReferencedTypes();

            boolean alreadyImplemented = false;
            for (final PsiClassType implementsClassType : implementsClassTypes) {
                if (implementsClassType.getClassName().equalsIgnoreCase("Cloneable")) {
                    alreadyImplemented = true;
                    break;
                }
            }

            if (!alreadyImplemented) {
                final PsiClassType type = (PsiClassType)psiElementFactory.createTypeFromText("java.lang.Cloneable", topLevelClass);
                final PsiJavaCodeReferenceElement referenceElement = psiElementFactory.createReferenceElementByType(type);
                topLevelClass.getImplementsList().add(referenceElement);
            }

            // Add clone() method
            cloneGenerator.generateClone(topLevelClass);
        }

        topLevelClass.add(builderClass);

        // Cleanup and format the generated code
        formatGeneratedCode();
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
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            PsiUtil.setModifierProperty(field, PsiModifier.FINAL, true);
        }
    }

    private void generateBuilderMethod(final PsiClass builderClass) {
        final PsiType builderType = TypeUtils.getType(builderClass);
        final PsiMethod builderMethod = psiElementFactory.createMethod("builder", builderType);
        PsiUtil.setModifierProperty(builderMethod, PsiModifier.STATIC, true);
        PsiUtil.setModifierProperty(builderMethod, PsiModifier.PUBLIC, true);

        final PsiStatement returnStatement = psiElementFactory.createStatementFromText("return new Builder();", builderMethod);
        builderMethod.getBody().add(returnStatement);

        topLevelClass.add(builderMethod);
    }

    private void generateConstructor(final PsiClass builderClass) {
        final PsiMethod constructor = psiElementFactory.createConstructor();
        PsiUtil.setModifierProperty(constructor, PsiModifier.PRIVATE, true);

        final PsiType builderType = TypeUtility.getTypeWithGenerics(builderClass, builderClass.getTypeParameters());
        final PsiParameter parameter = psiElementFactory.createParameter("builder", builderType);
        PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);
        constructor.getParameterList().add(parameter);

        final PsiCodeBlock body = constructor.getBody();

        final PsiStatement validateStatement = psiElementFactory.createStatementFromText("builder.validate();", constructor);
        body.add(validateStatement);

        final boolean makeCollectionsImmutable = BuilderOptionUtility.makeCollectionsImmutable(topLevelClass);

        // Assign values
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
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
            if (collectionTypeOptional.isPresent() && TypeUtility.getNonGenericType(fieldType).equalsIgnoreCase(collectionTypeOptional.get().getCanonicalName())) {
                final String unmodifiableMethod = collectionTypeOptional.get().getUnmodifiableMethod();

                addStatementToMethod(constructor, String.format("this.%s = %s(builder.%s);", fieldName, unmodifiableMethod, fieldName));
                return;
            }
        }

        addStatementToMethod(constructor, String.format("this.%s = builder.%s;", fieldName, fieldName));
    }

    private void addStatementToMethod(final PsiMethod method, final String statementText) {
        final PsiStatement statement = psiElementFactory.createStatementFromText(statementText, method);
        method.getBody().add(statement);
    }

    private void generateGetters() {
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            final String fieldName = field.getName();
            final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            final String getterMethodName = String.format("get%s", capitalizedFieldName);
            generateGetter(field, getterMethodName);

            if (TypeUtility.isPrimitiveBoolean(field.getType())) {
                final String isMethodName = String.format("is%s", capitalizedFieldName);
                generateGetter(field, isMethodName);
            }
        }
    }

    private void generateGetter(final PsiField field, final String getterMethodName) {
        final PsiMethod getter = psiElementFactory.createMethod(getterMethodName, field.getType());

        final PsiCodeBlock body = getter.getBody();
        final String returnStatementText = String.format("return %s;", field.getName());
        final PsiStatement returnStatement =
                psiElementFactory.createStatementFromText(returnStatementText, getter);
        body.add(returnStatement);

        final JavadocGenerator javadocGenerator = new JavadocGenerator(psiElementFactory);
        javadocGenerator.generateCommentForGetterMethod(getter, field);

        topLevelClass.add(getter);
    }

    private void formatGeneratedCode() {
        final PsiJavaFile psiJavaFile = (PsiJavaFile)topLevelClass.getContainingFile();

        final JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);
        javaCodeStyleManager.shortenClassReferences(psiJavaFile);
        javaCodeStyleManager.optimizeImports(psiJavaFile);

        final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        codeStyleManager.reformat(psiJavaFile);
    }
}

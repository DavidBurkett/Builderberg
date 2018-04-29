package com.github.davidburkett.builderberg;

import com.github.davidburkett.builderberg.generators.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiUtil;
import com.intellij.vcs.log.Hash;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.Arrays;
import java.util.List;

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

    public static BuilderbergRunnable create(final Project project, final PsiClass topLevelClass) {
        final PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
        final ToStringGenerator toStringGenerator = new ToStringGenerator(psiElementFactory);
        final HashCodeGenerator hashCodeGenerator = new HashCodeGenerator(psiElementFactory);
        final EqualsGenerator equalsGenerator = new EqualsGenerator(project, psiElementFactory);
        final BuilderClassGenerator builderClassGenerator = new BuilderClassGenerator(project, psiElementFactory);

        return new BuilderbergRunnable(project, psiElementFactory, topLevelClass, toStringGenerator, hashCodeGenerator,
                equalsGenerator, builderClassGenerator);
    }

    private BuilderbergRunnable(final Project project, final PsiElementFactory psiElementFactory, final PsiClass topLevelClass,
            final ToStringGenerator toStringGenerator, final HashCodeGenerator hashCodeGenerator,
            final EqualsGenerator equalsGenerator, final BuilderClassGenerator builderClassGenerator) {
        this.project = project;
        this.psiElementFactory = psiElementFactory;
        this.topLevelClass = topLevelClass;
        this.toStringGenerator = toStringGenerator;
        this.hashCodeGenerator = hashCodeGenerator;
        this.equalsGenerator = equalsGenerator;
        this.builderClassGenerator = builderClassGenerator;
    }

    @Override
    public void run() {
        // Clean up existing builder remnants, and make all class fields final
        prepareClassForBuilder(topLevelClass);

        // Create the inner-builder class
        final PsiClass builderClass = builderClassGenerator.createBuilderClass(topLevelClass);

        generateBuilderMethod(builderClass);
        generateConstructor(builderClass);
        generateGetters();

        toStringGenerator.generateToStringMethod(topLevelClass);
        hashCodeGenerator.generateHashCodeMethod(topLevelClass);
        equalsGenerator.generateEqualsMethod(topLevelClass);

        // TODO: Generate clone or copy constructor

        topLevelClass.add(builderClass);

        // Cleanup and format the generated code
        formatGeneratedCode();
    }

    private void prepareClassForBuilder(final PsiClass topLevelClass) {
        // Clean up previously-generated inner classes
        final PsiClass[] innerClasses = topLevelClass.getAllInnerClasses();
        for (final PsiClass innerClass : innerClasses) {
            innerClass.delete();
        }

        // Clean up previously-generated methods
        final PsiMethod[] methods = topLevelClass.getMethods();
        for (final PsiMethod method : methods) {
            method.delete();
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
        // TODO: Handle generics
        final PsiMethod constructor = psiElementFactory.createConstructor();
        PsiUtil.setModifierProperty(constructor, PsiModifier.PRIVATE, true);

        final PsiType builderType = TypeUtils.getType(builderClass);
        final PsiParameter parameter = psiElementFactory.createParameter("builder", builderType);
        PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);
        constructor.getParameterList().add(parameter);

        final PsiCodeBlock body = constructor.getBody();

        final PsiStatement validateStatement = psiElementFactory.createStatementFromText("builder.validate();", constructor);
        body.add(validateStatement);

        // Assign values
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            final String fieldName = field.getName();

            final String assignStatementText = String.format("this.%s = builder.%s;", fieldName, fieldName);
            final PsiStatement assignStatement =
                    psiElementFactory.createStatementFromText(assignStatementText, constructor);
            body.add(assignStatement);
        }

        topLevelClass.add(constructor);
    }

    private void generateGetters() {
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            final String fieldName = field.getName();
            final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            final String getterMethodName = String.format("get%s", capitalizedFieldName);
            final PsiMethod getter = psiElementFactory.createMethod(getterMethodName, field.getType());

            final PsiCodeBlock body = getter.getBody();
            final String returnStatementText = String.format("return %s;", fieldName);
            final PsiStatement returnStatement =
                    psiElementFactory.createStatementFromText(returnStatementText, getter);
            body.add(returnStatement);

            final JavadocGenerator javadocGenerator = new JavadocGenerator(psiElementFactory);
            javadocGenerator.generateCommentForGetterMethod(getter, field);

            topLevelClass.add(getter);
        }
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

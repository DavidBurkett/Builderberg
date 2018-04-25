package com.burkett.builderberg;

import com.burkett.builderberg.generators.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.Arrays;
import java.util.List;

public class InnerBuilderRunnable implements Runnable {
    private final Project project;
    private final PsiElementFactory psiElementFactory;
    private final PsiClass topLevelClass;

    public InnerBuilderRunnable(final Project project, final PsiElementFactory psiElementFactory, final PsiClass topLevelClass) {
        this.project = project;
        this.psiElementFactory = psiElementFactory;
        this.topLevelClass = topLevelClass;
    }

    @Override
    public void run() {
        cleanupPreviousBuilder(topLevelClass);

        final BuilderClassGenerator builderClassGenerator = new BuilderClassGenerator(project, psiElementFactory);
        final PsiClass builderClass = builderClassGenerator.createBuilderClass(topLevelClass);

        makeFieldsFinal();
        generateConstructor(builderClass);
        generateGetters();

        final ToStringGenerator toStringGenerator = new ToStringGenerator(psiElementFactory);
        toStringGenerator.generateToStringMethod(topLevelClass);

        final HashCodeGenerator hashCodeGenerator = new HashCodeGenerator(psiElementFactory);
        hashCodeGenerator.generateHashCodeMethod(topLevelClass);

        final EqualsGenerator equalsGenerator = new EqualsGenerator(project, psiElementFactory);
        equalsGenerator.generateEqualsMethod(topLevelClass);

        // TODO: Generate clone or copy constructor

        topLevelClass.add(builderClass);

        final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        codeStyleManager.reformat(topLevelClass);
    }

    private void cleanupPreviousBuilder(final PsiClass topLevelClass) {
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
    }

    private void makeFieldsFinal() {
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            PsiUtil.setModifierProperty(field, PsiModifier.FINAL, true);
        }
    }

    private void generateConstructor(final PsiClass builderClass) {
        final PsiMethod constructor = psiElementFactory.createConstructor();
        PsiUtil.setModifierProperty(constructor, PsiModifier.PRIVATE, true);

        final PsiType builderType = TypeUtils.getType(builderClass);
        final PsiParameter parameter = psiElementFactory.createParameter("builder", builderType);
        PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);
        constructor.getParameterList().add(parameter);

        final ValidationGenerator validationGenerator = new ValidationGenerator(project, psiElementFactory);

        final PsiCodeBlock body = constructor.getBody();

        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            // Assign value
            final String fieldName = field.getName();

            // Validate input
            final List<PsiElement> validationStatments = validationGenerator.generateValidationForField(constructor, field);
            for (final PsiElement validationStatement : validationStatments) {
                body.add(validationStatement);
            }

            final PsiStatement assignStatement =
                    psiElementFactory.createStatementFromText("this." + fieldName + " = builder." + fieldName + ";", constructor);
            body.add(assignStatement);
        }

        topLevelClass.add(constructor);
    }

    private void generateGetters() {
        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (PsiField field : fields) {
            final String fieldName = field.getName();
            final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            final PsiMethod getter = psiElementFactory.createMethod("get" + capitalizedFieldName, field.getType());

            final PsiCodeBlock body = getter.getBody();
            final PsiStatement returnStatement =
                    psiElementFactory.createStatementFromText("return " + fieldName + ";", getter);
            body.add(returnStatement);

            final JavadocGenerator javadocGenerator = new JavadocGenerator(psiElementFactory);
            javadocGenerator.generateCommentForGetterMethod(getter, field);

            topLevelClass.add(getter);
        }
    }
}

package com.burkett.builderberg.generators;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

import java.lang.reflect.Type;

public class EqualsGenerator {
    private final Project project;
    private final PsiElementFactory psiElementFactory;

    public EqualsGenerator(final Project project, final PsiElementFactory psiElementFactory) {
        this.project = project;
        this.psiElementFactory = psiElementFactory;
    }

    public void generateEqualsMethod(final PsiClass topLevelClass) {
        // Create equals method
        final PsiMethod equalsMethod =
                psiElementFactory.createMethod("equals", PsiType.BOOLEAN);

        // Add parameter
        final PsiParameter parameter = psiElementFactory.createParameter("o", PsiType.getJavaLangObject(PsiManager.getInstance(project), GlobalSearchScope.allScope(project)));
        equalsMethod.getParameterList().add(parameter);

        // TODO: Generate Javadoc

        // Add @Override annotation
        final PsiAnnotation overrideAnnotation =
                psiElementFactory.createAnnotationFromText("@Override", topLevelClass);
        equalsMethod.addBefore(overrideAnnotation, equalsMethod.getFirstChild());

        // Add trivial comparison statement
        final PsiCodeBlock methodBody = equalsMethod.getBody();
        final PsiStatement trivialComparison =
                psiElementFactory.createStatementFromText("if (this == o) { return true; }", equalsMethod);
        methodBody.add(trivialComparison);

        // Add type comparison
        final PsiType type = TypeUtils.getType(topLevelClass);
        final String typeName = type.getCanonicalText();
        final String typeComparison = "if (!(o instanceof " + typeName + ")) { return false; }";
        final PsiStatement typeComparisonStatement = psiElementFactory.createStatementFromText(typeComparison, equalsMethod);
        methodBody.add(typeComparisonStatement);

        // Add type casting
        final String typeCasting = "final " + typeName + " obj = (" + typeName + ") o;";
        final PsiStatement typeCastStatement = psiElementFactory.createStatementFromText(typeCasting, equalsMethod);
        methodBody.add(typeCastStatement);

        // Add comparison for each field
        for (final PsiField field : topLevelClass.getFields()) {
            generateFieldComparison(topLevelClass, equalsMethod, methodBody, field);
        }

        // Add return true statement
        final PsiStatement returnTrue = psiElementFactory.createStatementFromText("return true;", equalsMethod);
        methodBody.add(returnTrue);

        topLevelClass.add(equalsMethod);
    }

    private void generateFieldComparison(final PsiClass topLevelClass, final PsiMethod equalsMethod, final PsiCodeBlock codeBlock, final PsiField field) {
        final String fieldName = field.getName();
        final PsiType fieldType = field.getType();
        if (fieldType instanceof PsiPrimitiveType) {
            final String comparison = "if (field != obj.field) { return false; }".replaceAll("field", fieldName);
            final PsiStatement comparisonStatement = psiElementFactory.createStatementFromText(comparison, equalsMethod);
            codeBlock.add(comparisonStatement);
        } else {
            final String comparison = "if (!(field == obj.field || (field != null && field.equals(obj.field)))) { return false; }".replaceAll("field", fieldName);
            final PsiStatement comparisonStatement = psiElementFactory.createStatementFromText(comparison, equalsMethod);
            codeBlock.add(comparisonStatement);
        }
    }
}

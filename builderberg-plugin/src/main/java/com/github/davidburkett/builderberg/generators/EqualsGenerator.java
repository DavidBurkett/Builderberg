package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.AnnotationUtility;
import com.github.davidburkett.builderberg.utilities.BuilderOptionUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;

public class EqualsGenerator {
    private final Project project;
    private final PsiElementFactory psiElementFactory;
    private final MethodUtility methodUtility;

    public EqualsGenerator(final Project project, final PsiElementFactory psiElementFactory) {
        this.project = project;
        this.psiElementFactory = psiElementFactory;
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    /**
     * Generates an equals method for the given {@link PsiClass topLevelClass} and adds it to that class.
     * @param topLevelClass The {@link PsiClass} to generate an equals method for.
     */
    public void generateEqualsMethod(final PsiClass topLevelClass) {
        // Create equals method
        final PsiMethod equalsMethod = methodUtility.createPublicMethod("equals", PsiType.BOOLEAN);

        // Add parameter
        methodUtility.addParameter(equalsMethod, "o", TypeUtility.getJavaLangObject(project));

        // Generate inheritDoc javadoc
        methodUtility.addJavadoc(equalsMethod, ImmutableList.of("{@inheritDoc}"));

        // Add @Generated annotation
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, equalsMethod);

        // Add @Override annotation
        AnnotationUtility.addOverrideAnnotation(equalsMethod);

        // Add trivial comparison statement
        methodUtility.addIfStatement(equalsMethod, "this == o", "return true;");

        // Add type comparison
        final String typeName = TypeUtils.getType(topLevelClass).getCanonicalText();
        methodUtility.addIfStatement(equalsMethod, String.format("!(o instanceof %s)", typeName), "return false;");

        // Add type casting
        methodUtility.addStatement(equalsMethod, String.format("final %s obj = (%s) o;", typeName, typeName));

        // Check if excludeStaticFields is enabled
        final boolean excludeStaticFields = BuilderOptionUtility.excludeStaticFields(topLevelClass);

        // Add comparison for each field
        for (final PsiField field : topLevelClass.getFields()) {
            if (excludeStaticFields && field.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }

            generateFieldComparison(equalsMethod, field);
        }

        // Add return true statement
        methodUtility.addReturnStatement(equalsMethod, "true");

        topLevelClass.add(equalsMethod);
    }

    private void generateFieldComparison(final PsiMethod equalsMethod, final PsiField field) {
        final String fieldName = field.getName();
        final PsiType fieldType = field.getType();
        if (fieldType instanceof PsiPrimitiveType) {
            methodUtility.addIfStatement(equalsMethod, String.format("%s != obj.%s", fieldName, fieldName), "return false;");
        } else if (fieldType instanceof PsiArrayType) {
            methodUtility.addIfStatement(equalsMethod, String.format("!java.util.Arrays.equals(%s, obj.%s)", fieldName, fieldName), "return false;");
        } else {
            final String comparison = "if (!(field == obj.field || (field != null && field.equals(obj.field)))) { return false; }";
            methodUtility.addStatement(equalsMethod, comparison.replaceAll("field", fieldName));
        }
    }
}

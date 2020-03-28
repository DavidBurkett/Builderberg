package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.AnnotationUtility;
import com.github.davidburkett.builderberg.utilities.BuilderOptionUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class HashCodeGenerator {
    private final PsiElementFactory psiElementFactory;
    private final MethodUtility methodUtility;

    public HashCodeGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    /**
     * Implements a hashCode method using the hash function described by Josh Bloch in "Effective Java".
     * @param topLevelClass The class to generate the hashCode method for.
     */
    public void generateHashCodeMethod(final PsiClass topLevelClass) {
        // Create hashCode method
        final PsiMethod hashCodeMethod = methodUtility.createPublicMethod("hashCode", PsiType.INT);

        // Generate inheritDoc javadoc
        methodUtility.addJavadoc(hashCodeMethod, ImmutableList.of("{@inheritDoc}"));

        // Add @Generated annotation
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, hashCodeMethod);

        // Add @Override annotation
        AnnotationUtility.addOverrideAnnotation(hashCodeMethod);

        // Add return statement
        methodUtility.addReturnStatement(hashCodeMethod, getHashCodeArguments(topLevelClass));

        topLevelClass.add(hashCodeMethod);
    }

    private String getHashCodeArguments(final PsiClass topLevelClass) {
        final boolean excludeStaticFields = BuilderOptionUtility.excludeStaticFields(topLevelClass);

        final PsiField[] fields = topLevelClass.getFields();
        final String hashCodeParams = Arrays.stream(fields)
                .map(field -> getHashCodeArgument(field, excludeStaticFields))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        return String.format("java.util.Objects.hash(%s)", hashCodeParams);
    }

    private String getHashCodeArgument(final PsiField field, final boolean excludeStaticFields) {
        if (excludeStaticFields && field.hasModifierProperty(PsiModifier.STATIC)) {
            return null;
        }

        final PsiType type = field.getType();
        final String fieldName = field.getName();
        final PsiType[] superTypes = type.getSuperTypes();
        final boolean isEnum = superTypes.length > 0 && superTypes[0].getCanonicalText().startsWith("java.lang.Enum");
        if (isEnum) {
            return String.format("(%s != null ? %s.name().hashCode() : 0)", fieldName, fieldName);
        }

        return fieldName;
    }
}

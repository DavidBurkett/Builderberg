package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.fest.util.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AnnotationUtility {
    private static final String CUSTOM_LOGIC = "com.github.davidburkett.builderberg.annotations.CustomLogic";
    private static final String BUILDER_CONSTRAINT = "com.github.davidburkett.builderberg.annotations.BuilderConstraint";
    private static final String BUILDER_OPTIONS = "com.github.davidburkett.builderberg.annotations.BuilderOptions";

    public static boolean hasCustomLogicAnnotation(final PsiMethod method) {
        return hasCustomLogicAnnotation(method.getAnnotations());
    }

    public static boolean hasCustomLogicAnnotation(final PsiClass psiClass) {
        return hasCustomLogicAnnotation(psiClass.getAnnotations());
    }

    private static boolean hasCustomLogicAnnotation(final PsiAnnotation[] annotations) {
        final Optional<PsiAnnotation> customLogicAnnotation = Arrays.stream(annotations)
                .filter(a -> a.getQualifiedName().equals(CUSTOM_LOGIC))
                .findFirst();

        return customLogicAnnotation.isPresent();
    }

    public static List<PsiAnnotation> getBuilderConstraintAnnotations(final PsiField field) {
        final List<PsiAnnotation> builderConstaintAnnotations = Lists.newArrayList();

        final PsiAnnotation[] annotations = field.getAnnotations();
        for (final PsiAnnotation annotation : annotations) {
            if (annotation.getQualifiedName().equals(BUILDER_CONSTRAINT)) {
                builderConstaintAnnotations.add(annotation);
            }
        }

        return builderConstaintAnnotations;
    }

    public static Optional<PsiAnnotation> getBuilderOptionsAnnotation(final PsiClass psiClass) {
        final PsiAnnotation[] annotations = psiClass.getAnnotations();
        return Arrays.stream(annotations)
                .filter(a -> a.getQualifiedName().equals(BUILDER_OPTIONS))
                .findFirst();
    }
}

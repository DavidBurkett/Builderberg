package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.*;

import java.util.Optional;

public class BuilderOptionUtility {
    public static boolean supportJacksonDeserialization(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "deserializable");
        if (value != null) {
            final String text = value.getText();
            if (text != null && text.contains("jackson")) {
                return true;
            }
        }

        return false;
    }

    public static String minimumPluginVersion(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "minimumPluginVersion");
        if (value != null) {
            final String text = value.getText();
            if (text != null) {
                return text.trim().replaceAll("^\"|\"$", "");
            }
        }

        return "1.0.0";
    }

    public static boolean generateAllArgsConstructor(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "generateAllArgsConstructor");
        if (value != null) {
            final String text = value.getText();
            if (text != null && text.equals("true")) {
                return true;
            }
        }

        return false;
    }

    public static boolean generateToString(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "generateToString");
        if (value != null) {
            final String text = value.getText();
            if (text != null && text.equals("false")) {
                return false;
            }
        }

        return true;
    }

    public static boolean generateEquals(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "generateEquals");
        if (value != null) {
            final String text = value.getText();
            if (text != null && text.equals("false")) {
                return false;
            }
        }

        return true;
    }

    public static boolean generateHashCode(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "generateHashCode");
        if (value != null) {
            final String text = value.getText();
            if (text != null && text.equals("false")) {
                return false;
            }
        }

        return true;
    }

    public static boolean generateClone(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "generateClone");
        if (value != null) {
            final String text = value.getText();
            if (text != null && text.equals("true")) {
                return true;
            }
        }

        return false;
    }

    public static PsiType exceptionType(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "exceptionType");
        if (value != null) {
            if (value instanceof PsiClassObjectAccessExpression) {
                final PsiType type = ((PsiClassObjectAccessExpression) value).getOperand().getType();
                if (type != null) {
                    return type;
                }
            }
        }

        return PsiType.getTypeByName("java.lang.AssertionError", topLevelClass.getProject(), topLevelClass.getResolveScope());
    }

    public static boolean makeCollectionsImmutable(final PsiClass topLevelClass) {
        final PsiAnnotationMemberValue value = getBuilderOption(topLevelClass, "makeCollectionsImmutable");
        if (value != null) {
            final String text = value.getText();
            if (text != null && text.equals("true")) {
                return true;
            }
        }

        return false;
    }

    private static PsiAnnotationMemberValue getBuilderOption(final PsiClass topLevelClass, final String attributeName) {
        final Optional<PsiAnnotation> psiAnnotationOptional = AnnotationUtility.getBuilderOptionsAnnotation(topLevelClass);

        if (psiAnnotationOptional.isPresent()) {
            return psiAnnotationOptional.get().findAttributeValue(attributeName);
        }

        return null;
    }
}

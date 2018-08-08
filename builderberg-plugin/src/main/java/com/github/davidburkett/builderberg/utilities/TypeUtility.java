package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.*;
import com.siyeh.ig.psiutils.TypeUtils;
import org.fest.util.Maps;

import java.util.Map;

public class TypeUtility {
    /**
     * Determines if the given {@link PsiType} is a type of the given canonical name.
     * @param type The non-null {@link PsiType}.
     * @param canonicalName The canonical name of the type to compare. Example: "java.util.Collection".
     * @return True if it is of the given type. Otherwise, false.
     */
    public static boolean isOfType(final PsiType type, final String canonicalName) {
        if (type.getCanonicalText().equals(canonicalName)) {
            return true;
        }

        if (getNonGenericType(type).equals(canonicalName)) {
            return true;
        }

        for (final PsiType iterType : type.getSuperTypes()) {
            if (iterType.getCanonicalText().equals(canonicalName) || getNonGenericType(iterType).equals(canonicalName)) {
                return true;
            }
        }

        return false;
    }

    private static String getNonGenericType(final PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClassType pct = (PsiClassType) type;
            return pct.resolve().getQualifiedName();
        }

        return type.getCanonicalText();
    }

    public static boolean isMap(final PsiType type) {
        return isOfType(type, "java.util.Map");
    }

    public static boolean isCollection(final PsiType type) {
        return isOfType(type, "java.util.Collection");
    }

    public static boolean isString(final PsiType type, final PsiElement context) {
        return type.equals(TypeUtils.getStringType(context));
    }

    /**
     * Determines the type of the values in the given array or collection {@link PsiType}.
     * @param type The {@link PsiType} of the array or collection.
     * @return The non-null {@link PsiType} of the values in the array/collection.
     */
    public static PsiType getGenericValueType(final PsiType type) {
        if (type instanceof PsiArrayType) {
            final PsiArrayType arrayType = (PsiArrayType)type;
            return arrayType.getComponentType();
        } else if (type instanceof PsiClassType) {
            final PsiClassType classType = (PsiClassType) type;
            if (isMap(type)) {
                return classType.getParameters()[1];
            } else if (isCollection(type)) {
                return classType.getParameters()[0];
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Determines the type of the keys in the given Map {@link PsiType}.
     * @param type The {@link PsiType} of the java.util.Map.
     * @return The non-null {@link PsiType} of the keys in the Map.
     */
    public static PsiType getGenericKeyType(final PsiType type) {
        if (TypeUtility.isMap(type)) {
            if (type instanceof PsiClassType) {
                final PsiClassType classType = (PsiClassType)type;
                return classType.getParameters()[0];
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Creates a PsiType for a PsiClass enriched with generic substitution information if available
     */
    public static PsiType getTypeWithGenerics(final PsiClass psiClass, final PsiTypeParameter... classTypeParameters) {
        final PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        if (classTypeParameters.length > 0) {
            final Map<PsiTypeParameter, PsiType> substitutionMap = Maps.newHashMap();
            for (PsiTypeParameter typeParameter : classTypeParameters) {
                substitutionMap.put(typeParameter, factory.createType(typeParameter));
            }

            return factory.createType(psiClass, factory.createSubstitutor(substitutionMap));
        } else {
            return factory.createType(psiClass);
        }
    }
}

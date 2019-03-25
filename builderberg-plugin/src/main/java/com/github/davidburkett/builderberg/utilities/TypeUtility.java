package com.github.davidburkett.builderberg.utilities;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        final String nonGenericType = getNonGenericType(type);
        if (nonGenericType != null && nonGenericType.equals(canonicalName)) {
            return true;
        }

        for (final PsiType iterType : type.getSuperTypes()) {
            if (iterType.getCanonicalText().equals(canonicalName) || getNonGenericType(iterType).equals(canonicalName)) {
                return true;
            }
        }

        return false;
    }

    public static String getNonGenericType(final PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClassType pct = (PsiClassType) type;
            return pct.resolve().getQualifiedName();
        }

        return type.getCanonicalText();
    }

    public static boolean isCollection(final PsiType type) {
        return isOfType(type, "java.util.Collection");
    }

    public static boolean isList(final PsiType type) {
        return isOfType(type, "java.util.List");
    }

    public static boolean isSet(final PsiType type) {
        return isOfType(type, "java.util.Set");
    }

    public static boolean isMap(final PsiType type) {
        return isOfType(type, "java.util.Map");
    }

    public static boolean isString(final PsiType type, final PsiElement context) {
        return type.equals(TypeUtils.getStringType(context));
    }

    public static boolean isPrimitiveBoolean(final PsiType type) {
        return type == PsiType.BOOLEAN;
    }

    public static boolean isNumeric(final PsiType type) {
        final PsiType unboxedType = unboxIfPossible(type);
        if (unboxedType instanceof PsiPrimitiveType) {
            if (unboxedType != PsiType.BOOLEAN) {
                return true;
            }
        }

        return false;
    }

    private static PsiType unboxIfPossible(final PsiType type) {
        final Map<String, PsiType> unboxedTypesByBoxedName = new HashMap<>();
        unboxedTypesByBoxedName.put(CommonClassNames.JAVA_LANG_BOOLEAN, PsiType.BOOLEAN);
        unboxedTypesByBoxedName.put(CommonClassNames.JAVA_LANG_BYTE, PsiType.BYTE);
        unboxedTypesByBoxedName.put(CommonClassNames.JAVA_LANG_SHORT, PsiType.SHORT);
        unboxedTypesByBoxedName.put(CommonClassNames.JAVA_LANG_INTEGER, PsiType.INT);
        unboxedTypesByBoxedName.put(CommonClassNames.JAVA_LANG_LONG, PsiType.LONG);
        unboxedTypesByBoxedName.put(CommonClassNames.JAVA_LANG_FLOAT, PsiType.FLOAT);
        unboxedTypesByBoxedName.put(CommonClassNames.JAVA_LANG_DOUBLE, PsiType.DOUBLE);
        unboxedTypesByBoxedName.put(CommonClassNames.JAVA_LANG_CHARACTER, PsiType.CHAR);

        final Optional<String> boxedPrimitive = unboxedTypesByBoxedName.keySet().stream().filter(boxed -> isOfType(type, boxed)).findFirst();

        return boxedPrimitive.map(name -> unboxedTypesByBoxedName.get(name)).orElse(type);
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
            final Map<PsiTypeParameter, PsiType> substitutionMap = new HashMap<>();
            for (PsiTypeParameter typeParameter : classTypeParameters) {
                substitutionMap.put(typeParameter, factory.createType(typeParameter));
            }

            return factory.createType(psiClass, factory.createSubstitutor(substitutionMap));
        } else {
            return factory.createType(psiClass);
        }
    }

    public static PsiType getJavaLangObject(final Project project) {
        return PsiType.getJavaLangObject(PsiManager.getInstance(project), GlobalSearchScope.allScope(project));
    }
}

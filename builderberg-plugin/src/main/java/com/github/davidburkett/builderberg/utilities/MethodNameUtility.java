package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.PsiField;

public class MethodNameUtility {
    public static String getSetterName(final PsiField field) {
        final String fieldName = field.getName();
        final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        return "with" + capitalizedFieldName;
    }

    public static String getGetterName(final PsiField field) {
        final String fieldName = field.getName();
        final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        return "get" + capitalizedFieldName;
    }

    public static String getIsMethodName(final PsiField field) {
        final String fieldName = field.getName();
        final String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        return "is" + capitalizedFieldName;
    }
}

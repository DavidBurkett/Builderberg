package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import org.fest.util.Lists;

import java.util.Arrays;
import java.util.List;

public class QualifyingFieldsFinder {

    public static List<PsiField> findQualifyingFields(final PsiClass topLevelClass) {
        final List<PsiField> qualifyingFields = Lists.newArrayList();

        final List<PsiField> fields = Arrays.asList(topLevelClass.getFields());
        for (final PsiField field : fields) {

            // Skip final fields that are already initialized.
            if (field.hasModifierProperty(PsiModifier.FINAL)) {
                if (field.getInitializer() != null) {
                    continue;
                }
            }

            // Skip static fields.
            if (field.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }

            qualifyingFields.add(field);
        }

        return qualifyingFields;
    }
}

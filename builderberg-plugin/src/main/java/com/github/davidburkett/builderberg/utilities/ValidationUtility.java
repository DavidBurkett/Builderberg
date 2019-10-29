package com.github.davidburkett.builderberg.utilities;

import com.github.davidburkett.builderberg.enums.BuilderConstraintOption;
import com.github.davidburkett.builderberg.model.BuilderConstraintOptionAndBooleanValue;
import com.github.davidburkett.builderberg.model.BuilderConstraintOptionAndValue;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiNameValuePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ValidationUtility {

    private ValidationUtility() {
    }

    public static List<BuilderConstraintOptionAndValue> getBuilderConstraintsForField(final PsiField field) {
        final List<BuilderConstraintOptionAndValue> result = new ArrayList<>();

        boolean notNullAdded = false;
        final List<PsiAnnotation> builderConstraintAnnotations = AnnotationUtility.getBuilderConstraintAnnotations(
                field
        );
        for (final PsiAnnotation annotation : builderConstraintAnnotations) {
            final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
            for (final PsiNameValuePair attribute : attributes) {
                final Optional<BuilderConstraintOptionAndValue> optionalBuilderConstraintOptionAndValue =
                        BuilderConstraintOptionAndValueFactory.get(attribute);

                if (optionalBuilderConstraintOptionAndValue.isPresent()) {
                    final BuilderConstraintOptionAndValue builderConstraintOptionAndValue =
                            optionalBuilderConstraintOptionAndValue.get();

                    if (!notNullAdded && builderConstraintOptionAndValue.getOption().isNotNullRequired()) {
                        result.add(
                                0,
                                new BuilderConstraintOptionAndBooleanValue(BuilderConstraintOption.NOT_NULL, true)
                        );
                        notNullAdded = true;
                    }

                    result.add(builderConstraintOptionAndValue);
                }
            }
        }

        return Collections.unmodifiableList(result);
    }
}

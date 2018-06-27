package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationParameterList;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiNameValuePair;
import org.fest.util.Lists;

import java.util.List;

public class ValidationUtility {
    public static List<PsiNameValuePair> getBuilderConstraintsForField(final PsiField field) {
        final List<PsiNameValuePair> builderConstraints = Lists.newArrayList();

        final PsiAnnotation[] annotations = field.getAnnotations();
        for (final PsiAnnotation annotation : annotations) {
            if (annotation.getQualifiedName().equals("com.github.davidburkett.builderberg.annotations.BuilderConstraint")) {
                final PsiAnnotationParameterList annotationParameterList = annotation.getParameterList();
                final PsiNameValuePair[] attributes = annotationParameterList.getAttributes();
                for (final PsiNameValuePair attribute : attributes) {
                    if (attribute.getLiteralValue() != null && !attribute.getLiteralValue().equals("false")) {
                        builderConstraints.add(attribute);
                    }
                }
            }
        }

        return builderConstraints;
    }
}

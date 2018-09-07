package com.github.davidburkett.builderberg.exceptions;

import com.intellij.psi.PsiField;

public class InvalidConstraintException extends Exception {
    private final PsiField field;
    private final String constraint;

    public InvalidConstraintException(final PsiField field, final String constraint) {
        super();
        this.field = field;
        this.constraint = constraint;
    }

    public PsiField getField() {
        return field;
    }

    public String getConstraint() {
        return constraint;
    }
}

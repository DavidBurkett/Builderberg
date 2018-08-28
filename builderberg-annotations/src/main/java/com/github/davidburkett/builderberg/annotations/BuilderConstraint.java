package com.github.davidburkett.builderberg.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation logic will be applied to fields annotated with this.
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface BuilderConstraint {
    boolean notNull() default false;
    boolean notEmpty() default false;
    boolean notBlank() default false;

    boolean noNullKeys() default false;
    boolean noNullValues() default false;

    boolean notNegative() default false;
    boolean notPositive() default false;
    boolean negativeOnly() default false;
    boolean positiveOnly() default false;

    double minValue() default 0.0;
    double maxValue() default 0.0;

    @Deprecated
    String customValidation() default "";
}

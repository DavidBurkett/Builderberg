package com.burkett.builderberg.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface BuilderConstraint {
    boolean notNull() default false;
    boolean notEmpty() default false;
    boolean notBlank() default false;

    boolean noNullKeys() default false;
    boolean noNullValues() default false;
    // TODO: NoNegativeValues()?

    boolean notNegative() default false;
    boolean notPositive() default false;
    boolean negativeOnly() default false;
    boolean positiveOnly() default false;

    double minValue();
    double maxValue();

    String customValidation();
}

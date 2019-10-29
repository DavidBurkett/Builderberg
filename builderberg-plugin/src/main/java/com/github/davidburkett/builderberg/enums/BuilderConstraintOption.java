package com.github.davidburkett.builderberg.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum BuilderConstraintOption {
    NOT_NULL("notNull", false),
    NOT_EMPTY("notEmpty", true),
    NOT_BLANK("notBlank", true),
    NO_NULL_KEYS("noNullKeys", false),
    NO_NULL_VALUES("noNullValues", false),
    NOT_NEGATIVE("notNegative", false),
    NOT_POSITIVE("notPositive", false),
    NEGATIVE_ONLY("negativeOnly", true),
    POSITIVE_ONLY("positiveOnly", true),
    MIN_VALUE("minValue", false),
    MAX_VALUE("maxValue", false),
    CUSTOM("customValidation", false);

    private static final Map<String, BuilderConstraintOption> VALUE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(BuilderConstraintOption::getValue, Function.identity()));

    private final String value;
    private final boolean notNullRequired;

    BuilderConstraintOption(final String value, final boolean notNullRequired) {
        this.value = value;
        this.notNullRequired = notNullRequired;
    }

    public String getValue() {
        return value;
    }

    public boolean isNotNullRequired() {
        return notNullRequired;
    }

    public static BuilderConstraintOption fromValue(final String value) {
        final BuilderConstraintOption builderContraint = VALUE_MAP.get(value);

        return Optional.ofNullable(builderContraint)
                .orElseThrow(() -> new IllegalArgumentException(String.format("unsupported value (%s)", value)));
    }
}

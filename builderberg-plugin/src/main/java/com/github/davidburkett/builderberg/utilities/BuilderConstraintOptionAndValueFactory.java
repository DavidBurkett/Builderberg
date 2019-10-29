package com.github.davidburkett.builderberg.utilities;

import com.github.davidburkett.builderberg.enums.BuilderConstraintOption;
import com.github.davidburkett.builderberg.model.BuilderConstraintOptionAndBooleanValue;
import com.github.davidburkett.builderberg.model.BuilderConstraintOptionAndDoubleValue;
import com.github.davidburkett.builderberg.model.BuilderConstraintOptionAndStringValue;
import com.github.davidburkett.builderberg.model.BuilderConstraintOptionAndValue;
import com.intellij.psi.PsiNameValuePair;

import java.util.Optional;

public final class BuilderConstraintOptionAndValueFactory {

    private BuilderConstraintOptionAndValueFactory() {
    }

    public static Optional<BuilderConstraintOptionAndValue> get(final PsiNameValuePair attribute) {
        if (attribute.getLiteralValue() == null) {
            return Optional.empty();
        }

        final BuilderConstraintOption builderConstraintOption = BuilderConstraintOption.fromValue(attribute.getName());

        switch (builderConstraintOption) {
            case NOT_NULL:
            case NOT_EMPTY:
            case NOT_BLANK:
            case NO_NULL_KEYS:
            case NO_NULL_VALUES:
            case NOT_NEGATIVE:
            case NOT_POSITIVE:
            case NEGATIVE_ONLY:
            case POSITIVE_ONLY:
                return Optional.of(
                        new BuilderConstraintOptionAndBooleanValue(
                                builderConstraintOption,
                                Optional.ofNullable(attribute.getLiteralValue())
                                        .orElse("false")
                                        .trim()
                                        .equalsIgnoreCase("true")
                        )
                );
            case MIN_VALUE:
            case MAX_VALUE:
                return Optional.of(
                        new BuilderConstraintOptionAndDoubleValue(
                                builderConstraintOption,
                                Double.parseDouble(
                                        Optional.ofNullable(attribute.getLiteralValue())
                                                .orElse("0")
                                                .trim()
                                )
                        )
                );
            case CUSTOM:
                return Optional.of(
                        new BuilderConstraintOptionAndStringValue(
                                builderConstraintOption,
                                Optional.ofNullable(attribute.getLiteralValue()).orElse("").trim()
                        )
                );
        }

        return Optional.empty();
    }
}

package com.github.davidburkett.builderberg.model;

import com.github.davidburkett.builderberg.enums.BuilderConstraintOption;

public class BuilderConstraintOptionAndStringValue implements BuilderConstraintOptionAndValue<String> {

    private final BuilderConstraintOption option;
    private final String value;

    public BuilderConstraintOptionAndStringValue(final BuilderConstraintOption option, final String value) {
        this.option = option;
        this.value = value;
    }

    @Override
    public BuilderConstraintOption getOption() {
        return option;
    }

    @Override
    public String getValue() {
        return value;
    }
}

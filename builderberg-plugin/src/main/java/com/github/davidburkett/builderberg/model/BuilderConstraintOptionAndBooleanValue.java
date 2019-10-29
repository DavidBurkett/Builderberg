package com.github.davidburkett.builderberg.model;

import com.github.davidburkett.builderberg.enums.BuilderConstraintOption;

public class BuilderConstraintOptionAndBooleanValue implements BuilderConstraintOptionAndValue<Boolean> {

    private final BuilderConstraintOption option;
    private final boolean value;

    public BuilderConstraintOptionAndBooleanValue(final BuilderConstraintOption option, final Boolean value) {
        this.option = option;
        this.value = value;
    }

    @Override
    public BuilderConstraintOption getOption() {
        return option;
    }

    @Override
    public Boolean getValue() {
        return value;
    }
}

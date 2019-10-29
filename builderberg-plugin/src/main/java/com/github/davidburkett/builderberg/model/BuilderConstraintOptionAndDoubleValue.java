package com.github.davidburkett.builderberg.model;

import com.github.davidburkett.builderberg.enums.BuilderConstraintOption;

public class BuilderConstraintOptionAndDoubleValue implements BuilderConstraintOptionAndValue<Double> {

    private final BuilderConstraintOption option;
    private final double value;

    public BuilderConstraintOptionAndDoubleValue(final BuilderConstraintOption option, final Double value) {
        this.option = option;
        this.value = value;
    }

    @Override
    public BuilderConstraintOption getOption() {
        return option;
    }

    @Override
    public Double getValue() {
        return value;
    }
}

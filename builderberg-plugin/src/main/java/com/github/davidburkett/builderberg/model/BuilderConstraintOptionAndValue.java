package com.github.davidburkett.builderberg.model;

import com.github.davidburkett.builderberg.enums.BuilderConstraintOption;

public interface BuilderConstraintOptionAndValue<V> {
    BuilderConstraintOption getOption();
    V getValue();
}

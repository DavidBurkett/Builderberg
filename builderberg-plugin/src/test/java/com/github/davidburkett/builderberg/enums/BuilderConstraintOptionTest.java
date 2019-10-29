package com.github.davidburkett.builderberg.enums;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.github.davidburkett.builderberg.enums.BuilderConstraintOption.*;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BuilderConstraintOptionTest {

    @Test
    public void test_getValue_success() {
        assertEquals("notNull", NOT_NULL.getValue());
        assertEquals("notEmpty", NOT_EMPTY.getValue());
        assertEquals("notBlank", NOT_BLANK.getValue());
        assertEquals("noNullKeys", NO_NULL_KEYS.getValue());
        assertEquals("noNullValues", NO_NULL_VALUES.getValue());
        assertEquals("notNegative", NOT_NEGATIVE.getValue());
        assertEquals("notPositive", NOT_POSITIVE.getValue());
        assertEquals("negativeOnly", NEGATIVE_ONLY.getValue());
        assertEquals("positiveOnly", POSITIVE_ONLY.getValue());
        assertEquals("minValue", MIN_VALUE.getValue());
        assertEquals("maxValue", MAX_VALUE.getValue());
        assertEquals("customValidation", CUSTOM.getValue());
    }

    @Test
    public void test_fromValue_success() {
        assertEquals(NOT_NULL, BuilderConstraintOption.fromValue("notNull"));
        assertEquals(NOT_EMPTY, BuilderConstraintOption.fromValue("notEmpty"));
        assertEquals(NOT_BLANK, BuilderConstraintOption.fromValue("notBlank"));
        assertEquals(NO_NULL_KEYS, BuilderConstraintOption.fromValue("noNullKeys"));
        assertEquals(NO_NULL_VALUES, BuilderConstraintOption.fromValue("noNullValues"));
        assertEquals(NOT_NEGATIVE, BuilderConstraintOption.fromValue("notNegative"));
        assertEquals(NOT_POSITIVE, BuilderConstraintOption.fromValue("notPositive"));
        assertEquals(NEGATIVE_ONLY, BuilderConstraintOption.fromValue("negativeOnly"));
        assertEquals(POSITIVE_ONLY, BuilderConstraintOption.fromValue("positiveOnly"));
        assertEquals(MIN_VALUE, BuilderConstraintOption.fromValue("minValue"));
        assertEquals(MAX_VALUE, BuilderConstraintOption.fromValue("maxValue"));
        assertEquals(CUSTOM, BuilderConstraintOption.fromValue("customValidation"));
    }
}
package com.github.davidburkett.builderberg.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows specification of configuration options to apply to builder code generated for the class.
 * @since 1.1.0
 * NOTE: 1.0.3 and older will ignore these options.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface BuilderOptions {
    /**
     * Generates constructors, annotations, and/or methods to support the specified DeserializationTypes.
     * @return An array containing the deserialization types the generated object should support.
     */
    DeserializationType[] deserializable() default {};

    /**
     * The minimum required version of the builderberg plugin that should be used to generate a builder for the class.
     * You will be prevented from generating an inner builder using an older version of the builderberg plugin.
     * @return The minimum builderberg plugin version. Uses semantic versioning (#.#.#)
     */
    String minimumPluginVersion() default "1.1.0";

    /**
     * @return {@code true} if an all-arguments constructor should be generated for the generated object.
     */
    boolean generateAllArgsConstructor() default false;

    /**
     * @return {@code true} if a toString method should be generated for the generated object.
     */
    boolean generateToString() default true;

    /**
     * @return {@code true} if an equals method should be generated for the generated object.
     */
    boolean generateEquals() default true;

    /**
     * @return {@code true} if a hashCode method should be generated for the generated object.
     */
    boolean generateHashCode() default true;

    /**
     * @return {@code true} if a clone method should be generated for the generated object.
     */
    boolean generateClone() default false;

    /**
     * @return The exception/error type to throw when builder constraints are violated (@see {@link BuilderConstraint}).
     * NOTE: The Throwable must have a constructor that takes in a string as its only parameter.
     */
    Class<? extends Throwable> exceptionType() default AssertionError.class;

    /**
     * @return {@code true} if collections for generated objects should be made immutable.
     */
    boolean makeCollectionsImmutable() default false;

    /**
     * Excludes static fields from getter, hashCode, equals, and toString methods. This behavior is preferred but is
     * disabled by default to avoid breaking existing consumers.
     *
     * @since 1.1.3
     */
    boolean excludeStaticFields() default false;
}

# Builder Options
Builderberg provides users the ability specify custom options per class that will be used when generating inner builders.

## Usage
To specify builderberg options for a class, you need to simply annotate the class with the [BuilderOptions] annotation.
[BuilderOptions] will be available on any project configured to include builderberg-annotations:

Using gradle:
* compile group: 'com.github.davidburkett', name: 'builderberg-annotations', version: '1.1.1'

##### Example

```
@BuilderOptions(deserializable = DeserializationType.jackson, minimumPluginVersion = 1.1.0, exceptionType = com.example.CustomValidationException.class)
public class ExampleBuilderbergObject {
    private final double field;
}
```

## Available Options
NOTE: You should refer to the [BuilderOptions] implementation of the version you're using for the most accurate list.

    deserializable:
        DESCRIPTION: Generates constructors, annotations, and/or methods to support the specified DeserializationTypes.
        BuilderConstraints will still be enforced on the deserialized objects.
        USAGE: @BuilderOptions(deserializable = {DeserializationType.jackson}
        DEFAULT BEHAVIOR: If unspecified, no deserialization libraries will be supported.
        ACCEPTABLE VALUES: An individual or array of [DeserializationType]s.
            jackson: Creates an allArgsConstructor annotated with @JsonCreator, and each field annotated with @JsonProperty.

    minimumPluginVersion:
        DESCRIPTION: The minimum required version of the builderberg plugin that should be used to generate a builder for the class.
        You will be prevented from generating an inner builder using an older version of the builderberg plugin.
        USAGE: @BuilderOptions(minimumPluginVersion = "1.1.0")
        DEFAULT BEHAVIOR: If unspecified, all builderberg versions will be able to generate an inner builder for the class.
        ACCEPTABLE VALUES: Any string specifying a semantic version (#.#.#).
        
    generateAllArgsConstructor:
        DESCRIPTION: Indicates whether an all-arguments constructor should be generated for the object.
        USAGE: @BuilderOptions(generateAllArgsConstructor = true)
        DEFAULT BEHAVIOR: If unspecified, an all-arguments constructor WILL NOT be generated.
        ACCEPTABLE VALUES: true, false
        
    generateToString:
        DESCRIPTION: Indicates whether a toString method should be generated for the object.
        USAGE: @BuilderOptions(generateToString = true)
        DEFAULT BEHAVIOR: If unspecified, a toString method WILL be generated.
        ACCEPTABLE VALUES: true, false
        
    generateEquals:
        DESCRIPTION: Indicates whether an equals method should be generated for the object.
        USAGE: @BuilderOptions(generateEquals = true)
        DEFAULT BEHAVIOR: If unspecified, an equals method WILL be generated.
        ACCEPTABLE VALUES: true, false
        
    generateHashCode:
        DESCRIPTION: Indicates whether a hashCode method should be generated for the object.
        USAGE: @BuilderOptions(generateHashCode = true)
        DEFAULT BEHAVIOR: If unspecified, a hashCode method WILL be generated.
        ACCEPTABLE VALUES: true, false
        
    generateClone:
        DESCRIPTION: Indicates whether a clone method should be generated for the object. Generated clones create a shallow copy.
        USAGE: @BuilderOptions(generateClone = true)
        DEFAULT BEHAVIOR: If unspecified, a clone method WILL NOT be generated.
        ACCEPTABLE VALUES: true, false
        
    exceptionType:
        DESCRIPTION: The exception/error type to throw when builder constraints are violated.
        USAGE: @BuilderOptions(exceptionType = com.example.CustomValidationException.class)
        DEFAULT BEHAVIOR: If unspecified, violating constraints will result in an java.lang.AssertionError being thrown.
        ACCEPTABLE VALUES: Any class that extends Throwable and has a constructor that takes in a string as its only parameter.

    makeCollectionsImmutable:
        DESCRIPTION: Indicates whether a hashCode method should be generated for the object.
        USAGE: @BuilderOptions(makeCollectionsImmutable = true)
        DEFAULT BEHAVIOR: If unspecified, collections will not be made immutable.
        ACCEPTABLE VALUES: true, false
        CAVEATS: This only applies to fields of the below types. Derived classes and specific implementations (like ArrayList) are not supported.
            java.util.Collection - uses java.util.Collections.unmodifiableCollection
            java.util.List - uses java.util.Collections.unmodifiableList
            java.util.Set - uses java.util.Collections.unmodifiableSet
            java.util.SortedSet - uses java.util.Collections.unmodifiableSortedSet
            java.util.NavigableSet - uses java.util.Collections.unmodifiableNavigableSet
            java.util.Map - uses java.util.Collections.unmodifiableMap
            java.util.SortedMap - uses java.util.Collections.unmodifiableSortedMap
            java.util.NavigableMap - uses java.util.Collections.unmodifiableNavigableMap

[BuilderOptions]: https://github.com/DavidBurkett/Builderberg/blob/master/builderberg-annotations/src/main/java/com/github/davidburkett/builderberg/annotations/BuilderOptions.java
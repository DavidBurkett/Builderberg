## Builder Constraints
Builderberg provides users the ability specify field constraints that will be validated when an object is being built.
This will guarantee that the values of the fields on built objects will meet the specified constraints.

### Usage
To specify a constraint for a field, you need to simply annotate the field with the [BuilderConstraint] annotation.
BuilderConstraint will be available on any project configured to include builderberg-annotations:

Using gradle:
* compile group: 'com.github.davidburkett', name: 'builderberg-annotations', version: '1.1.1'

##### Example

```
public class ExampleBuilderbergObject {
    @BuilderConstraint(minValue = 10.0, maxValue = 45.0)
    private final double fieldWithMinAndMax;
}
```

### Available Constraints
NOTE: You should refer to the [BuilderConstraint] implementation of the version you're using for the most accurate list.

    notNull:
        Guarantees that the object will not be null.
        Usage: @BuilderConstraint(notNull = true)
        Allowed field types: Any non-primitive objects
        
    notEmpty:
        Guarantees that the string, collection or map will not be null or empty.
        Usage: @BuilderConstraint(notEmpty = true)
        Allowed field types: java.lang.String, java.util.Collection, or java.util.Map
        
    notBlank:
        Guarantees that the string will not be null, empty, or blank.
        Usage: @BuilderConstraint(notBlank = true)
        Allowed field types: java.lang.String
        
    noNullKeys:
        Guarantees that the map keyset will not contain any null keys.
        Usage: @BuilderConstraint(noNullKeys = true)
        Allowed field types: java.util.Map
        
    noNullValues:
        Guarantees that the collection or map will not contain any null values.
        Usage: @BuilderConstraint(noNullValues = true)
        Allowed field types: java.util.Collection, java.util.Map
        
    notNegative:
        Guarantees that the numeric value is not less than zero.
        Usage: @BuilderConstraint(notNegative = true)
        Allowed field types: All numeric primitives, and their equivalent boxed forms.
        
    notPositive:
        Guarantees that the numeric value is not greater than zero.
        Usage: @BuilderConstraint(notPositive = true)
        Allowed field types: All numeric primitives, and their equivalent boxed forms.
        
    negativeOnly:
        Guarantees that the numeric value is less than zero.
        Usage: @BuilderConstraint(negativeOnly = true)
        Allowed field types: All numeric primitives, and their equivalent boxed forms.
        
    positiveOnly:
        Guarantees that the numeric value is greater than zero.
        Usage: @BuilderConstraint(positiveOnly = true)
        Allowed field types: All numeric primitives, and their equivalent boxed forms.
        
    minValue:
        Guarantees that the numeric value is not less than the specified value.
        Usage: @BuilderConstraint(minValue = 12.0)
        Allowed field types: All numeric primitives, and their equivalent boxed forms.
        
    maxValue:
        Guarantees that the numeric value is not greater than the specified value.
        Usage: @BuilderConstraint(maxValue = 12.0)
        Allowed field types: All numeric primitives, and their equivalent boxed forms.

[BuilderConstraint]: https://github.com/DavidBurkett/Builderberg/blob/master/builderberg-annotations/src/main/java/com/github/davidburkett/builderberg/annotations/BuilderConstraint.java
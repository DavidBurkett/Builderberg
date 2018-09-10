## Builderberg - Inner Builder Generator

Generates an inner builder for plain old data classes using a builder pattern similar to the one proposed by Joshua Bloch in "Effective Java"[1]. 
Using this builder turns a field-only class into an immutable class that can only be constructed by using the generated inner builder.
In addition to providing immutable objects, Builderberg also provides:
1. Support for field constraint validation with customizable exception types.
2. The ability to customize which Object methods are overloaded. (toString, hashCode, clone, etc.)
3. Opt-in support for Jackson deserialization. (support for other libraries coming soon)
4. Support for generic objects.
5. Generated javadoc for getter and with/setter methods clearly documenting each field's allowable values.
6. And much much more.


[1] http://thefinestartist.com/effective-java/02

### Installation Instructions
##### Plugin
From IntelliJ IDEA, go to preferences > plugins.
Click the "Browse Repositories" button.
Search for "Builderberg" and install the plugin.

##### BuilderConstraint Annotations
If you're looking to leverage the power of BuilderConstraints (notNull, notEmpty, notBlank, notNull, notNegative, etc.), BuilderOptions (deserializable, minimumPluginVersion, exceptionType, generateClone, etc.), or CustomLogic you'll need to include the builderberg-annotations module in your projects.
Using gradle:
* compile group: 'com.github.davidburkett', name: 'builderberg-annotations', version: '1.1.1'

### Usage

##### Basic Usage
To use the builder with no advanced features:
1. Create your class and add the fields you want it to contain.
2. Right-click on the file and select "Generate Inner Builder". Alternatively, you can use the shortcut "Shift Alt B".
This will generate the inner builder class, all of the getters, constructors, setters, etc to make it usable.
3. A populated instance of the object can be obtained as follows: 
* final ClassName immutableObject = ClassName.builder().withField1(field1Value).withField2(field2Value).build();

##### Builder Constraints
For instructions on using field constraints, see 'docs/BuilderConstraints.md'

##### Builder Options
For advanced builder options, see 'docs/BuilderOptions.md'

### Example

##### Before
```
@BuilderOptions(generateAllArgsConstructor = true, makeCollectionsImmutable = true)
public class ExampleObject {
    /**
     * Integer field
     */
    @BuilderConstraint(notNegative = true)
    private final int field1;

    /**
     * String field
     */
    @BuilderConstraint(notBlank = true)
    private final String field2;
}
```

##### After
```
@BuilderOptions(generateAllArgsConstructor = true, makeCollectionsImmutable = true)
public class ExampleObject {
    /**
     * Integer field
     */
    @BuilderConstraint(notNegative = true)
    private final int field1;

    /**
     * String field
     */
    @BuilderConstraint(notBlank = true)
    private final String field2;

    private ExampleObject(final Builder builder) {
        builder.validate();
        this.field1 = builder.field1;
        this.field2 = builder.field2;
    }

    public ExampleObject(final int field1, final String field2) {
        this(builder().withField1(field1).withField2(field2));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return Integer field
     * Guaranteed to not be negative.
     */
    public int getField1() {
        return field1;
    }

    /**
     * @return String field
     * Guaranteed to not be null, empty, or blank.
     */
    public String getField2() {
        return field2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{\"field1\":" + field1 + ",\"field2\":\"" + field2 + "\"}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + 0;
        result = 31 * result + field2.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExampleObject)) {
            return false;
        }
        final ExampleObject obj = (ExampleObject) o;
        if (field1 != obj.field1) {
            return false;
        }
        if (!(field2 == obj.field2 || (field2 != null && field2.equals(obj.field2)))) {
            return false;
        }
        return true;
    }

    public static final class Builder {
        private int field1;
        private String field2;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder withField1(final int field1) {
            if (!(field1 >= 0)) {
                throw new AssertionError("Constraint not met: field1 >= 0");
            }
            this.field1 = field1;
            return this;
        }

        public Builder withField2(final String field2) {
            if (!(!field2.trim().isEmpty())) {
                throw new AssertionError("Constraint not met: !field2.trim().isEmpty()");
            }
            this.field2 = field2;
            return this;
        }

        public ExampleObject build() {
            return new ExampleObject(this);
        }

        private void validate() {
            if (!(field1 >= 0)) {
                throw new AssertionError("Constraint not met: field1 >= 0");
            }
            if (!(!field2.trim().isEmpty())) {
                throw new AssertionError("Constraint not met: !field2.trim().isEmpty()");
            }
        }
    }
}
```
 ##### Usage:
 `final ExampleObject instance = ExampleObject.builder().withField1(7).withField2("SomeString").build();`
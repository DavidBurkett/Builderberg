## Builderberg - USAGE
### Example:
#### Before:

```
public class PlainOldObject {
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

#### After:
```
public class PlainOldObject {
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

    private PlainOldObject(final Builder builder) {
        builder.validate();
        this.field1 = builder.field1;
        this.field2 = builder.field2;
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
        return "{\"field1\": \"" + field1 + "\",\"field2\": \"" + field2 + "\"}";
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlainOldObject)) {
            return false;
        }
        final PlainOldObject obj = (PlainOldObject) o;
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
            assert (field1 >= 0);
            this.field1 = field1;
            return this;
        }

        public Builder withField2(final String field2) {
            assert (!field2.trim().isEmpty());
            this.field2 = field2;
            return this;
        }

        public PlainOldObject build() {
            return new PlainOldObject(this);
        }

        private void validate() {
            assert (field1 >= 0);
            assert (!field2.trim().isEmpty());
        }
    }
}
 ```

 #### Usage:
 `final PlainOldObject instance = PlainOldObject.builder().withField1(7).withField2("SomeString").build();`
###Builderberg - Inner Builder Generator

Generates an inner builder for plain old data classes using a builder pattern similar to the one proposed by Joshua Bloch in "Effective Java"[1]. Using this builder turns a field-only class into an immutable class that can only be constructed by using the generated inner builder.

_TODO: Explain the benefits of the builder pattern._

[1] http://thefinestartist.com/effective-java/02

_TODO: Add installation instructions_

#### Project Status:
The project has not yet been officially released, but does produce working output. I will not be releasing the plugin until the following features are implemented:
* The generated toString() method produces fully-formed JSON for all data types
* hashCode(), equals(), and clone() methods are generated
* More thorough javadoc is generated
* Validation criteria can be specified and enforced for each field (Example: @NotNull)

#### Example:
##### Before:

`public class PlainOldObject {
    /**
     * Integer field
     */
    private int field1;
    
    /**
     * String field
     */
    private String field2;
}`

##### After:
`public class PlainOldObject {
     /**
      * Integer field
      */
     private final int field1;
 
     /**
      * String field
      */
     private final String field2;
 
     private PlainOldObject(final Builder builder) {
         this.field1 = builder.field1;
         this.field2 = builder.field2;
     }
 
     /**
      * @return Integer field
      */
     public int getField1() {
         return field1;
     }
 
     /**
      * @return String field
      */
     public String getField2() {
         return field2;
     }
 
     @Override public java.lang.String toString() {
         return "{\"field1\": \"" + field1 + "\",\"field2\": \"" + field2 + "\"}";
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
             this.field1 = field1;
             return this;
         }
 
         public Builder withField2(final String field2) {
             this.field2 = field2;
             return this;
         }
 
         public PlainOldObject build() {
             return new PlainOldObject(this);
         }
     }
 }`
 
 ##### Usage:
 `final PlainOldObject instance = PlainOldObject.Builder.create().withField1(7).withField2("SomeString").build();`
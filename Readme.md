## Builderberg - Inner Builder Generator

Generates an inner builder for plain old data classes using a builder pattern similar to the one proposed by Joshua Bloch in "Effective Java"[1]. Using this builder turns a field-only class into an immutable class that can only be constructed by using the generated inner builder.

[1] http://thefinestartist.com/effective-java/02

### Installation Instructions
#### Plugin
From IntelliJ IDEA, go to preferences > plugins.
Click the "Browse Repositories" button.
Search for "Builderberg" and install the plugin.

#### BuilderConstraint Annotations
If you're looking to leverage the power of BuilderConstraints (notNull, notEmpty, notBlank, notNull, notNegative, etc.), BuilderOptions (deserializable, minimumPluginVersion, exceptionType, generateClone, etc.), or CustomLogic you'll need to include the builderberg-annotations module in your projects.
Using gradle:
* compile group: 'com.github.davidburkett', name: 'builderberg-annotations', version: '1.1.1'

### Usage:
For basic usage, see 'docs/Usage.md'
For advanced builder options, see 'docs/BuilderOptions.md'
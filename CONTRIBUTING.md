# Contributing to Documented DFU

Contributions are always welcome! I cannot do this all by myself, after all.

## Code Style

DFU uses the IDEA default code style, with some modifications to import order and the removal of black lines before the 
package declaration. You are encouraged to format doc comments, but make sure that no code is modified.

This bears repeating: _Code must not be modified._

## General Documentation

Doc comments should have at least one sentence. Paragraphs are allowed, but not required. Every doctag should be 
sentence-like, i.e. begin with a captial letter and end with terminal punctuation.

Good:
```java
/**
 * Computes a thing using the given value.
 *
 * @param foo The value.
 * @return Zero on success, or a nonzero value if an error occurs.
 */
int computeThing(int foo);
```

Bad:
```java
/**
 * computes a thing using the given value
 *
 * @param foo the value
 * @return zero on success, or a nonzero value if an error occurs
 */
int computeThing(int foo);
```

Doc comments should be written in US English. When documenting advanced concepts, use the accepted terminology
(such as "optic" or "profunctor"), but describe it in terms of more relatable concepts.

### Access Levels

All `public` and `protected` members should be documented. Package-private and `private` members may optionally be
documented.

### Doctags

General doctags should be used according to [this post](https://blog.codefx.org/java/new-javadoc-tags/).

- The doc comment body should be used for API spcification
- `@apiNote` should be used to describe API quirks or nonintuitive API elements
- `@implSpec` should be used to describe default or implementations.
- `@implNote` should be used to describe implementation quirks or bugs.

`@deprecated` tags should only be used with elements marked a `@Deprecated` in code. Related elements should be
linked to using `@link` inline tags or with `@see` block tags. Confusing or advanced concepts, such as functors
can contain external links in `@see` tags to outside reference.

Example (from `DynamicOps<T>`):
```java
/**
 * Performs a reduction operation on the map entries extracted from the input. Specifically, this method
 * performs a left fold on the entries extracted from the input.
 *
 * @param input    The serialized value.
 * @param empty    An empty or identity element of the result type.
 * @param combiner Combines the current serialized key, value, and previous result to produce a new result.
 * @param <R>      The result type.
 * @return A {@link DataResult} containing the final result.
 * @implSpec The default implementation extracts the entries using {@link #getMapValues(Object)}, then
 * accumulates the final result value by iterating over the stream in encounter order.
 * @see Stream#reduce(Object, BiFunction, BinaryOperator)
 * @see <a href="https://en.wikipedia.org/wiki/Fold_(higher-order_function)">The fold higher order function</a>
 */
default <R> DataResult<R> readMap(final T input, final DataResult<R> empty, final Function3<R, T, T, DataResult<R>> combiner) {
    return getMapValues(input).flatMap(stream -> {
        // TODO: AtomicReference.getPlain/setPlain in java9+
        final MutableObject<DataResult<R>> result = new MutableObject<>(empty);
        stream.forEach(p -> result.setValue(result.getValue().flatMap(r -> combiner.apply(r, p.getFirst(), p.getSecond()))));
        return result.getValue();
    });
}
```

## Type Documentation

Class and interface documentation should contain the main comment body as well as `@param` tags for any type parameters.
Every type should have a doc comment.

Example:
```java
/**
 * An adapter for a hierarchical serialization format. Clients may use this interface to
 * interact with serialization formats such as JSON and NBT without knowing the specific
 * serialization format being used.
 *
 * @param <T> The type this interface serializes to and deserializes from. For example,
 *            {@link com.google.gson.JsonElement} or NbtTag.
 * @see Dynamic
 * @see Codec
 */
public class DynamicOps<T> {
}
```

### Type Shapes

Some types in DFU should be displayed using a custom rendering. For example, the type `FunctionType<A, B>` is
rendered as `(A) -> B` in the generated documentation. For appropriate types, insert a block-level `@dfu.shape`
tag so that the type is rendered properly.

For reference on the format of `@dfu.shape` template strings, [see the wiki](https://github.com/kvverti/Documented-DataFixerUpper/wiki/Custom-type-shapes-with-@dfu.shape).

## Method Documentation

Methods should be fully documented with `@param` and `@return` tags, except for "simple properties". Throws clauses
should always be documented using `@throws` tags.

Example:
```java
/**
 * Serializes a list of values, in the form of a {@link Stream} to the serialized type.
 *
 * @param input The elements to serialize.
 * @return The serialized value.
 */
T createList(Stream<T> input);

// in this case, the method is a simple property, so this is also appropriate

/**
 * Serializes a list of values, in the form of a {@link Stream} to the serialized type.
 */
T createList(Stream<T> input);
```

Default methods should additionally contain an `@implSpec` tag describing the default implementation, unless the
method API is sufficiently constrained as not to permit multiple implementations.

Method overrides do not need a doc comment unless they strengthen the contract of the overridden method. Method
overrides may inherit the main documentation using `@inheritDoc`, but this is not required.

## Constructor Documentation

Constructors should be documented like methods, except that because constructors are not inherited, all constructors
require a doc comment. If a constructor does nothing special, then the doc comment can be as simple as
"Constructs a Foo."

```java
/**
 * Constructs a {@link Foo} with the default settings.
 *
 * @see #Foo(int, int, String)
 */
public Foo() {
}
```

## Field Documentation

Fields should be documented with a standard doc comment. Constraints and invariants on the values of the field
should be specified in the doc comment.

## Package Documentation

This is the only time when code changes are allowed. Package documentation should go in `package-info.java` as is
conventional. Package documentation may be as short as one or two sentences, but for packages relating to advanced
concepts (such as kinds or profunctors), several paragraphs may be warranted.

## Duplicate or Similar Elements

Duplicate or similar members should all receive separate doc comments. These doc comments may be similar or identical.
For example, the 16 `group` methods in `Kind1` should all receive a doc comment, but these comments may be identical.

## Pull Requests

Pull requests should not contain too many documented members. If a type has more than five methods, consider requesting
a pull for that type by itself instead of incorporating it into a larger pull request.

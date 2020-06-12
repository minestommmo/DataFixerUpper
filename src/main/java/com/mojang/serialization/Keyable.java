package com.mojang.serialization;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A definer or acceptor of serialized keys.
 *
 * <p>Types implementing this interface define a set of valid keys. Typically, these keys are serialized to or
 * extracted from some serialized object.
 *
 * @see MapCodec
 */
public interface Keyable {
    /**
     * Returns the set of keys this object defines or accepts, serialized to the provided form.
     *
     * @param ops The {@link DynamicOps} instance defining the serialized form.
     * @param <T> The type of the serialized form.
     * @return The set of keys this object defines.
     * @implSpec The returned stream should be finite and provide distinct elements. The stream, furthermore, should
     * not already be consumed (that is, a new stream should be created each time this method is called).
     */
    <T> Stream<T> keys(DynamicOps<T> ops);

    /**
     * Returns a {@link Keyable} that defines the keys supplied by the argument.
     *
     * <p>The supplier must return a fresh stream on each invocation. As well, care should be taken that the
     * source backing the returned stream is not modified or otherwise invalidated, because the returned
     * {@link Keyable} does not store a local copy of the keys. <strong>The caller is responsible for making
     * a defensive copy of the backing source, if one is required.</strong>
     *
     * @param keys A supplier of key streams. A fresh stream should be returned on each invocation.
     * @return A {@link Keyable} for the given keys.
     */
    static Keyable forStrings(final Supplier<Stream<String>> keys) {
        return new Keyable() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return keys.get().map(ops::createString);
            }
        };
    }
}

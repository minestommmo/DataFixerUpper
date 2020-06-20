// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

/**
 * An unmodifiable store for serialized key-value pairs. This interface can be used when access to and iteration over
 * serialized key-value pairs is necessary, but other aspects of Java maps, such as mutability, are unnecessary.
 *
 * @param <T> The type of the serialized form.
 */
public interface MapLike<T> {
    /**
     * Retrieves the value corresponding to the given key, or {@code null} if there is no mapping for the given key.
     * Note that, unlike {@link Map#get(Object)}, a return value of {@code null} should only occur if this map
     * truly does not contain a mapping.
     *
     * @param key The serialized key.
     * @return The value associated with the key, or {@code null} if the entry does not exist.
     */
    @Nullable
    T get(final T key);

    /**
     * Retrieves the value corresponding to the given key, or {@code null} if there is no mapping for the given key.
     * Note that, unlike {@link Map#get(Object)}, a return value of {@code null} should only occur if this map
     * truly does not contain a mapping.
     *
     * <p>The argument should be the {@link String} representation of a serialized key.
     *
     * @param key The string key.
     * @return The value associated with the key, or {@code null} if the entry does not exist.
     */
    @Nullable
    T get(final String key);

    /**
     * Returns a fresh {@link Stream} of all the entries in this map.
     */
    Stream<Pair<T, T>> entries();

    /**
     * Creates a {@link MapLike} containing the entries of the given {@link Map}.
     *
     * <p>The map is not defensively copied, so modifications to the map are reflected in the returned {@link MapLike}.
     *
     * @param map The map to wrap.
     * @param ops A {@link DynamicOps} instance defining the serialized form.
     * @param <T> The type of the serialized form.
     * @return A {@link MapLike} containing the entries of the given map.
     */
    static <T> MapLike<T> forMap(final Map<T, T> map, final DynamicOps<T> ops) {
        return new MapLike<T>() {
            @Nullable
            @Override
            public T get(final T key) {
                return map.get(key);
            }

            @Nullable
            @Override
            public T get(final String key) {
                return get(ops.createString(key));
            }

            @Override
            public Stream<Pair<T, T>> entries() {
                return map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + map + "]";
            }
        };
    }
}

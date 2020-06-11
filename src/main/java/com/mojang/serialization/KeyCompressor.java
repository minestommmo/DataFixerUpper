// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.stream.Stream;

/**
 * An immutable collection of indexed, string-convertible keys. When serializing and deserializing maps with
 * {@linkplain DynamicOps#compressMaps() compressed keys}, a key compressor is used to associate an integer
 * index with each possible key.
 *
 * @param <T> The type of the serialized form.
 * @implNote The implementation uses two hash tables to associate contiguous indices with keys in order to ensure
 * both compression and decompression run in constant time. Using a single array of {@code T[this.size()]} is more
 * straightforward, but incurs linear run time for compression.
 * @see DynamicOps#compressMaps()
 */
public final class KeyCompressor<T> {
    private final Int2ObjectMap<T> decompress = new Int2ObjectArrayMap<>();
    private final Object2IntMap<T> compress = new Object2IntArrayMap<>();
    private final Object2IntMap<String> compressString = new Object2IntArrayMap<>();
    private final int size;
    private final DynamicOps<T> ops;

    /**
     * Constructs a new key compressor for the given key stream. If a key appears in the key stream more
     * than once, the second and subsequent appearances are ignored.
     *
     * <p>The key stream is consumed in the constructor. It should be finite, or else this constructor
     * will run forever.
     *
     * @param ops       The {@link DynamicOps} instance defining the serialized form.
     * @param keyStream A stream of keys this key compressor compresses.
     */
    public KeyCompressor(final DynamicOps<T> ops, final Stream<T> keyStream) {
        this.ops = ops;

        compressString.defaultReturnValue(-1);

        keyStream.forEach(key -> {
            if (compress.containsKey(key)) {
                return;
            }
            final int next = compress.size();
            compress.put(key, next);
            ops.getStringValue(key).result().ifPresent(k ->
                compressString.put(k, next)
            );
            decompress.put(next, key);
        });

        size = compress.size();
    }

    /**
     * Returns the key associated with the given key index, or {@code null} if the key index does not
     * correspond to a key.
     *
     * <p><strong>Caution:</strong> This is one of the few methods in DFU that may return {@code null}.
     *
     * @param key The key index.
     * @return The key associated with the key index, or {@code null} if the key index is invalid.
     * @implNote This method incurs one lookup and runs in constant time.
     */
    public T decompress(final int key) {
        return decompress.get(key);
    }

    /**
     * Returns the key index associated with the key the given string represents. If the argument does not
     * represent a valid key, {@code -1} is returned.
     *
     * @param key The string representation of a key.
     * @return The index associated with that key, or {@code null} if the key does not exist.
     * @implNote This method incurs at most two lookups and runs in constant time.
     * @see #compress(Object)
     */
    public int compress(final String key) {
        final int id = compressString.getInt(key);
        return id == -1 ? compress(ops.createString(key)) : id;
    }

    /**
     * Returns the key index associated with the given key. If the argument is not a valid key, {@code -1} is returned.
     *
     * @param key The key.
     * @return The key index associated with the key, or {@code -1} if the key is invalid.
     * @implNote This method incurs one lookup and runs in constant time.
     */
    public int compress(final T key) {
        return compress.get(key);
    }

    /**
     * The size of the key set represented by this key compressor.
     */
    public int size() {
        return size;
    }
}

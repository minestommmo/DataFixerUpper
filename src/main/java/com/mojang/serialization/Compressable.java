// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

/**
 * A {@link Keyable} that furthermore supports key compression.
 *
 * @see KeyCompressor
 */
public interface Compressable extends Keyable {
    /**
     * Returns the {@link KeyCompressor} used to compress keys in this object's key set for the given serialized form.
     *
     * @param ops The {@link DynamicOps} instance defining the serialized form.
     * @param <T> The type of the serialized form.
     * @return A {@link KeyCompressor} for this object's keys.
     */
    <T> KeyCompressor<T> compressor(final DynamicOps<T> ops);
}

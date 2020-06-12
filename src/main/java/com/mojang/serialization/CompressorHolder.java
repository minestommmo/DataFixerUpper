// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Map;

/**
 * Abstract base class for {@link Compressable} objects. This class provides a default implementation of the method
 * {@link #compressor(DynamicOps)}.
 *
 * @implNote This class internally caches {@link KeyCompressor} objects generated from different serialization forms.
 */
public abstract class CompressorHolder implements Compressable {
    private final Map<DynamicOps<?>, KeyCompressor<?>> compressors = new Object2ObjectArrayMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> KeyCompressor<T> compressor(final DynamicOps<T> ops) {
        return (KeyCompressor<T>) compressors.computeIfAbsent(ops, k -> new KeyCompressor<>(ops, keys(ops)));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.util;

/**
 * The unit type with a single value, {@link #INSTANCE}.
 *
 * <p>The unit type is useful for representing empty values or
 * operations with no results, similar to the {@code void} type for method return values.
 */
public enum Unit {
    /**
     * The singleton unit value.
     */
    INSTANCE;

    @Override
    public String toString() {
        return "Unit";
    }
}

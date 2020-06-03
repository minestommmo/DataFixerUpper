// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

/**
 * A marker interface representing a unary type constructor, also called a kind.
 *
 * <p>A type constructor may be thought of as an unapplied parameterized type. For example, the parameterized
 * type {@code List<String>} is an application of the type constructor {@code List<_>} on the type
 * {@code String}. In DFU, application of a type constructor is done using {@link App}.
 *
 * @see App
 * @see K2
 * @see <a href="https://en.wikipedia.org/wiki/Kind_(type_theory)">Kind (Type Theory)</a>
 */
public interface K1 {
}

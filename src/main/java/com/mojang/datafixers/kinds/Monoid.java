// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * The monoid type class defines the {@link #add(Object, Object)} method for combining two objects.
 *
 * @param <T> The object type that this monoid instance handles.
 * @dfu.shape %.Type. %0
 * @see <a href="https://en.wikipedia.org/wiki/Monoid">The monoid algebraic structure / type class</a>
 */
public interface Monoid<T> {
    /**
     * Returns the identity element of this monoid.
     *
     * @return The identity element.
     * @see #add(Object, Object)
     */
    T point();

    /**
     * Combines {@code first} and {@code second} together.
     *
     * <p>When the identity element is combined with any other object,
     * the result is the other input parameter.
     *
     * @param first  The first input.
     * @param second The second input.
     * @return The combined result.
     */
    T add(final T first, final T second);

    /**
     * Creates a monoid instance for {@link List}.
     *
     * @param <T> The element type of the list.
     * @return The list monoid.
     */
    static <T> Monoid<List<T>> listMonoid() {
        // TODO: immutable list with structural sharing
        return new Monoid<List<T>>() {
            @Override
            public List<T> point() {
                return ImmutableList.of();
            }

            @Override
            public List<T> add(final List<T> first, final List<T> second) {
                final ImmutableList.Builder<T> builder = ImmutableList.builder();
                builder.addAll(first);
                builder.addAll(second);
                return builder.build();
            }
        };
    }
}

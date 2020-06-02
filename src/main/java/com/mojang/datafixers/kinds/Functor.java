// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.function.Function;

/**
 * The functor type class defines one method, {@link #map(Function, App)},
 * which transforms the contents of a container to another type.
 *
 * @param <F>  The container type.
 * @param <Mu> The witness type of this functor.
 * @see <a href="https://en.wikipedia.org/wiki/Functor_(functional_programming)">The functor type class</a>
 */
public interface Functor<F extends K1, Mu extends Functor.Mu> extends Kind1<F, Mu> {
    /**
     * Unboxes an {@link App} representing a functor into a functor.
     *
     * @param proofBox The boxed functor.
     * @param <F>      The container type.
     * @param <Mu>     The witness type of the functor.
     * @return The unboxed functor.
     * @throws ClassCastException If {@code proofBox} is not a functor.
     */
    static <F extends K1, Mu extends Functor.Mu> Functor<F, Mu> unbox(final App<Mu, F> proofBox) {
        return (Functor<F, Mu>) proofBox;
    }

    /**
     * The witness type of a functor.
     */
    interface Mu extends Kind1.Mu {}

    /**
     * Maps the contents of {@code ts} from {@code T} to {@code R} using the {@code func}.
     *
     * @param func The transformation function.
     * @param ts   The input container that will be transformed.
     * @param <T>  The input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    <T, R> App<F, R> map(final Function<? super T, ? extends R> func, final App<F, T> ts);
}

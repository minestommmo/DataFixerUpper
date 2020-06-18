// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.mojang.datafixers.FunctionType;

/**
 * A {@link Functor} that can convert values to and from a function.
 *
 * @param <T>  The container type.
 * @param <C>  The input type of the functions.
 * @param <Mu> The witness type of this functor.
 * @dfu.shape %.Type. %0
 */
public interface Representable<T extends K1, C, Mu extends Representable.Mu> extends Functor<T, Mu> {
    /**
     * Thunk method that casts an applied {@link Representable.Mu} to a {@link Representable}.
     *
     * @param proofBox The boxed {@link Representable}.
     * @param <F>      The container type.
     * @param <C>      The function input type.
     * @param <Mu>     The witness type of the given functor.
     * @return The unboxed {@link Representable}.
     */
    static <F extends K1, C, Mu extends Representable.Mu> Representable<F, C, Mu> unbox(final App<Mu, F> proofBox) {
        return (Representable<F, C, Mu>) proofBox;
    }

    /**
     * The witness type of a {@link Representable}.
     *
     * @dfu.shape %.Mu. %^1
     */
    interface Mu extends Functor.Mu {}

    /**
     * Converts the given container to a function.
     *
     * @param input The container.
     * @param <A>   The type of contained value.
     * @return A function from the input type to the contained value type.
     */
    <A> App<FunctionType.ReaderMu<C>, A> to(final App<T, A> input);

    /**
     * Converts a function to a value of the container type.
     *
     * @param input A function from the input type to the contained value type.
     * @param <A>   The type of contained value.
     * @return A container.
     */
    <A> App<T, A> from(final App<FunctionType.ReaderMu<C>, A> input);
}

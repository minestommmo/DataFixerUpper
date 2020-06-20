// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.kinds.Kind2;

import java.util.function.Function;

/**
 * A type class that defines a method {@link #distribute(App, App2)} that wraps the input and output of a profunctor
 * into an effect defined via a {@linkplain com.mojang.datafixers.kinds.Functor functor}.
 *
 * <p>Note that this interface does <em>not</em> extend {@link Profunctor}.
 *
 * @param <T>  The functor type.
 * @param <P>  The profunctor type.
 * @param <Mu> The witness type for this {@link FunctorProfunctor}.
 * @dfu.shape %.Type.[%0] %1
 * @see Profunctor
 * @see com.mojang.datafixers.kinds.Functor
 */
public interface FunctorProfunctor<T extends K1, P extends K2, Mu extends FunctorProfunctor.Mu<T>> extends Kind2<P, Mu> {
    /**
     * Thunk method that casts an applied {@link FunctorProfunctor.Mu} to a {@link FunctorProfunctor}.
     *
     * @param proofBox The boxed {@link FunctorProfunctor}.
     * @param <P>      The transformation type.
     * @param <Mu>     The witness type for the profunctor.
     * @return The unboxed {@link FunctorProfunctor}.
     */
    static <T extends K1, P extends K2, Mu extends FunctorProfunctor.Mu<T>> FunctorProfunctor<T, P, Mu> unbox(final App<Mu, P> proofBox) {
        return (FunctorProfunctor<T, P, Mu>) proofBox;
    }

    /**
     * The witness type for {@link FunctorProfunctor}.
     *
     * @dfu.shape %.Mu.[%0] %^1
     */
    interface Mu<T extends K1> extends Kind2.Mu {}

    /**
     * Wraps the input and output of the given transformation in the given functor.
     *
     * <p>This method is equivalent to {@link com.mojang.datafixers.kinds.Functor#map(Function, App)}, but generalized
     * to operate on any {@link Profunctor}.
     *
     * @param proof The type class instance for the desired functor.
     * @param input The transformation.
     * @param <A>   The input type of the transformation.
     * @param <B>   The output type of the transformation.
     * @param <F>   The type of functor.
     * @return A transformation between types wrapped in the given functor.
     * @apiNote This method <em>distributes</em> the functor {@code F} across the arguments of the profunctor {@code P},
     * hence the name {@code distribute}.
     * @see com.mojang.datafixers.kinds.Functor#map(Function, App)
     */
    <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(final App<? extends T, F> proof, final App2<P, A, B> input);
}

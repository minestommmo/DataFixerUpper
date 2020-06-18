// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.function.Function;

/**
 * The traversable type class for some container type takes in an effectful transformation
 * and produces an equivalent effectful transformation on the container type.
 *
 * <p>This type class may be used to e.g. produce a function {@code ListBox<A> -> DataResult<ListBox<B>>}
 * from a function {@code A -> DataResult<B>}, only producing a successful result if all the original
 * elements were converted successfully.
 *
 * <p>Note that the implementation of {@link #map(Function, App)} inherited from {@link Functor} can be implemented
 * in terms of {@link #traverse(Applicative, Function, App)} as {@code map(f, ta) = traverse(IdF, f, ta)}. This
 * interface leaves that method abstract because of the differing function types in {@link #map(Function, App)}
 * and {@link #traverse(Applicative, Function, App)} that make the default implementation outline above impossible
 * without an unchecked (albeit safe) cast.
 *
 * @param <T>  The container type.
 * @param <Mu> The witness type for this traversable.
 * @dfu.shape %.Type. %0
 * @see Applicative
 */
public interface Traversable<T extends K1, Mu extends Traversable.Mu> extends Functor<T, Mu> {
    /**
     * Thunk method that casts an applied {@link Traversable.Mu} into a {@link Traversable}.
     *
     * @param proofBox The boxed value.
     * @param <F>      The container type.
     * @param <Mu>     The witness type.
     * @return The casted {@link Traversable}.
     */
    static <F extends K1, Mu extends Traversable.Mu> Traversable<F, Mu> unbox(final App<Mu, F> proofBox) {
        return (Traversable<F, Mu>) proofBox;
    }

    /**
     * The witness type for {@link Traversable}.
     *
     * @dfu.shape %.Mu. %^1
     */
    interface Mu extends Functor.Mu {}

    /**
     * Applies a function that produces an {@link Applicative} effect to each value contained within
     * {@code input}, then builds a container with an equivalent structure containing the (unboxed) results.
     *
     * @param applicative An instance of the {@link Applicative} type class which defines the behavior of {@code F}.
     * @param function    The function to apply.
     * @param input       The input container.
     * @param <F>         The type of the effect.
     * @param <A>         The input type.
     * @param <B>         The output type.
     * @return A container holding the results of applying {@code function} to each element, if the function was
     * successful for every element.
     * @apiNote This function defines the <em>traversable operator</em> with the applicative functor {@code F}.
     */
    <F extends K1, A, B> App<F, App<T, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<T, A> input);

    /**
     * Swaps the order of this container with a nested container in {@code input}.
     *
     * <p>This function is equivalent to
     *
     * <pre><code>
     * traverse(applicative, Function.identity(), input)</code></pre>
     *
     * @param applicative An instance of the {@link Applicative} type class which defines the behavior of {@code F}.
     * @param input       The input container.
     * @param <F>         The nested container type.
     * @param <A>         The contained type.
     * @return The nested contained value with the containers swapped.
     * @see #traverse(Applicative, Function, App)
     */
    default <F extends K1, A> App<F, App<T, A>> flip(final Applicative<F, ?> applicative, final App<T, App<F, A>> input) {
        return traverse(applicative, Function.identity(), input);
    }
}

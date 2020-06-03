// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.kinds.Kind2;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The profunctor type class defines a method, {@link #dimap(Function, Function)}, which transforms a mapping
 * between types to a mapping between wrapped types.
 *
 * <p>This type class requires that the container {@code P} is contravariant in its first parameter.
 *
 * @param <P>  The container type.
 * @param <Mu> The witness type for this profunctor.
 * @dfu.shape %.Type. %0
 * @see <a href="https://typeclasses.com/profunctors">Understanding profunctors</a>
 */
public interface Profunctor<P extends K2, Mu extends Profunctor.Mu> extends Kind2<P, Mu> {
    /**
     * The witness type of a {@link Profunctor}.
     *
     * @dfu.shape %.Mu. %^1
     * @see #TYPE_TOKEN
     */
    interface Mu extends Kind2.Mu {
        /**
         * The value representing the witness type {@link Mu}.
         */
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<Mu>() {};
    }

    /**
     * Thunk method that casts an applied {@link Profunctor.Mu} to a {@link Profunctor}.
     *
     * @param proofBox The boxed profunctor.
     * @param <P>      The container type.
     * @param <Proof>  The witness type.
     * @return The unboxed profunctor.
     */
    static <P extends K2, Proof extends Profunctor.Mu> Profunctor<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Profunctor<P, Proof>) proofBox;
    }

    /**
     * Takes a function from the output type {@code C} to the input type {@code A} and a function from the input
     * type {@code B} to the output type {@code D}, and returns a function from a container of {@code A, B} to
     * a container of {@code C, D}.
     *
     * <p>A straightforward example of an implementation of {@code dimap} is the method {@link Function#andThen(Function)}.
     * For functions, the expression
     *
     * <pre><code>
     * dimap(g, h).apply(f)
     * </code></pre>
     *
     * <p>is equivalent to
     *
     * <pre><code>
     * g.andThen(f).andThen(h)
     * </code></pre>
     *
     * @param g   A function from output to input.
     * @param h   A function from input to output.
     * @param <A> The first input type.
     * @param <B> The second input type.
     * @param <C> The first output type.
     * @param <D> The second output type.
     * @return A function from an input container to an output container.
     * @see #dimap(App2, Function, Function)
     */
    <A, B, C, D> FunctionType<App2<P, A, B>, App2<P, C, D>> dimap(final Function<C, A> g, final Function<B, D> h);

    //<A, B, C, D> FunctionType<App2<P, A, B>, App2<P, C, D>> dimap(final Function<C, A> g, final Function<B, D> h);

    /**
     * Takes an input container, a left mapping from output to input, and a right mapping from input to output,
     * and returns an output container.
     *
     * @param arg The input container.
     * @param g   A function from output to input.
     * @param h   A function from input to output.
     * @param <A> The first input type.
     * @param <B> The second input type.
     * @param <C> The first output type.
     * @param <D> The second output type.
     * @return An output container.
     * @implSpec This method is equivalent to {@code dimap(g, h).apply(arg)}.
     * @see #dimap(Function, Function)
     */
    default <A, B, C, D> App2<P, C, D> dimap(final App2<P, A, B> arg, final Function<C, A> g, final Function<B, D> h) {
        return dimap(g, h).apply(arg);
    }

    /**
     * Takes an input container, a mapping from output to input, and a mapping from input to output, and returns
     * an output container.
     *
     * @param arg The input container.
     * @param g   A function from output to input.
     * @param h   A function from input to output.
     * @param <A> The first input type.
     * @param <B> The second input type.
     * @param <C> The first output type.
     * @param <D> The second output type.
     * @return An output container.
     * @implSpec This method is equivalent to {@code dimap(arg.get(), g, h)}.
     * @see #dimap(App2, Function, Function)
     * @see #dimap(Function, Function)
     */
    default <A, B, C, D> App2<P, C, D> dimap(final Supplier<App2<P, A, B>> arg, final Function<C, A> g, final Function<B, D> h) {
        return dimap(g, h).apply(arg.get());
    }

    /**
     * Maps the first, or left hand, parameter of the given input.
     *
     * @param input The input container.
     * @param g     The mapping function.
     * @param <A>   The first input type.
     * @param <B>   The second input and output type.
     * @param <C>   The first output type.
     * @return A container taking the mapped input and yielding the same output.
     * @see #dimap(App2, Function, Function)
     */
    default <A, B, C> App2<P, C, B> lmap(final App2<P, A, B> input, final Function<C, A> g) {
        return dimap(input, g, Function.identity());
    }

    /**
     * Maps the second, or left hand, parameter of the given input.
     *
     * @param input The input container.
     * @param h     The mapping function.
     * @param <A>   The first input and output type.
     * @param <B>   The second input type.
     * @param <D>   The second output type.
     * @return A container taking the same input and yielding the mapped output.
     * @see #dimap(App2, Function, Function)
     */
    default <A, B, D> App2<P, A, D> rmap(final App2<P, A, B> input, final Function<B, D> h) {
        return dimap(input, Function.identity(), h);
    }
}

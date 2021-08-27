// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.TraversalP;

import java.util.function.Function;

/**
 * A traversal is an optic that implements access to and effectful modification of any number of fields of an object.
 * All other optics can be implemented as specializations of {@code Traversal}.
 *
 * <p>The operation performed by traversals is analogous to {@link com.mojang.datafixers.kinds.Traversable},
 * which defines a structure-preserving transformation in the context of an effectful transformation.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @dfu.shape %.Type.[%0::%2,%1::%3]
 * @see com.mojang.datafixers.kinds.Traversable
 */
public interface Traversal<S, T, A, B> extends Wander<S, T, A, B>, App2<Traversal.Mu<A, B>, S, T>, Optic<TraversalP.Mu, S, T, A, B> {
    /**
     * The witness type for {@link Traversal}.
     *
     * @param <A> The input field type.
     * @param <B> The input object type.
     * @dfu.shape %.Mu.[%^1::%0,%^2::%1]
     * @dfu.hidden
     */
    final class Mu<A, B> implements K2 {}

    /**
     * Thunk method that casts an applied {@link Traversal.Mu} to a {@link Traversal}.
     *
     * @param box The boxed {@link Traversal}.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed {@link Traversal}.
     * @dfu.hidden
     */
    static <S, T, A, B> Traversal<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Traversal<S, T, A, B>) box;
    }

    /**
     * Produces a function that takes a transformation between field types and produces a transformation between
     * object types. The returned function uses this traversal to traverse the object types and transform each
     * applicable field.
     *
     * @param proof The type class associated with this optic.
     * @param <P>   The transformation type.
     * @return A function from field transformations to object transformations.
     * @see Wander#wander(Applicative, FunctionType)
     */
    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends TraversalP.Mu, P> proof) {
        final TraversalP<P, ? extends TraversalP.Mu> proof1 = TraversalP.unbox(proof);
        return input -> proof1.wander(this, input);
    }

    /**
     * The {@link TraversalP} type class instance for {@link Traversal}.
     *
     * @param <A2> The input field type.
     * @param <B2> The output field type.
     * @dfu.shape instance %.Mu.[_::%0,_::%1]
     */
    final class Instance<A2, B2> implements TraversalP<Mu<A2, B2>, TraversalP.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Traversal.Mu<A2, B2>, A, B>, App2<Traversal.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return tr -> new Traversal<C, D, A2, B2>() {
                @Override
                public <F extends K1> FunctionType<C, App<F, D>> wander(final Applicative<F, ?> applicative, final FunctionType<A2, App<F, B2>> input) {
                    return c -> applicative.map(h, Traversal.unbox(tr).wander(applicative, input).apply(g.apply(c)));
                }
            };
        }

        @Override
        public <S, T, A, B> App2<Traversal.Mu<A2, B2>, S, T> wander(final Wander<S, T, A, B> wander, final App2<Traversal.Mu<A2, B2>, A, B> input) {
            return new Traversal<S, T, A2, B2>() {
                @Override
                public <F extends K1> FunctionType<S, App<F, T>> wander(final Applicative<F, ?> applicative, final FunctionType<A2, App<F, B2>> function) {
                    return wander.wander(applicative, unbox(input).wander(applicative, function));
                }
            };
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.CartesianLike;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.Traversable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A <em>product type</em> containing two values.
 *
 * @param <F> The type of the first value.
 * @param <S> The type of the second value.
 * @dfu.shape "(%0%.,.%1)"
 */
public class Pair<F, S> implements App<Pair.Mu<S>, F> {
    /**
     * The witness type for {@link Pair}. The represents the partially applied type constructor {@code Pair<_,S>}.
     *
     * @param <S> The type of the second value.
     * @dfu.shape "(%^1%.,.%0)"
     */
    public static final class Mu<S> implements K1 {}

    /**
     * Thunk method to cast an applied {@link Pair.Mu} to a {@link Pair}.
     *
     * @param box The boxed pair.
     * @param <F> The type of the first value.
     * @param <S> The type of the second value.
     * @return The unboxed pair.
     */
    public static <F, S> Pair<F, S> unbox(final App<Mu<S>, F> box) {
        return (Pair<F, S>) box;
    }

    private final F first;
    private final S second;

    /**
     * Constructs a pair with the given values.
     *
     * @param first  The first value.
     * @param second The second value.
     * @see #of(Object, Object)
     */
    public Pair(final F first, final S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * The first value.
     */
    public F getFirst() {
        return first;
    }

    /**
     * The second value.
     */
    public S getSecond() {
        return second;
    }

    /**
     * Returns a pair with the elements of this pair swapped.
     */
    public Pair<S, F> swap() {
        return of(second, first);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Pair<?, ?>)) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(first, second);
    }

    /**
     * Transforms the first element of this pair to another type.
     *
     * @param function The transformation.
     * @param <F2>     The new type of the first element.
     * @return A pair with the transformed first element.
     * @apiNote This method implements the <em>functor operator</em> for the first element of {@link Pair}.
     * @see Instance#map(Function, App)
     */
    public <F2> Pair<F2, S> mapFirst(final Function<? super F, ? extends F2> function) {
        return of(function.apply(first), second);
    }

    /**
     * Transforms the second element of this pair to another type.
     *
     * @param function The transformation.
     * @param <S2>     The new type of the second element.
     * @return A pair with the transformed second element.
     * @apiNote This method implements the <em>functor operator</em> for the second element of {@link Pair}.
     */
    public <S2> Pair<F, S2> mapSecond(final Function<? super S, ? extends S2> function) {
        return of(first, function.apply(second));
    }

    /**
     * Creates a pair with the given values.
     *
     * @param first  The first value.
     * @param second The second value.
     * @param <F>    The type of the first value.
     * @param <S>    The type of the second value.
     * @return A pair containing the given values.
     */
    public static <F, S> Pair<F, S> of(final F first, final S second) {
        return new Pair<>(first, second);
    }

    /**
     * Returns a {@link Collector} that collects pairs into a {@link Map}, using the first value as the key
     * in the map and the second value as the value in the map.
     *
     * @param <F> The type of the first value.
     * @param <S> The type of the second value.
     * @return A {@link Collector} that transforms a stream of {@link Pair} into a {@link Map}.
     */
    public static <F, S> Collector<Pair<F, S>, ?, Map<F, S>> toMap() {
        return Collectors.toMap(Pair::getFirst, Pair::getSecond);
    }

    /**
     * The {@link CartesianLike} type class instance for {@link Pair}.
     *
     * @param <S2> The type of the pair's second element.
     * @dfu.shape "(_%.,.%0)"
     */
    public static final class Instance<S2> implements Traversable<Mu<S2>, Instance.Mu<S2>>, CartesianLike<Mu<S2>, S2, Instance.Mu<S2>> {
        /**
         * The witness type of {@link Instance}.
         *
         * @param <S2> The type of the pair's second element.
         * @dfu.shape instance %^1
         */
        public static final class Mu<S2> implements Traversable.Mu, CartesianLike.Mu {}

        @Override
        public <T, R> App<Pair.Mu<S2>, R> map(final Function<? super T, ? extends R> func, final App<Pair.Mu<S2>, T> ts) {
            return Pair.unbox(ts).mapFirst(func);
        }

        @Override
        public <F extends K1, A, B> App<F, App<Pair.Mu<S2>, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<Pair.Mu<S2>, A> input) {
            final Pair<A, S2> pair = Pair.unbox(input);
            return applicative.ap(b -> of(b, pair.second), function.apply(pair.first));
        }

        @Override
        public <A> App<Pair.Mu<S2>, A> to(final App<Pair.Mu<S2>, A> input) {
            return input;
        }

        @Override
        public <A> App<Pair.Mu<S2>, A> from(final App<Pair.Mu<S2>, A> input) {
            return input;
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A container wrapping a single value.
 *
 * <p>The main feature of this class is its {@linkplain IdF.Instance implementation} of
 * {@linkplain Applicative applicative functor} operations. It's useful in cases where a functor is required
 * but there is no existing functor that would be used.
 *
 * @param <A> The type of the contained value.
 * @apiNote This class represents the <em>identity functor</em>, hence the name {@code IdF}.
 * @see <a href="https://en.wikipedia.org/wiki/Monad_(functional_programming)#Identity_monad">Identity monads,
 * a similar concept for the monad type class</a>
 */
public final class IdF<A> implements App<IdF.Mu, A> {
    /**
     * The witness type of {@link IdF}.
     *
     * @dfu.shape %.Mu.[%^1]
     */
    public static final class Mu implements K1 {}

    /**
     * The value contained in this {@code IdF}.
     */
    protected final A value;

    IdF(final A value) {
        this.value = value;
    }

    /**
     * Gets the value of this container.
     *
     * @return The value.
     */
    public A value() {
        return value;
    }

    /**
     * Gets the value stored in an {@link App} representing an {@code IdF} instance.
     *
     * @param box The boxed {@code IdF} instance.
     * @param <A> The type of the contained value.
     * @throws ClassCastException If the {@code box} is not an instance of {@code IdF}.
     */
    public static <A> A get(final App<Mu, A> box) {
        return ((IdF<A>) box).value;
    }

    /**
     * Creates an {@code IdF} container for a value.
     *
     * @param a   The value that will be stored.
     * @param <A> The type of the contained value.
     * @return The created container.
     */
    public static <A> IdF<A> create(final A a) {
        return new IdF<>(a);
    }

    /**
     * An instance of {@link Functor} and {@link Applicative} for {@link IdF}.
     */
    public enum Instance implements Functor<Mu, Instance.Mu>, Applicative<Mu, Instance.Mu> {
        /**
         * The singleton instance of this type.
         */
        INSTANCE;

        /**
         * The witness type of {@code IdF.Instance}.
         */
        public static final class Mu implements Functor.Mu, Applicative.Mu {}

        @Override
        public <T, R> App<IdF.Mu, R> map(final Function<? super T, ? extends R> func, final App<IdF.Mu, T> ts) {
            final IdF<T> idF = (IdF<T>) ts;
            return new IdF<>(func.apply(idF.value));
        }

        @Override
        public <A> App<IdF.Mu, A> point(final A a) {
            return create(a);
        }

        @Override
        public <A, R> Function<App<IdF.Mu, A>, App<IdF.Mu, R>> lift1(final App<IdF.Mu, Function<A, R>> function) {
            return a -> create(get(function).apply(get(a)));
        }

        @Override
        public <A, B, R> BiFunction<App<IdF.Mu, A>, App<IdF.Mu, B>, App<IdF.Mu, R>> lift2(final App<IdF.Mu, BiFunction<A, B, R>> function) {
            return (a, b) -> create(get(function).apply(get(a), get(b)));
        }
    }
}

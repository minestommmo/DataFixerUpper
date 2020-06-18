// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link Functor} that stores values of an unrelated type. As functor and applicative operations are performed
 * on const values, the stored values accumulate in a predictable fashion.
 *
 * <p>The type {@code T} is known as a <em>phantom type</em>. That is, no values of this type are ever required
 * to use const objects. Because of this, the type {@code T} may be any type, even a type without any instances.
 *
 * <p>The {@link Const} type is typically used to supply values to traversal algorithms when the desired result
 * does not depend on the types being traversed over.
 *
 * @param <C> The type of values stored in this class.
 * @param <T> A phantom type representing the type of values the functor and applicative operations transform.
 *            No values of this type ever exist in objects of this type.
 * @see Monoid
 * @see Traversable
 */
public final class Const<C, T> implements App<Const.Mu<C>, T> {
    /**
     * The witness type for {@link Const}.
     *
     * @param <C> The type of stored values.
     * @dfu.shape %.Mu.[%0,%^1]
     */
    public static final class Mu<C> implements K1 {}

    /**
     * Returns the value contained within the given const box.
     *
     * @param box The boxed {@link Const}.
     * @param <C> The type of the stored value.
     * @param <T> The type of value the boxed const purports to support.
     * @return The stored value.
     */
    public static <C, T> C unbox(final App<Mu<C>, T> box) {
        return ((Const<C, T>) box).value;
    }

    /**
     * Creates a {@link Const} with the given stored value.
     *
     * @param value The stored value.
     * @param <C>   The type of the stored value.
     * @param <T>   The type of the value purportedly supported in the returned object.
     * @return A {@link Const} that stores the given value.
     */
    public static <C, T> Const<C, T> create(final C value) {
        return new Const<>(value);
    }

    private final C value;

    Const(final C value) {
        this.value = value;
    }

    /**
     * The {@link Applicative} type class instance for {@link Const}. Applicative operations on the type
     * {@link Const} correspond to monoidal operations on the type {@code C}.
     *
     * @param <C> The type of value stored in the const objects.
     * @dfu.shape instance %.Mu.[%0,_]
     */
    public static final class Instance<C> implements Applicative<Mu<C>, Instance.Mu<C>> {
        /**
         * The witness type for {@link Const.Instance}
         *
         * @param <C> The type of value stored in the const objects.
         * @dfu.shape instance %^1
         */
        public static final class Mu<C> implements Applicative.Mu {}

        private final Monoid<C> monoid;

        /**
         * Constructs a new instance using the given {@link Monoid} type class instance.
         *
         * @param monoid The monoid instance that defines the behavior of the type {@code C}.
         */
        public Instance(final Monoid<C> monoid) {
            this.monoid = monoid;
        }

        /**
         * {@inheritDoc}
         *
         * <p>This method simply leaves the stored value unchanged.
         */
        @Override
        public <T, R> App<Const.Mu<C>, R> map(final Function<? super T, ? extends R> func, final App<Const.Mu<C>, T> ts) {
            return create(Const.unbox(ts));
        }

        /**
         * {@inheritDoc}
         *
         * <p>This method corresponds to {@link Monoid#point()}.
         */
        @Override
        public <A> App<Const.Mu<C>, A> point(final A a) {
            return create(monoid.point());
        }

        /**
         * {@inheritDoc}
         *
         * <p>This method corresponds to {@link Monoid#add(Object, Object)}.
         */
        @Override
        public <A, R> Function<App<Const.Mu<C>, A>, App<Const.Mu<C>, R>> lift1(final App<Const.Mu<C>, Function<A, R>> function) {
            return a -> create(monoid.add(Const.unbox(function), Const.unbox(a)));
        }

        @Override
        public <A, B, R> BiFunction<App<Const.Mu<C>, A>, App<Const.Mu<C>, B>, App<Const.Mu<C>, R>> lift2(final App<Const.Mu<C>, BiFunction<A, B, R>> function) {
            return (a, b) -> create(monoid.add(Const.unbox(function), monoid.add(Const.unbox(a), Const.unbox(b))));
        }
    }
}

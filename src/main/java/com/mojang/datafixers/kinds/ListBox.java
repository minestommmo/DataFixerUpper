// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A container wrapping an ordered sequence of values.
 *
 * @param <T> The type of values contained in the list.
 * @apiNote This class represents the <em>list functor</em>.
 * @see ListBox.Instance
 */
public final class ListBox<T> implements App<ListBox.Mu, T> {
    /**
     * The witness type of {@link ListBox}.
     *
     * @dfu.shape %.Mu.[%^1]
     */
    public static final class Mu implements K1 {}

    /**
     * Thunk method that casts the given applied {@link ListBox.Mu} to a {@link ListBox}.
     *
     * @param box The boxed {@link ListBox}.
     * @param <T> The type of values contained in the list.
     * @return The unboxed list.
     */
    public static <T> List<T> unbox(final App<Mu, T> box) {
        return ((ListBox<T>) box).value;
    }

    /**
     * Creates a {@link ListBox} containing the same elements as the given {@link List}.
     *
     * @param value The list of values.
     * @param <T>   The type of values.
     * @return A {@link ListBox} containing the same values as {@code value}.
     */
    public static <T> ListBox<T> create(final List<T> value) {
        return new ListBox<>(value);
    }

    private final List<T> value;

    private ListBox(final List<T> value) {
        this.value = value;
    }

    /**
     * Applies an operation {@code function} to each element of {@code input}, then returns a container holding a
     * list of the outputs.
     *
     * @param applicative The {@link Applicative} instance that defines the behavior of {@code F}.
     * @param function    The operation to apply to each element.
     * @param input       The input elements.
     * @param <F>         The output container type.
     * @param <A>         The type of the input elements.
     * @param <B>         The type of the output elements.
     * @return A container of the output values.
     * @apiNote This method implements the <em>traversable operator</em> for the type {@link ListBox}.
     * @see Instance#traverse(Applicative, Function, App)
     */
    public static <F extends K1, A, B> App<F, List<B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final List<A> input) {
        return applicative.map(ListBox::unbox, Instance.INSTANCE.traverse(applicative, function, create(input)));
    }

    /**
     * Transforms a list of some container to a container of lists.
     *
     * @param applicative The {@link Applicative} instance that defines the behavior of {@code F}.
     * @param input       The list of container.
     * @param <F>         The container type.
     * @param <A>         The inner element type.
     * @return A container of list.
     * @see Traversable#flip(Applicative, App)
     */
    public static <F extends K1, A> App<F, List<A>> flip(final Applicative<F, ?> applicative, final List<App<F, A>> input) {
        return applicative.map(ListBox::unbox, Instance.INSTANCE.flip(applicative, create(input)));
    }

    /**
     * The {@link Traversable} type class instance for {@link ListBox}.
     *
     * @dfu.shape instance %.Mu.[_]
     */
    public enum Instance implements Traversable<Mu, Instance.Mu> {
        INSTANCE;

        /**
         * The witness type of {@link Instance}.
         *
         * @dfu.shape instance %^1
         */
        public static final class Mu implements Traversable.Mu {}

        @Override
        public <T, R> App<ListBox.Mu, R> map(final Function<? super T, ? extends R> func, final App<ListBox.Mu, T> ts) {
            return create(ListBox.unbox(ts).stream().map(func).collect(Collectors.toList()));
        }

        @Override
        public <F extends K1, A, B> App<F, App<ListBox.Mu, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<ListBox.Mu, A> input) {
            final List<? extends A> list = unbox(input);

            App<F, ImmutableList.Builder<B>> result = applicative.point(ImmutableList.builder());

            for (final A a : list) {
                final App<F, B> fb = function.apply(a);
                result = applicative.ap2(applicative.point(ImmutableList.Builder::add), result, fb);
            }

            return applicative.map(b -> create(b.build()), result);
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.CocartesianLike;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.Traversable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A type which may hold <em>either</em> a value of the left type, or a value of the right type. An {@link Either}
 * is a basic sum type, or tagged union.
 *
 * @param <L> The left type.
 * @param <R> The right type.
 * @dfu.shape "(%0%.|.%1)"
 */
public abstract class Either<L, R> implements App<Either.Mu<R>, L> {
    /**
     * The witness type for {@link Either}.
     *
     * @param <R> The right type.
     * @dfu.shape "(%^1%.|.%0)"
     */
    public static final class Mu<R> implements K1 {}

    /**
     * Thunk method to cast an applied {@link Either.Mu} to an {@link Either}.
     *
     * @param box The boxed either.
     * @param <L> The left type.
     * @param <R> The right type.
     * @return The unboxed either.
     */
    public static <L, R> Either<L, R> unbox(final App<Mu<R>, L> box) {
        return (Either<L, R>) box;
    }

    private static final class Left<L, R> extends Either<L, R> {
        private final L value;

        public Left(final L value) {
            this.value = value;
        }

        @Override
        public <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2) {
            return new Left<>(f1.apply(value));
        }

        @Override
        public <T> T map(final Function<? super L, ? extends T> l, final Function<? super R, ? extends T> r) {
            return l.apply(value);
        }

        @Override
        public Either<L, R> ifLeft(Consumer<? super L> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public Either<L, R> ifRight(Consumer<? super R> consumer) {
            return this;
        }

        @Override
        public Optional<L> left() {
            return Optional.of(value);
        }

        @Override
        public Optional<R> right() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "Left[" + value + "]";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Left<?, ?> left = (Left<?, ?>) o;
            return Objects.equals(value, left.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    private static final class Right<L, R> extends Either<L, R> {
        private final R value;

        public Right(final R value) {
            this.value = value;
        }

        @Override
        public <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2) {
            return new Right<>(f2.apply(value));
        }

        @Override
        public <T> T map(final Function<? super L, ? extends T> l, final Function<? super R, ? extends T> r) {
            return r.apply(value);
        }

        @Override
        public Either<L, R> ifLeft(Consumer<? super L> consumer) {
            return this;
        }

        @Override
        public Either<L, R> ifRight(Consumer<? super R> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public Optional<L> left() {
            return Optional.empty();
        }

        @Override
        public Optional<R> right() {
            return Optional.of(value);
        }

        @Override
        public String toString() {
            return "Right[" + value + "]";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Right<?, ?> right = (Right<?, ?>) o;
            return Objects.equals(value, right.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    private Either() {
    }

    /**
     * Maps either the left or the right value, whichever is present, to another type.
     *
     * @param f1  A mapping function for the left type.
     * @param f2  A mapping function for the right type.
     * @param <C> The new left type.
     * @param <D> The new right type.
     * @return An {@link Either} containing the new left or right value.
     */
    public abstract <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2);

    /**
     * Maps either the left or the right value, whichever is present, to a common third type.
     *
     * @param l   A mapping function for the left type.
     * @param r   A mapping function for the right type.
     * @param <T> The common type to map to.
     * @return The result of mapping the left or right value.
     */
    public abstract <T> T map(final Function<? super L, ? extends T> l, Function<? super R, ? extends T> r);

    /**
     * Applies the given callback if the left value is present.
     *
     * @param consumer A callback to apply to the left value.
     * @return This instance.
     */
    public abstract Either<L, R> ifLeft(final Consumer<? super L> consumer);

    /**
     * Applies the given callback if the right value is present.
     *
     * @param consumer A callback to apply to the right value.
     * @return This instance.
     */
    public abstract Either<L, R> ifRight(final Consumer<? super R> consumer);

    /**
     * Gets the left value in an {@link Optional}.
     */
    public abstract Optional<L> left();

    /**
     * Gets the right value in an {@link Optional}.
     */
    public abstract Optional<R> right();

    /**
     * Maps the left value, if present, to another type.
     *
     * @param l   A mapping function for the left value.
     * @param <T> The new left type.
     * @return An {@link Either} which may contain the mapped left value.
     */
    public <T> Either<T, R> mapLeft(final Function<? super L, ? extends T> l) {
        return map(t -> left(l.apply(t)), Either::right);
    }

    /**
     * Maps the right value, if present, to another type.
     *
     * @param l   A mapping function for the right value.
     * @param <T> The new right type.
     * @return An {@link Either} which may contain the mapped right value.
     */
    public <T> Either<L, T> mapRight(final Function<? super R, ? extends T> l) {
        return map(Either::left, t -> right(l.apply(t)));
    }

    /**
     * Constructs an {@link Either} with the given left value.
     *
     * @param value The left value.
     * @param <L>   The left type.
     * @param <R>   The right type.
     * @return An {@link Either} containing the given value.
     */
    public static <L, R> Either<L, R> left(final L value) {
        return new Left<>(value);
    }

    /**
     * Constructs an {@link Either} with the given right value.
     *
     * @param value The right value.
     * @param <L>   The left type.
     * @param <R>   The right type.
     * @return An {@link Either} containing the given value.
     */
    public static <L, R> Either<L, R> right(final R value) {
        return new Right<>(value);
    }

    /**
     * Returns the left value if it is present, or throws an exception if it is not. If the right value is an
     * instance of {@link Throwable}, the thrown exception has that value as its {@linkplain Throwable#getCause() cause}.
     *
     * @return The left value, if present.
     * @throws RuntimeException If the left value is not present.
     */
    public L orThrow() {
        return map(l -> l, r -> {
            if (r instanceof Throwable) {
                throw new RuntimeException((Throwable) r);
            }
            throw new RuntimeException(r.toString());
        });
    }

    /**
     * Swaps the value in this {@link Either}, such that the left value becomes the right value and visa-versa.
     */
    public Either<R, L> swap() {
        return map(Either::right, Either::left);
    }

    /**
     * Maps the left value, if it is present, to a new type in an {@link Either}.
     *
     * @param function A mapping function for the left value.
     * @param <L2>     The new left type.
     * @return An {@link Either} that is the result of the mapping.
     */
    public <L2> Either<L2, R> flatMap(final Function<L, Either<L2, R>> function) {
        return map(function, Either::right);
    }

    /**
     * The {@link Applicative}, {@link Traversable}, and {@link CocartesianLike} type class instance for {@link Either}.
     *
     * @param <R2> The right type.
     * @dfu.shape instance "(_%.|.%0)"
     */
    public static final class Instance<R2> implements Applicative<Mu<R2>, Instance.Mu<R2>>, Traversable<Mu<R2>, Instance.Mu<R2>>, CocartesianLike<Mu<R2>, R2, Instance.Mu<R2>> {
        /**
         * The witness type for {@link Either.Instance}.
         *
         * @param <R2> The right type.
         * @dfu.shape instance %^1
         */
        public static final class Mu<R2> implements Applicative.Mu, Traversable.Mu, CocartesianLike.Mu {}

        @Override
        public <T, R> App<Either.Mu<R2>, R> map(final Function<? super T, ? extends R> func, final App<Either.Mu<R2>, T> ts) {
            return Either.unbox(ts).mapLeft(func);
        }

        @Override
        public <A> App<Either.Mu<R2>, A> point(final A a) {
            return left(a);
        }

        @Override
        public <A, R> Function<App<Either.Mu<R2>, A>, App<Either.Mu<R2>, R>> lift1(final App<Either.Mu<R2>, Function<A, R>> function) {
            return a -> Either.unbox(function).flatMap(f -> Either.unbox(a).mapLeft(f));
        }

        @Override
        public <A, B, R> BiFunction<App<Either.Mu<R2>, A>, App<Either.Mu<R2>, B>, App<Either.Mu<R2>, R>> lift2(final App<Either.Mu<R2>, BiFunction<A, B, R>> function) {
            return (a, b) -> Either.unbox(function).flatMap(
                f -> Either.unbox(a).flatMap(
                    av -> Either.unbox(b).mapLeft(
                        bv -> f.apply(av, bv)
                    )
                )
            );
        }

        @Override
        public <F extends K1, A, B> App<F, App<Either.Mu<R2>, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<Either.Mu<R2>, A> input) {
            return Either.unbox(input).map(
                l -> {
                    final App<F, B> b = function.apply(l);
                    return applicative.ap(Either::left, b);
                },
                r -> applicative.point(right(r))
            );
        }

        @Override
        public <A> App<Either.Mu<R2>, A> to(final App<Either.Mu<R2>, A> input) {
            return input;
        }

        @Override
        public <A> App<Either.Mu<R2>, A> from(final App<Either.Mu<R2>, A> input) {
            return input;
        }
    }
}

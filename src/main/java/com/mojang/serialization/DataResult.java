// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function3;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents either a successful operation, or a partial operation with an error message and a partial result (if available).
 * Also stores an additional lifecycle marker (monoidal).
 *
 * @param <R> The type of the wrapped result.
 */
public class DataResult<R> implements App<DataResult.Mu, R> {
    /**
     * A marker interface representing the type constructor {@link DataResult}.
     */
    public static final class Mu implements K1 {}

    /**
     * Thunk method to cast the applied {@link DataResult} type constructor to the type {@link DataResult}.
     */
    public static <R> DataResult<R> unbox(final App<Mu, R> box) {
        return (DataResult<R>) box;
    }

    private final Either<R, PartialResult<R>> result;
    private final Lifecycle lifecycle;

    /**
     * Creates a successful {@link DataResult} with the given result value.
     *
     * @param result The result value.
     * @param <R>    The type of the result value.
     * @return A successful {@link DataResult}.
     * @see #success(Object, Lifecycle)
     */
    public static <R> DataResult<R> success(final R result) {
        return success(result, Lifecycle.experimental());
    }

    /**
     * Creates an error {@link DataResult} with the given message and partial result value.
     *
     * @param message       The error message.
     * @param partialResult The partial or fallback result value.
     * @param <R>           The type of the result value.
     * @return An error {@link DataResult}.
     * @see #error(String, Object, Lifecycle)
     */
    public static <R> DataResult<R> error(final String message, final R partialResult) {
        return error(message, partialResult, Lifecycle.experimental());
    }

    /**
     * Creates an error {@link DataResult} with the given message and no partial result.
     *
     * @param message The error message.
     * @param <R>     The expected type of the result.
     * @return An error {@link DataResult}.
     * @see #error(String, Object)
     */
    public static <R> DataResult<R> error(final String message) {
        return error(message, Lifecycle.experimental());
    }

    /**
     * Creates a successful {@link DataResult} with the given result value and lifecycle.
     *
     * @param result       The result value.
     * @param experimental The lifecycle to use.
     * @param <R>          The type of the result value.
     * @return A successful {@link DataResult}.
     * @see #success(Object, Lifecycle)
     */
    public static <R> DataResult<R> success(final R result, final Lifecycle experimental) {
        return new DataResult<>(Either.left(result), experimental);
    }

    /**
     * Creates an error {@link DataResult} with the given message, partial result value, and lifecycle.
     *
     * @param message       The error message.
     * @param partialResult The partial or fallback result value.
     * @param lifecycle     The lifecycle to use.
     * @param <R>           The type of the result value.
     * @return An error {@link DataResult}.
     */
    public static <R> DataResult<R> error(final String message, final R partialResult, final Lifecycle lifecycle) {
        return new DataResult<>(Either.right(new PartialResult<>(message, Optional.of(partialResult))), lifecycle);
    }

    /**
     * Creates an error {@link DataResult} with the given message and lifecycle.
     *
     * @param message   The error message.
     * @param lifecycle The lifecycle to use.
     * @param <R>       The expected type of the result.
     * @return An error {@link DataResult}.
     * @see #error(String)
     */
    public static <R> DataResult<R> error(final String message, final Lifecycle lifecycle) {
        return new DataResult<>(Either.right(new PartialResult<>(message, Optional.empty())), lifecycle);
    }

    /**
     * Converts a partial function into a function that produces a {@link DataResult}. If the partial function
     * returns {@code null}, then the returned function returns an error {@link DataResult}, otherwise a successful
     * {@link DataResult}.
     *
     * @param partialGet  The partial function.
     * @param errorPrefix The error string to use if {@code partialGet} returns {@code null}.
     * @param <K>         The argument type of the partial function.
     * @param <V>         The result type of the partial function.
     * @return A function that wraps the result of the partial function in a {@link DataResult}.
     * @see Optional
     */
    public static <K, V> Function<K, DataResult<V>> partialGet(final Function<K, V> partialGet, final Supplier<String> errorPrefix) {
        return name -> Optional.ofNullable(partialGet.apply(name)).map(DataResult::success).orElseGet(() -> error(errorPrefix.get() + name));
    }

    /**
     * Creates a {@link DataResult} from an {@link Either}. The created {@link DataResult} will be successful
     * if {@code result} is a left, and will be an error with the contained {@link PartialResult} if {@code result}
     * is a right.
     *
     * @param result    The {@link Either} to wrap.
     * @param lifecycle The lifecycle to use.
     * @param <R>       The type of the result value.
     * @return A {@link DataResult} wrapping {@code result}.
     */
    private static <R> DataResult<R> create(final Either<R, PartialResult<R>> result, final Lifecycle lifecycle) {
        return new DataResult<>(result, lifecycle);
    }

    private DataResult(final Either<R, PartialResult<R>> result, final Lifecycle lifecycle) {
        this.result = result;
        this.lifecycle = lifecycle;
    }

    /**
     * Returns the wrapped {@link Either}.
     */
    public Either<R, PartialResult<R>> get() {
        return result;
    }

    /**
     * Returns the successful result, if one is present.
     *
     * @see #error()
     */
    public Optional<R> result() {
        return result.left();
    }

    /**
     * Returns the lifecycle used in this {@link DataResult}.
     */
    public Lifecycle lifecycle() {
        return lifecycle;
    }

    /**
     * Returns any result, if one is present. If this result is an error, the partial result, if any, is returned.
     *
     * @param onError A callback to run on error. It receives the error message.
     * @return The result or partial result, if present.
     */
    public Optional<R> resultOrPartial(final Consumer<String> onError) {
        return result.map(
            Optional::of,
            r -> {
                onError.accept(r.message);
                return r.partialResult;
            }
        );
    }

    /**
     * Unsafely gets the result stored in this {@link DataResult}.
     *
     * @param allowPartial Whether to return the partial result if this is an error.
     * @param onError      A callback to run on error. It receives the error message.
     * @return The result.
     * @throws RuntimeException If this {@link DataResult} does not contain a result value.
     */
    public R getOrThrow(final boolean allowPartial, final Consumer<String> onError) {
        return result.map(
            l -> l,
            r -> {
                onError.accept(r.message);
                if (allowPartial && r.partialResult.isPresent()) {
                    return r.partialResult.get();
                }
                throw new RuntimeException(r.message);
            }
        );
    }

    /**
     * Returns the error {@link PartialResult}, if one is present.
     *
     * @see #result()
     */
    public Optional<PartialResult<R>> error() {
        return result.right();
    }

    /**
     * Applies a function to the result or partial result. Successes remain successes and errors remain errors.
     *
     * @param function The conversion function.
     * @param <T>      The new result type.
     * @return The converted result.
     * @apiNote This method implements the <em>functor operator</em> for the type {@link DataResult}.
     * @see #flatMap(Function)
     * @see #ap(DataResult)
     */
    public <T> DataResult<T> map(final Function<? super R, ? extends T> function) {
        return create(result.mapBoth(
            function,
            r -> new PartialResult<>(r.message, r.partialResult.map(function))
        ), lifecycle);
    }

    /**
     * Promotes an error with a partial result to a success. If this is a success, it is returned
     * unchanged.
     *
     * @param onError A callback to run on error. It is passed the error string.
     * @return A success containing the result or partial result, or an error containing no result.
     */
    public DataResult<R> promotePartial(final Consumer<String> onError) {
        return result.map(
            r -> new DataResult<>(Either.left(r), lifecycle),
            r -> {
                onError.accept(r.message);
                return r.partialResult
                    .map(pr -> new DataResult<>(Either.left(pr), lifecycle))
                    .orElseGet(() -> create(Either.right(r), lifecycle));
            }
        );
    }

    private static String appendMessages(final String first, final String second) {
        return first + "; " + second;
    }

    /**
     * Applies the function to either full or partial result, in case of partial concatenates errors.
     *
     * @param function The partial conversion function.
     * @param <R2>     The new result type.
     * @return The converted result.
     * @apiNote This method implements the <em>monad operator</em> in the context of {@link DataResult}.
     * @see #map(Function)
     */
    public <R2> DataResult<R2> flatMap(final Function<? super R, ? extends DataResult<R2>> function) {
        return result.map(
            l -> {
                final DataResult<R2> second = function.apply(l);
                return create(second.get(), lifecycle.add(second.lifecycle));
            },
            r -> r.partialResult
                .map(value -> {
                    final DataResult<R2> second = function.apply(value);
                    return create(Either.right(second.get().map(
                        l2 -> new PartialResult<>(r.message, Optional.of(l2)),
                        r2 -> new PartialResult<>(appendMessages(r.message, r2.message), r2.partialResult)
                    )), lifecycle.add(second.lifecycle));
                })
                .orElseGet(
                    () -> create(Either.right(new PartialResult<>(r.message, Optional.empty())), lifecycle)
                )
        );
    }

    /**
     * Applies a function which is itself wrapped in a {@link DataResult} to the result or partial result.
     *
     * <p>If either {@code this} or {@code functionResult} are errors, then the return value is also an error.
     * Also, if either {@code this} or {@code functionResult} does not contain a result, then the
     * return value also does not contain a result.
     *
     * @param functionResult The function to potentially apply.
     * @param <R2>           The new result type.
     * @return The result of {@code functionResult} applies to {@code this}.
     * @apiNote This method implements the <em>applicative operator</em> in the context of {@link DataResult}.
     * @see #map(Function)
     * @see Applicative
     * @see <a href="https://medium.com/@lettier/your-easy-guide-to-monads-applicatives-functors-862048d61610">Functors, Applicatives, and Monads</a>
     */
    public <R2> DataResult<R2> ap(final DataResult<Function<R, R2>> functionResult) {
        return create(result.map(
            arg -> functionResult.result.mapBoth(
                func -> func.apply(arg),
                funcError -> new PartialResult<>(funcError.message, funcError.partialResult.map(f -> f.apply(arg)))
            ),
            argError -> Either.right(functionResult.result.map(
                func -> new PartialResult<>(argError.message, argError.partialResult.map(func)),
                funcError -> new PartialResult<>(
                    appendMessages(argError.message, funcError.message),
                    argError.partialResult.flatMap(a -> funcError.partialResult.map(f -> f.apply(a)))
                )
            ))
        ), lifecycle.add(functionResult.lifecycle));
    }

    /**
     * Combines this result with another result using the given function.
     *
     * @param function The function to apply to the wrapped values of this and the given result.
     * @param second   The second {@link DataResult} value.
     * @param <R2>     The type of the second result value.
     * @param <S>      The type of the output result value.
     * @return A {@link DataResult} wrapping the application of the {@code function} to the provided results.
     * @apiNote This is an arity-2 specialization for {@link #map(Function)}.
     * @see #map(Function)
     */
    public <R2, S> DataResult<S> apply2(final BiFunction<R, R2, S> function, final DataResult<R2> second) {
        return unbox(instance().apply2(function, this, second));
    }

    /**
     * Combines this result with another result using the given function, under the "stable" lifecycle.
     *
     * <p>This method is equivalent to {@link #apply2(BiFunction, DataResult)}, except that the stable lifecycle
     * is always used.
     *
     * @param function The function to apply to the wrapped values of this and the given result.
     * @param second   The second {@link DataResult} value.
     * @param <R2>     The type of the second result value.
     * @param <S>      The type of the output result value.
     * @return A {@link DataResult} wrapping the application of the {@code function} to the provided results.
     * @see #apply2(BiFunction, DataResult)
     * @see #map(Function)
     */
    public <R2, S> DataResult<S> apply2stable(final BiFunction<R, R2, S> function, final DataResult<R2> second) {
        final Applicative<Mu, Instance.Mu> instance = instance();
        final DataResult<BiFunction<R, R2, S>> f = unbox(instance.point(function)).setLifecycle(Lifecycle.stable());
        return unbox(instance.ap2(f, this, second));
    }

    /**
     * Combines this result with two other results using the given function.
     *
     * @param function The function to apply to the wrapped values of this and the given results.
     * @param second   The second {@link DataResult} value.
     * @param third    The third {@link DataResult} value.
     * @param <R2>     The type of the second result value.
     * @param <R3>     The type of the third result value.
     * @param <S>      The type of the output result value.
     * @return A {@link DataResult} wrapping the application of the {@code function} to the provided results.
     * @apiNote This is an arity-3 specialization for {@link #map(Function)}.
     * @see #map(Function)
     */
    public <R2, R3, S> DataResult<S> apply3(final Function3<R, R2, R3, S> function, final DataResult<R2> second, final DataResult<R3> third) {
        return unbox(instance().apply3(function, this, second, third));
    }

    /**
     * If this result is an error, replaces any partial result with the supplied partial result.
     *
     * @param partial A {@link Supplier} of partial results.
     * @return A {@link DataResult} equivalent to {@code this} with the partial result set appropriately.
     */
    public DataResult<R> setPartial(final Supplier<R> partial) {
        return create(result.mapRight(r -> new PartialResult<>(r.message, Optional.of(partial.get()))), lifecycle);
    }

    /**
     * If this result is an error, replaces any partial result with the given partial result.
     *
     * @param partial A partial result.
     * @return A {@link DataResult} equivalent to {@code this} with the partial result set appropriately.
     */
    public DataResult<R> setPartial(final R partial) {
        return create(result.mapRight(r -> new PartialResult<>(r.message, Optional.of(partial))), lifecycle);
    }

    /**
     * Applies the given function to the error message contained in this result.
     *
     * @param function The function to apply.
     * @return A {@link DataResult} equivalent to {@code this}, but with any error message replaced.
     */
    public DataResult<R> mapError(final UnaryOperator<String> function) {
        return create(result.mapRight(r -> new PartialResult<>(function.apply(r.message), r.partialResult)), lifecycle);
    }

    /**
     * Returns a {@link DataResult} with the same value as this result, but with the provided lifecycle.
     */
    public DataResult<R> setLifecycle(final Lifecycle lifecycle) {
        return create(result, lifecycle);
    }

    /**
     * Returns a {@link DataResult} with the same value as this result, but with the provided lifecycle added to
     * this result's lifecycle.
     *
     * @see Lifecycle#add(Lifecycle)
     */
    public DataResult<R> addLifecycle(final Lifecycle lifecycle) {
        return create(result, this.lifecycle.add(lifecycle));
    }

    /**
     * Returns the <em>applicative type instance</em> for the type {@link DataResult}.
     *
     * @see Instance
     */
    public static Instance instance() {
        return Instance.INSTANCE;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DataResult<?> that = (DataResult<?>) o;
        return Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
    }

    /**
     * Returns a {@link String} describing this {@link DataResult}.
     */
    @Override
    public String toString() {
        return "DataResult[" + result + ']';
    }

    /**
     * A container for a partial result in an error {@link DataResult}.
     *
     * @param <R> The type of the result.
     */
    public static class PartialResult<R> {
        private final String message;
        private final Optional<R> partialResult;

        /**
         * Constructs a new {@link PartialResult} with the given message and optional result.
         *
         * @param message       The error message.
         * @param partialResult The result.
         */
        public PartialResult(final String message, final Optional<R> partialResult) {
            this.message = message;
            this.partialResult = partialResult;
        }

        /**
         * Applies the given function to the result stored in this {@link PartialResult}.
         *
         * @param function The function to apply.
         * @param <R2>     The new result type.
         * @return A {@link PartialResult} with the given function applied.
         * @apiNote This method implements the <em>functor operator</em> in the context of {@link PartialResult}.
         */
        public <R2> PartialResult<R2> map(final Function<? super R, ? extends R2> function) {
            return new PartialResult<>(message, partialResult.map(function));
        }

        /**
         * Applies the given function to the result stored in this {@link PartialResult}, flattening
         * nested {@link PartialResult} wrappers.
         *
         * @param function The function to apply.
         * @param <R2>     The new result type.
         * @return A {@link PartialResult} with the given function applied.
         * @apiNote This method implements the <em>monad operator</em> in the context of {@link PartialResult}.
         */
        public <R2> PartialResult<R2> flatMap(final Function<R, PartialResult<R2>> function) {
            if (partialResult.isPresent()) {
                final PartialResult<R2> result = function.apply(partialResult.get());
                return new PartialResult<>(appendMessages(message, result.message), result.partialResult);
            }
            return new PartialResult<>(message, Optional.empty());
        }

        /**
         * Returns the error message.
         */
        public String message() {
            return message;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final PartialResult<?> that = (PartialResult<?>) o;
            return Objects.equals(message, that.message) && Objects.equals(partialResult, that.partialResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message, partialResult);
        }

        /**
         * Returns a {@link String} describing this {@link PartialResult}.
         */
        @Override
        public String toString() {
            return "DynamicException[" + message + ' ' + partialResult + ']';
        }
    }

    /**
     * The <em>applicative functor</em> type instance for the type constructor {@link DataResult}.
     *
     * @see <a href="https://medium.com/@lettier/your-easy-guide-to-monads-applicatives-functors-862048d61610">Functors, Applicatives, and Monads</a>
     */
    public enum Instance implements Applicative<Mu, Instance.Mu> {
        /**
         * The type instance.
         */
        INSTANCE;

        /**
         * A marker class representing the meta-type constructor {@link DataResult.Instance}.
         */
        public static final class Mu implements Applicative.Mu {}

        @Override
        public <T, R> App<DataResult.Mu, R> map(final Function<? super T, ? extends R> func, final App<DataResult.Mu, T> ts) {
            return unbox(ts).map(func);
        }

        @Override
        public <A> App<DataResult.Mu, A> point(final A a) {
            return success(a);
        }

        @Override
        public <A, R> Function<App<DataResult.Mu, A>, App<DataResult.Mu, R>> lift1(final App<DataResult.Mu, Function<A, R>> function) {
            return fa -> ap(function, fa);
        }

        @Override
        public <A, R> App<DataResult.Mu, R> ap(final App<DataResult.Mu, Function<A, R>> func, final App<DataResult.Mu, A> arg) {
            return unbox(arg).ap(unbox(func));
        }

        @Override
        public <A, B, R> App<DataResult.Mu, R> ap2(final App<DataResult.Mu, BiFunction<A, B, R>> func, final App<DataResult.Mu, A> a, final App<DataResult.Mu, B> b) {
            final DataResult<BiFunction<A, B, R>> fr = unbox(func);
            final DataResult<A> ra = unbox(a);
            final DataResult<B> rb = unbox(b);

            // for less recursion
            if (fr.result.left().isPresent()
                && ra.result.left().isPresent()
                && rb.result.left().isPresent()
            ) {
                return new DataResult<>(Either.left(fr.result.left().get().apply(
                    ra.result.left().get(),
                    rb.result.left().get()
                )), fr.lifecycle.add(ra.lifecycle).add(rb.lifecycle));
            }

            return Applicative.super.ap2(func, a, b);
        }

        @Override
        public <T1, T2, T3, R> App<DataResult.Mu, R> ap3(final App<DataResult.Mu, Function3<T1, T2, T3, R>> func, final App<DataResult.Mu, T1> t1, final App<DataResult.Mu, T2> t2, final App<DataResult.Mu, T3> t3) {
            final DataResult<Function3<T1, T2, T3, R>> fr = unbox(func);
            final DataResult<T1> dr1 = unbox(t1);
            final DataResult<T2> dr2 = unbox(t2);
            final DataResult<T3> dr3 = unbox(t3);

            // for less recursion
            if (fr.result.left().isPresent()
                && dr1.result.left().isPresent()
                && dr2.result.left().isPresent()
                && dr3.result.left().isPresent()
            ) {
                return new DataResult<>(Either.left(fr.result.left().get().apply(
                    dr1.result.left().get(),
                    dr2.result.left().get(),
                    dr3.result.left().get()
                )), fr.lifecycle.add(dr1.lifecycle).add(dr2.lifecycle).add(dr3.lifecycle));
            }

            return Applicative.super.ap3(func, t1, t2, t3);
        }
    }
}

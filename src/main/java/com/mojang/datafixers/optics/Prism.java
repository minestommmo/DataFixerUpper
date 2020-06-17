// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.util.Either;

import java.util.function.Function;

/**
 * A prism is an optic that provides pattern matching against and construction from a field. It provides functionality
 * to extract a value {@code A} from the input <em>sum type</em> {@code S} and to build an object of sum type {@code T}
 * from a derived value {@code B}.
 *
 * <p>The canonical example for using a prism is to extract and update a field of a tagged {@code union} type, using
 * the C meaning of {@code union}.
 *
 * <p>In order to be a <em>lawful prism</em>, the implementations of {@link #match(Object)} and {@link #build(Object)}
 * must satisfy certain requirements. Assume that the object types {@code S} and {@code T} are implicitly convertible
 * between each other and that the field types {@code A} and {@code B} are similarly convertible. Then the following
 * rules must hold ({@code ==} here represents logical equality and not reference equality).
 *
 * <ol>
 *     <li>
 *         {@code match(build(b)) == Right(b)} - Matching against a built value yields that value.
 *     </li>
 *     <li>
 *         {@code build?(match(s)) == s} - If a field is matched, building with it yields the original object.
 *     </li>
 * </ol>
 *
 * <p>Prisms that are not <em>lawful</em> are said to be either <em>neutral</em> or <em>chaotic</em>, depending on the
 * degree to which the prism laws are broken.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @dfu.shape %.Type.[%0::%2,%1::%3]
 * @see <a href="https://en.wikipedia.org/wiki/Union_type">Union Types</a>
 */
public interface Prism<S, T, A, B> extends App2<Prism.Mu<A, B>, S, T>, Optic<Cocartesian.Mu, S, T, A, B> {
    /**
     * The witness type for {@link Prism}.
     *
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @dfu.shape %.Mu.[%^1::%0,%^2::%1]
     */
    final class Mu<A, B> implements K2 {}

    /**
     * Thunk method that casts an applied {@link Prism.Mu} to a {@link Prism}.
     *
     * @param box The boxed prism.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed prism.
     */
    static <S, T, A, B> Prism<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Prism<S, T, A, B>) box;
    }

    /**
     * Attempts to extract the input field from the input object. Returns the straightforward transformation of the
     * input object to the output object if the input field is not present.
     *
     * <p>For example, say that the input object type is {@link com.mojang.serialization.DataResult} and the output
     * object type is {@link java.util.Optional}. Calling {@code match} with the data result will return either the
     * value present in the result (if a {@link com.mojang.serialization.DataResult} is a success), or an absent
     * {@link java.util.Optional} (if the result is an error).
     *
     * @param s A value of the input object type from which to attempt to extract the input field value.
     * @return Either the value of the input field, or a value of the output object type equivalent to the input.
     * @implSpec The implementation must, in conjunction with {@link #build(Object)}, satisfy the prism laws in order
     * for this prism to be a <em>lawful prism</em>.
     */
    Either<T, A> match(final S s);

    /**
     * Constructs a value of the output object type with the given output field value.
     *
     * <p>For example, this may be used to construct an {@link java.util.Optional} or {@link com.mojang.serialization.DataResult}
     * from a plain value.
     *
     * @param b The value to construct the output object from.
     * @return A value of the output object type that contains the output field.
     * @implSpec The implementation must, in conjunction with {@link #match(Object)}, satisfy the prism laws in order
     * for this prism to be a <em>lawful prism</em>.
     */
    T build(final B b);

    /**
     * Evaluates this prism to produce a function that, when given a transformation between field types, produces a
     * transformation between object types. The transformation {@linkplain #match(Object) matches} the input object
     * against the input field, and either {@linkplain #build(Object) builds} an output object from the extracted
     * value or returns the converted input.
     *
     * @param proof The {@link Cocartesian} type class instance for the transformation type.
     * @return A function that takes a transformation between field types and produces a transformation between
     * object types.
     * @see Prism.Instance
     */
    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Cocartesian.Mu, P> proof) {
        final Cocartesian<P, ? extends Cocartesian.Mu> cocartesian = Cocartesian.unbox(proof);
        return input -> cocartesian.dimap(
            cocartesian.right(input),
            this::match,
            (Either<T, B> a) -> {
                return a.map(Function.identity(), this::build);
            }
        );
    }

    /**
     * The {@link Cocartesian} type class instance for {@link Prism}.
     *
     * @param <A2> The input field type.
     * @param <B2> The output field type.
     * @dfu.shape instance %.Type.[_::%0,_::%1]
     */
    final class Instance<A2, B2> implements Cocartesian<Mu<A2, B2>, Cocartesian.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Prism.Mu<A2, B2>, A, B>, App2<Prism.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return prismBox -> Optics.prism(
                (C c) -> Prism.unbox(prismBox).match(g.apply(c)).mapLeft(h),
                (B2 b) -> h.apply(Prism.unbox(prismBox).build(b))
            );
        }

        @Override
        public <A, B, C> App2<Prism.Mu<A2, B2>, Either<A, C>, Either<B, C>> left(final App2<Prism.Mu<A2, B2>, A, B> input) {
            final Prism<A, B, A2, B2> prism = Prism.unbox(input);
            return Optics.prism(
                (Either<A, C> either) -> either.map(
                    a -> prism.match(a).mapLeft(Either::left),
                    c -> Either.left(Either.right(c))
                ),
                (B2 b) -> Either.left(prism.build(b))
            );
        }

        @Override
        public <A, B, C> App2<Prism.Mu<A2, B2>, Either<C, A>, Either<C, B>> right(final App2<Prism.Mu<A2, B2>, A, B> input) {
            final Prism<A, B, A2, B2> prism = Prism.unbox(input);
            return Optics.prism(
                (Either<C, A> either) -> either.map(
                    c -> Either.left(Either.left(c)),
                    a -> prism.match(a).mapLeft(Either::right)
                ),
                (B2 b) -> Either.right(prism.build(b))
            );
        }
    }
}

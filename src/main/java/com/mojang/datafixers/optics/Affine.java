// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.function.Function;

/**
 * An affine is an optic that provides access and modification to a single field of an algebraic data type.
 * It provides functionality to extract the value of the input field type {@code A} from the input object type {@code S}
 * and to combine the remainder of the input {@code S} and the transformed field type {@code B} into the output object
 * type {@code T}.
 *
 * <p>An affine can be thought of as a generalization of the {@link Lens} and {@link Prism} optics.
 *
 * <p>In order to be a <em>lawful affine</em>, the implementations of {@link #preview(Object)} and {@link #set(Object, Object)}
 * must satisfy certain requirements. Assume that the object types {@code S} and {@code T} are implicitly convertible
 * between each other and that the field types {@code A} and {@code B} are similarly convertible. Then the following
 * rules must hold ({@code ==} here represents logical equality and not reference equality).
 *
 * <ol>
 *     <li>
 *         {@code set(b2, set(b1, s)) == set(b2, s)} - Setting twice is equivalent to setting once.
 *     </li>
 *     <li>
 *         {@code preview(update(b, s)) == Right(b)} - Previewing after setting yields the value used to set.
 *     </li>
 *     <li>
 *         {@code set?(preview(s), s) == s} - Setting with a previewed value yields the original object.
 *     </li>
 * </ol>
 *
 * <p>Affine optics that are not <em>lawful</em> are said to be either <em>neutral</em> or <em>chaotic</em>, depending
 * on the degree to which the affine laws are broken.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @apiNote Note that the affine laws shown here are quite similar to the lens laws. In fact, an affine optic is a
 * combination of a {@link Lens} and a {@link Prism}. Colloquially, the term "prism" may actually refer to this
 * optic, and not to {@link Prism}.
 * @dfu.shape %.Type.[%0::%2,%1::%3]
 * @see Lens
 * @see Prism
 */
public interface Affine<S, T, A, B> extends App2<Affine.Mu<A, B>, S, T>, Optic<AffineP.Mu, S, T, A, B> {
    /**
     * The witness type for {@link Affine}.
     *
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @dfu.shape %.Mu.[%^1::%0,%^2::%1]
     * @dfu.hidden
     */
    final class Mu<A, B> implements K2 {}

    /**
     * Thunk method that casts an applied {@link Affine.Mu} to a {@link Affine}.
     *
     * @param box The boxed affine.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed affine.
     * @dfu.hidden
     */
    static <S, T, A, B> Affine<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Affine<S, T, A, B>) box;
    }

    /**
     * Attempts to extract a value from the input object. Returns the residual transformation of the input
     * object to the output object if the input is not of the variant which contains the field.
     *
     * @param s The input object.
     * @return Either the extracted field value, or a corresponding residual value of the output object type.
     * @implSpec The implementation must, in conjunction with {@link #set(Object, Object)}, satisfy the affine laws
     * in order for this affine to be a <em>lawful affine</em>.
     */
    Either<T, A> preview(final S s);

    /**
     * Combines the given output field and an input object to produce an output object. If the field is already
     * present in the input object, the existing value is overwritten; otherwise the field is added.
     *
     * @param b A value of the output field type.
     * @param s A value of the input object type.
     * @return A value of the output object type that contains the output field.
     * @implSpec The implementation must, in conjunction with {@link #preview(Object)}, satisfy the affine laws
     * in order for this affine to be a <em>lawful affine</em>.
     */
    T set(final B b, final S s);

    /**
     * Evaluates this affine to produce a function that, when given a transformation between field types, produces
     * a transformation between object types. The transformation {@linkplain #preview(Object) extracts} an optional
     * value from the input object, and either {@linkplain #set(Object, Object) sets} the output field with the
     * transformed field value or returns the input converted to type {@link T} using a residual transformation.
     *
     * @param proof The {@link AffineP} type class instance for the transformation type.
     * @param <P>   The type of transformation.
     * @return A function that takes a transformation between field types and produces a transformation between
     * object types.
     * @see Affine.Instance
     */
    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends AffineP.Mu, P> proof) {
        final Cartesian<P, ? extends AffineP.Mu> cartesian = Cartesian.unbox(proof);
        final Cocartesian<P, ? extends AffineP.Mu> cocartesian = Cocartesian.unbox(proof);
        return input -> cartesian.dimap(
            cocartesian.left(cartesian.rmap(cartesian.<A, B, S>first(input), p -> set(p.getFirst(), p.getSecond()))),
            (S s) -> preview(s).map(Either::right, a -> Either.left(Pair.of(a, s))),
            (Either<T, T> e) -> {
                return e.map(Function.identity(), Function.identity());
            }
        );
    }

    /**
     * The {@link AffineP} type class instance for {@link Affine}.
     *
     * @param <A2> The input field type.
     * @param <B2> The output field type.
     * @dfu.shape instance %.Type.[_::%0,_::%1]
     */
    final class Instance<A2, B2> implements AffineP<Mu<A2, B2>, AffineP.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Affine.Mu<A2, B2>, A, B>, App2<Affine.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return affineBox -> Optics.affine(
                (C c) -> Affine.unbox(affineBox).preview(g.apply(c)).mapLeft(h),
                (b2, c) -> h.apply(Affine.unbox(affineBox).set(b2, g.apply(c)))
            );
        }

        @Override
        public <A, B, C> App2<Affine.Mu<A2, B2>, Pair<A, C>, Pair<B, C>> first(final App2<Affine.Mu<A2, B2>, A, B> input) {
            final Affine<A, B, A2, B2> affine = Affine.unbox(input);
            return Optics.affine(
                pair -> affine.preview(pair.getFirst()).mapBoth(b -> Pair.of(b, pair.getSecond()), Function.identity()),
                (b2, pair) -> Pair.of(affine.set(b2, pair.getFirst()), pair.getSecond())
            );
        }

        @Override
        public <A, B, C> App2<Affine.Mu<A2, B2>, Pair<C, A>, Pair<C, B>> second(final App2<Affine.Mu<A2, B2>, A, B> input) {
            final Affine<A, B, A2, B2> affine = Affine.unbox(input);
            return Optics.affine(
                pair -> affine.preview(pair.getSecond()).mapBoth(b -> Pair.of(pair.getFirst(), b), Function.identity()),
                (b2, pair) -> Pair.of(pair.getFirst(), affine.set(b2, pair.getSecond()))
            );
        }

        @Override
        public <A, B, C> App2<Affine.Mu<A2, B2>, Either<A, C>, Either<B, C>> left(final App2<Affine.Mu<A2, B2>, A, B> input) {
            final Affine<A, B, A2, B2> affine = Affine.unbox(input);
            return Optics.affine(
                either -> either.map(
                    a -> affine.preview(a).mapLeft(Either::left),
                    c -> Either.left(Either.right(c))
                ),
                (b, either) -> either.map(l -> Either.left(affine.set(b, l)), Either::right)
            );
        }

        @Override
        public <A, B, C> App2<Affine.Mu<A2, B2>, Either<C, A>, Either<C, B>> right(final App2<Affine.Mu<A2, B2>, A, B> input) {
            final Affine<A, B, A2, B2> affine = Affine.unbox(input);
            return Optics.affine(
                either -> either.map(
                    c -> Either.left(Either.left(c)),
                    a -> affine.preview(a).mapLeft(Either::right)
                ),
                (b, either) -> either.map(Either::left, r -> Either.right(affine.set(b, r)))
            );
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.util.Pair;

import java.util.function.Function;

/**
 * A lens is an optic the provides access and modification to a single field. It provides functionality
 * to extract a single value of the input field type {@code A} from the input object type {@code S} and
 * to combine the input {@code S} and the transformed field type {@code B} into the output object type {@code T}.
 *
 * <p>The canonical example for using a lens is to extract and update a field of a {@code struct} type, using the
 * C meaning of {@code struct}.
 *
 * <p>In order to be a <em>lawful lens</em>, the implementations of {@link #view(Object)} and {@link #update(Object, Object)}
 * must satisfy certain requirements. Assume that the object types {@code S} and {@code T} are implicitly convertible
 * between each other and that the field types {@code A} and {@code B} are similarly convertible. Then the following
 * rules must hold ({@code ==} here represents logical equality and not reference equality).
 *
 * <ol>
 *     <li>
 *         {@code update(b2, update(b1, s)) == update(b2, s)} - Updating twice is equivalent to updating once.
 *     </li>
 *     <li>
 *         {@code view(update(b, s)) == b} - Viewing after an update yields the value used to update.
 *     </li>
 *     <li>
 *         {@code update(view(s), s) == s} - Updating with a viewed value yields the original object.
 *     </li>
 * </ol>
 *
 * <p>Lenses that are not <em>lawful</em> are said to be either <em>neutral</em> or <em>chaotic</em>, depending on the
 * degree to which the lens laws are broken.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @dfu.shape %.Type.[%0::%2,%1::%3]
 * @see <a href="https://en.wikipedia.org/wiki/Record_(computer_science)">Record Types</a>
 */
public interface Lens<S, T, A, B> extends App2<Lens.Mu<A, B>, S, T>, Optic<Cartesian.Mu, S, T, A, B> {
    /**
     * The witness type for {@link Lens} with the field types applied.
     *
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @dfu.shape %.Mu.[%^1::%0,%^2::%1]
     * @dfu.hidden
     */
    final class Mu<A, B> implements K2 {}

    /**
     * The witness type for {@link Lens} with the object types applied.
     *
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @dfu.shape %.Mu.[%0::%^2,%1::%^1]
     * @dfu.hidden
     */
    final class Mu2<S, T> implements K2 {}

    /**
     * Thunk method that casts an applied {@link Lens.Mu} to a {@link Lens}.
     *
     * @param box The boxed lens.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed lens.
     * @dfu.hidden
     */
    static <S, T, A, B> Lens<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Lens<S, T, A, B>) box;
    }

    /**
     * Thunk method that casts an applied {@link Lens.Mu2} to a {@link Lens}.
     *
     * @param box The boxed lens.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed lens.
     * @dfu.hidden
     */
    static <S, T, A, B> Lens<S, T, A, B> unbox2(final App2<Mu2<S, T>, B, A> box) {
        return ((Box<S, T, A, B>) box).lens;
    }

    /**
     * Boxes the given lens into an applied {@link Lens.Mu2}.
     *
     * @param lens The lens.
     * @param <S>  The input object type.
     * @param <T>  The output object type.
     * @param <A>  The input field type.
     * @param <B>  The output field type.
     * @return The boxed lens.
     * @apiNote This method is necessary because {@link Lens} cannot extend two different instantiations of
     * {@link App2}.
     * @dfu.hidden
     */
    static <S, T, A, B> App2<Mu2<S, T>, B, A> box(final Lens<S, T, A, B> lens) {
        return new Box<>(lens);
    }

    /**
     * A container for a {@link Lens} that implements {@link App2} suitable for instantiation with {@link Mu2}.
     * Programmers will typically not explicitly use this type.
     *
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @dfu.shape %.Type.[%0::%2,%1::%3]
     * @dfu.hidden
     */
    final class Box<S, T, A, B> implements App2<Mu2<S, T>, B, A> {
        private final Lens<S, T, A, B> lens;

        /**
         * Constructs a new box wrapping the given lens.
         *
         * @param lens The lens.
         */
        public Box(final Lens<S, T, A, B> lens) {
            this.lens = lens;
        }
    }

    /**
     * Extracts a value of the input field type from the input object type.
     *
     * <p>This method is analogous to a "getter" in traditional object-oriented programming.
     *
     * @param s A value of the input object type.
     * @return The extracted value of the input field type.
     * @implSpec The implementation must, in conjunction with {@link #update(Object, Object)}, satisfy the lens laws
     * in order for this lens to be a <em>lawful lens</em>.
     */
    A view(final S s);

    /**
     * Combines a value of the output field type with a value of the input object type to produce a combined value
     * of the output object type.
     *
     * <p>This method is analogous to a "setter" in traditional object-oriented programming. Note that the given
     * field {@code b} does not necessarily have to correspond with the straightforward transformation from the
     * input object to the output object.
     *
     * @param b A value of the output field type with which to update the output object.
     * @param s A value of the input object type to be converted to a value of the output object type.
     * @return A value of the output object type, with the output field updated with the given value.
     * @implSpec The implementation must, in conjunction with {@link #view(Object)}, satisfy the lens laws in order
     * for this lens to be a <em>lawful lens</em>.
     */
    T update(final B b, final S s);

    /**
     * Evaluates this lens to produce a function that, when given a transformation between field types, produces
     * a transformation between object types. The transformation {@linkplain #view(Object) extracts} a value from
     * the input object, uses the given transformation to produce a new value of the output field type, and finally
     * uses that value to {@linkplain #update(Object, Object) update} the corresponding field in the output object.
     *
     * @param proofBox The {@link Cartesian} type class instance for the transformation type.
     * @param <P>      The type of transformation.
     * @return A function that takes a transformation between field types and produces a transformation between
     * object types.
     * @see Lens.Instance
     */
    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Cartesian.Mu, P> proofBox) {
        final Cartesian<P, ? extends Cartesian.Mu> proof = Cartesian.unbox(proofBox);
        return a -> proof.dimap(
            proof.<A, B, S>first(a),
            s -> Pair.<A, S>of(view(s), s),
            pair -> update(pair.getFirst(), pair.getSecond())
        );
    }

    /**
     * The {@link Cartesian} type class instance for {@link Lens}. This type class corresponds to the partially applied
     * {@link Lens.Mu}.
     *
     * @param <A2> The input field type.
     * @param <B2> The output field type.
     * @dfu.shape instance %.Type.[_::%0,_::%1]
     */
    final class Instance<A2, B2> implements Cartesian<Mu<A2, B2>, Cartesian.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Lens.Mu<A2, B2>, A, B>, App2<Lens.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return l -> Optics.lens(
                c -> Lens.unbox(l).view(g.apply(c)),
                (b2, c) -> h.apply(Lens.unbox(l).update(b2, g.apply(c)))
            );
        }

        @Override
        public <A, B, C> App2<Lens.Mu<A2, B2>, Pair<A, C>, Pair<B, C>> first(final App2<Lens.Mu<A2, B2>, A, B> input) {
            return Optics.lens(
                pair -> Lens.unbox(input).view(pair.getFirst()),
                (b2, pair) -> Pair.of(Lens.unbox(input).update(b2, pair.getFirst()), pair.getSecond())
            );
        }

        @Override
        public <A, B, C> App2<Lens.Mu<A2, B2>, Pair<C, A>, Pair<C, B>> second(final App2<Lens.Mu<A2, B2>, A, B> input) {
            return Optics.lens(
                pair -> Lens.unbox(input).view(pair.getSecond()),
                (b2, pair) -> Pair.of(pair.getFirst(), Lens.unbox(input).update(b2, pair.getSecond()))
            );
        }
    }
}

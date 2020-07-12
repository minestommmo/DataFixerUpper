// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.kinds.Traversable;
import com.mojang.datafixers.optics.Wander;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

/**
 * The {@link Profunctor} type class for {@link com.mojang.datafixers.optics.Traversal} optics. This type extends
 * {@link AffineP} with the method {@link #wander(Wander, App2)}, which applies a given {@link Wander} transformation
 * to the input.
 *
 * @param <P>  The type of transformation.
 * @param <Mu> The witness type for this profunctor.
 * @dfu.shape %.Type. %0
 */
public interface TraversalP<P extends K2, Mu extends TraversalP.Mu> extends AffineP<P, Mu>/*, Monoidal<P, Mu>*/ {
    /**
     * Thunk method that casts an applied {@link TraversalP.Mu} to a {@link TraversalP}.
     *
     * @param proofBox The boxed {@link TraversalP}.
     * @param <P>      The type of transformation.
     * @param <Proof>  The witness type of the traversal profunctor.
     * @return The unboxed {@link TraversalP}.
     */
    static <P extends K2, Proof extends TraversalP.Mu> TraversalP<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (TraversalP<P, Proof>) proofBox;
    }

    /**
     * The witness type for {@link TraversalP}.
     *
     * @dfu.shape %.Mu. %^1
     */
    public interface Mu extends AffineP.Mu/*, Monoidal.Mu*/ {
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<Mu>() {};
    }

    /**
     * Takes an operation defined by {@link Wander} and a transformation between field types, and produces a
     * transformation between object types.
     *
     * <p>The returned transformation should accept an object {@code S}, extract a field {@code A}, apply the given input,
     * and return the resulting {@code B} via an output object {@code T}, all in the context of some effectful computation
     * defined by the {@code wander}.
     *
     * @param wander A mapping from effectful functions on the field types to effectful functions on the object types.
     * @param input  A (non-effectful) transformation between object types.
     * @param <S>    The input object type.
     * @param <T>    The output object type.
     * @param <A>    The input field type.
     * @param <B>    The output field type.
     * @return A transformation between object types.
     * @see Wander
     * @see Traversable
     */
    <S, T, A, B> App2<P, S, T> wander(final Wander<S, T, A, B> wander, final App2<P, A, B> input);

    /*default <S, T, A, B> App2<P, S, T> wander(final Wander<S, T, A, B> wander, final App2<P, A, B> input) {
        return this.<App<Baz.Mu<T, B>, A>, App<Baz.Mu<T, B>, B>, S, T>dimap(
            traverse(new Baz.Instance<>(), input),
            s -> new Baz<T, B, A>(){
                @Override
                public <F extends K1> App<F, T> run(final Applicative<F> applicative, final FunctionType<A, App<F, B>> function) {
                    return wander.<F>wander(applicative, function).apply(s);
                }
            },
            Baz::<T, B>sold
        );
    }*/

    /*@Override
    default <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(final App<T, F> proof, final App2<P, A, B> input) {
        return traverse(Traversable.unbox(proof), input);
    }*/

    /**
     * Takes a {@link Traversable} instance and some input transformation, and produces an output transformation
     * between the effects defined by the traversable instance.
     *
     * <p>This is a specialization of {@link #wander(Wander, App2)} where {@code S' = T<A>} and {@code T' = T<B>}.
     *
     * @param traversable A {@link Traversable} instance for {@code T}.
     * @param input       A transformation between field types.
     * @param <T>         The traversable type.
     * @param <A>         The input field type.
     * @param <B>         The output field type.
     * @return A transformation between effects of the field types.
     * @implSpec The default implementation calls {@link #wander(Wander, App2)} with a {@link Wander} value that uses
     * the given {@link Traversable} to traverse inputs.
     */
    default <T extends K1, A, B> App2<P, App<T, A>, App<T, B>> traverse(final Traversable<T, ?> traversable, final App2<P, A, B> input) {
        return wander(new Wander<App<T, A>, App<T, B>, A, B>() {
            @Override
            public <F extends K1> FunctionType<App<T, A>, App<F, App<T, B>>> wander(final Applicative<F, ?> applicative, final FunctionType<A, App<F, B>> function) {
                return ta -> traversable.<F, A, B>traverse(applicative, function, ta);
            }
        }, input);
    }

    @Override
    default <A, B, C> App2<P, Pair<A, C>, Pair<B, C>> first(final App2<P, A, B> input) {
        return dimap(traverse(new Pair.Instance<>(), input), box -> box, Pair::unbox);
    }

    @Override
    default <A, B, C> App2<P, Either<A, C>, Either<B, C>> left(final App2<P, A, B> input) {
        return dimap(traverse(new Either.Instance<>(), input), box -> box, Either::unbox);
    }

    /**
     * Converts this profunctor into a {@link FunctorProfunctor} that distributes {@link Traversable} functors.
     */
    default FunctorProfunctor<Traversable.Mu, P, FunctorProfunctor.Mu<Traversable.Mu>> toFP3() {
        return new FunctorProfunctor<Traversable.Mu, P, FunctorProfunctor.Mu<Traversable.Mu>>() {
            @Override
            public <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(final App<? extends Traversable.Mu, F> proof, final App2<P, A, B> input) {
                return traverse(Traversable.unbox(proof), input);
            }
        };
    }
}

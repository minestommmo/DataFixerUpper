// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.CartesianLike;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Pair;

/**
 * A profunctor that supports converting a transformation acting on some type to and from a transformation acting
 * on pairs holding that type. This allows one to add a "pass-through" value to the type transformed via the
 * profunctor that is left untransformed.
 *
 * @param <P>  The transformation type.
 * @param <Mu> The witness type for this profunctor.
 * @dfu.shape %.Type. %0
 */
public interface Cartesian<P extends K2, Mu extends Cartesian.Mu> extends Profunctor<P, Mu> {
    /**
     * Thunk method that casts an applied {@link Cartesian.Mu} to a {@link Cartesian}.
     *
     * @param proofBox The boxed {@link Cartesian}.
     * @param <P>      The transformation type.
     * @param <Proof>  The witness type for the profunctor.
     * @return The unboxed {@link Cartesian}.
     */
    static <P extends K2, Proof extends Cartesian.Mu> Cartesian<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Cartesian<P, Proof>) proofBox;
    }

    /**
     * The witness type for {@link Cartesian}.
     *
     * @dfu.shape %.Mu. %^1
     */
    interface Mu extends Profunctor.Mu {
        /**
         * The value representing the witness type {@link Cartesian.Mu}.
         */
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<Mu>() {};
    }

    /**
     * Converts the given transformation into one that transforms the first type of a {@link Pair}. The second type
     * is not transformed - any values of that type are passed through the returned transformation unchanged.
     *
     * @param input The transformation.
     * @param <A>   The input type.
     * @param <B>   The output type.
     * @param <C>   A "pass-through" type that is not transformed.
     * @return A transformation on pairs, where the first type is transformed and the second is not.
     */
    <A, B, C> App2<P, Pair<A, C>, Pair<B, C>> first(final App2<P, A, B> input);

    /**
     * Converts the given transformation into one that transforms the second type of a {@link Pair}. The first type
     * is not transformed - any values of that type are passed through the returned transformation unchanged.
     *
     * @param input The transformation.
     * @param <A>   The input type.
     * @param <B>   The output type.
     * @param <C>   A "pass-through" type that is not transformed.
     * @return A transformation on pairs, where the second type is transformed and the first is not.
     * @implSpec The default implementation calls {@link #first(App2)} and swaps the order of the types in the
     * {@link Pair}.
     */
    default <A, B, C> App2<P, Pair<C, A>, Pair<C, B>> second(final App2<P, A, B> input) {
        return dimap(first(input), Pair::swap, Pair::swap);
    }

    /**
     * Converts this profunctor into a {@link FunctorProfunctor} that distributes {@link CartesianLike} functors.
     */
    default FunctorProfunctor<CartesianLike.Mu, P, FunctorProfunctor.Mu<CartesianLike.Mu>> toFP2() {
        return new FunctorProfunctor<CartesianLike.Mu, P, FunctorProfunctor.Mu<CartesianLike.Mu>>() {
            @Override
            public <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(final App<? extends CartesianLike.Mu, F> proof, final App2<P, A, B> input) {
                return cap(CartesianLike.unbox(proof), input);
            }

            private <A, B, F extends K1, C> App2<P, App<F, A>, App<F, B>> cap(final CartesianLike<F, C, ?> cLike, final App2<P, A, B> input) {
                return dimap(first(input), p -> Pair.unbox(cLike.to(p)), cLike::from);
            }
        };
    }
}

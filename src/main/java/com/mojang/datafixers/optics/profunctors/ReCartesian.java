// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Pair;

/**
 * A profunctor that supports converting a transformation on one of the types in a pair into a transformation
 * on that type. This type class implements the inverse of {@link Cartesian}.
 *
 * @param <P>  The transformation type.
 * @param <Mu> The witness type of this profunctor.
 * @apiNote The name {@code ReCartesian} is short for "reified cartesian", though one could also think of it as
 * a "reverse" or "inverse" operation.
 * @dfu.shape %.Type. %0
 * @see Cartesian
 */
public interface ReCartesian<P extends K2, Mu extends ReCartesian.Mu> extends Profunctor<P, Mu> {
    /**
     * Thunk method that casts an applied {@link ReCartesian.Mu} to a {@link ReCartesian}.
     *
     * @param proofBox The boxed {@link ReCartesian}.
     * @param <P>      The transformation type.
     * @param <Proof>  The witness type for the profunctor.
     * @return The unboxed {@link ReCartesian}.
     * @dfu.hidden
     */
    static <P extends K2, Proof extends ReCartesian.Mu> ReCartesian<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (ReCartesian<P, Proof>) proofBox;
    }

    /**
     * The witness type for {@link ReCartesian}.
     *
     * @dfu.shape %.Mu. %^1
     * @dfu.hidden
     */
    interface Mu extends Profunctor.Mu {}

    /**
     * Converts a transformation on the first type in a pair into a transformation on that type. This method is
     * the inverse of {@link Cartesian#first(App2)}.
     *
     * @param input The pair transformation.
     * @param <A>   The input transformed type.
     * @param <B>   The output transformed type.
     * @param <C>   The invariant second type in the pair.
     * @return A transformation that operates on the first type of the pair.
     */
    <A, B, C> App2<P, A, B> unfirst(final App2<P, Pair<A, C>, Pair<B, C>> input);

    /**
     * Converts a transformation on the second type in a pair into a transformation on that type. This method is
     * the inverse of {@link Cartesian#second(App2)}.
     *
     * @param input The pair transformation.
     * @param <A>   The input transformed type.
     * @param <B>   The output transformed type.
     * @param <C>   The invariant first type in the pair.
     * @return A transformation that operates on the second type of the pair.
     */
    <A, B, C> App2<P, A, B> unsecond(final App2<P, Pair<C, A>, Pair<C, B>> input);
}

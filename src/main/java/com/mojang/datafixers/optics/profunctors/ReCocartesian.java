// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Either;

/**
 * A profunctor that supports converting a transformation on one of the types in an {@link Either} into a transformation
 * on that type. This type class implements the inverse of {@link Cocartesian}.
 *
 * @param <P>  The transformation type.
 * @param <Mu> The witness type of this profunctor.
 * @apiNote The name {@code ReCocartesian} is short for "reified cocartesian", though one could also think of it as
 * a "reverse" or "inverse" operation.
 * @dfu.shape %.Type. %0
 * @see Cocartesian
 */
public interface ReCocartesian<P extends K2, Mu extends ReCocartesian.Mu> extends Profunctor<P, Mu> {
    /**
     * Thunk method that casts an applied {@link ReCocartesian.Mu} to a {@link ReCocartesian}.
     *
     * @param proofBox The boxed {@link ReCocartesian}.
     * @param <P>      The transformation type.
     * @param <Proof>  The witness type for the profunctor.
     * @return The unboxed {@link ReCocartesian}.
     */
    static <P extends K2, Proof extends ReCocartesian.Mu> ReCocartesian<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (ReCocartesian<P, Proof>) proofBox;
    }

    /**
     * The witness type for {@link ReCocartesian}.
     *
     * @dfu.shape %.Mu. %^1
     */
    interface Mu extends Profunctor.Mu {}

    /**
     * Converts a transformation on the left type in an {@link Either} into a transformation on that type, discarding
     * the invariant right type. This method is the inverse of {@link Cocartesian#left(App2)}.
     *
     * @param input The {@link Either} transformation.
     * @param <A>   The input transformed type.
     * @param <B>   The output transformed type.
     * @param <C>   The invariant right type in the {@link Either}.
     * @return A transformation that operates on the left type of the {@link Either}.
     */
    <A, B, C> App2<P, A, B> unleft(final App2<P, Either<A, C>, Either<B, C>> input);

    /**
     * Converts a transformation on the right type in an {@link Either} into a transformation on that type, discarding
     * the invariant left type. This method is the inverse of {@link Cocartesian#right(App2)}.
     *
     * @param input The {@link Either} transformation.
     * @param <A>   The input transformed type.
     * @param <B>   The output transformed type.
     * @param <C>   The invariant left type in the {@link Either}.
     * @return A transformation that operates on the right type of the {@link Either}.
     */
    <A, B, C> App2<P, A, B> unright(final App2<P, Either<C, A>, Either<C, B>> input);
}

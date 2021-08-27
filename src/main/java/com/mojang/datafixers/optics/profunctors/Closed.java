// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;

/**
 * A {@link Profunctor} that may be turned into a transformation on functions. This allows one to pass functions
 * through a profunctor and interleave them into the effects governed through this profunctor.
 *
 * @param <P>  The profunctor type.
 * @param <Mu> The witness type for this profunctor.
 */
public interface Closed<P extends K2, Mu extends Closed.Mu> extends Profunctor<P, Mu> {
    /**
     * Casts a boxed {@link Closed} to its unboxed form.
     *
     * @param proofBox The boxed profunctor.
     * @param <P>      The profunctor type.
     * @param <Proof>  The witness type.
     * @return The unboxed profunctor.
     */
    static <P extends K2, Proof extends Closed.Mu> Closed<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Closed<P, Proof>) proofBox;
    }

    /**
     * The witness type for a {@link Closed}.
     */
    interface Mu extends Profunctor.Mu {
        /**
         * The run time type token for {@link Closed} type contructors.
         */
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<Mu>() {};
    }

    /**
     * Partially composes the given transformation by creating a transformation on functions based on it.
     *
     * @param input The transformation.
     * @param <A>   The input type.
     * @param <B>   The output type.
     * @param <X>   An intermediary input type.
     * @return A transformation on functions from the intermediary input type to the I/O types.
     */
    <A, B, X> App2<P, FunctionType<X, A>, FunctionType<X, B>> closed(final App2<P, A, B> input);
}

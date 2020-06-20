// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.K2;

/**
 * An interface combining the {@link Cartesian} and {@link Cocartesian} type classes. The {@link com.mojang.datafixers.optics.Affine}
 * optic requires these profunctor type classes to implement its behavior.
 *
 * @param <P>  The type of transformation.
 * @param <Mu> The witness type for this profunctor.
 * @dfu.shape %.Type. %0
 */
public interface AffineP<P extends K2, Mu extends AffineP.Mu> extends Cartesian<P, Mu>, Cocartesian<P, Mu> {
    /**
     * The witness type for {@link AffineP}.
     *
     * @dfu.shape %.Mu. %^1
     */
    interface Mu extends Cartesian.Mu, Cocartesian.Mu {
        /**
         * The value representing the witness type {@link AffineP.Mu}.
         */
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<Mu>() {};
    }
}

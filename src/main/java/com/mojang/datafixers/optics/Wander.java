// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;

/**
 * An interface related to {@link com.mojang.datafixers.kinds.Traversable}, but operating on an optic's object and
 * field types rather than on a functor. This interface is primarily used in specifying the {@link Traversal} optic.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @dfu.shape %.Type.[%0::%2,%1::%3]
 * @see Traversal
 * @see com.mojang.datafixers.kinds.Traversable
 */
public interface Wander<S, T, A, B> {
    /**
     * Takes the given {@link Applicative}-effectful function over the field types and produces an effectful function
     * over the object types. The returned function applies the transformation to each field {@code A} in {@code S},
     * and combines the resulting effects into a single effect containing the transformed object {@code T}.
     *
     * @param applicative The {@link Applicative} type class instance defining the behavior of {@code F}.
     * @param input       A function from the input field type to the output field type.
     * @param <F>         The functor produced by the given function.
     * @return A function from the input object type to the output object type.
     */
    <F extends K1> FunctionType<S, App<F, T>> wander(final Applicative<F, ?> applicative, final FunctionType<A, App<F, B>> input);
}

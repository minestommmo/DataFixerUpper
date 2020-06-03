// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

/**
 * A marker interface representing an applied unary type constructor. This interface is necessary because Java does
 * not support higher-kinded types.
 *
 * <p>For a generic (or higher kinded) type {@code F<_>}, the use of {@code App<F.Mu, A>}
 * corresponds to the parameterized type {@code F<A>}. This allows algorithms to be generified over the
 * type of the constructor {@code F}, which is not otherwise possible in Java.
 *
 * @param <F> The <em>type witness</em> representing the type constructor. This is often a nested {@code Mu} empty class.
 * @param <A> The type applied to the type constructor.
 * @dfu.shape applied
 * @see K1
 */
public interface App<F extends K1, A> {
}

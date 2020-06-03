// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

/**
 * A marker interface representing an applied binary type constructor. This interface is necessary because Java does
 * not support higher-kinded types.
 *
 * <p>For a generic (or higher kinded) type {@code F<_, _>}, the use of {@code App2<F.Mu, A, B>}
 * corresponds to the parameterized type {@code F<A, B>}. This allows algorithms to be generified over the
 * type of the constructor {@code F}, which is not otherwise possible in Java.
 *
 * @param <F> The <em>type witness</em> representing the type constructor. This is often a nested {@code Mu} empty class.
 * @param <A> The first type applied to the type constructor.
 * @param <B> The second type applied to the type constructor.
 * @see K2
 * @see App
 */
public interface App2<F extends K2, A, B> {
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Profunctor;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A composition of {@linkplain Profunctor profunctors}. Where a profunctor represents a transformation from one
 * type to another, a {@link Procompose} represents the composition of these transformations. This is analogous
 * to function composition, but for a general profunctor.
 *
 * <p>A {@code Procompose<F, G, A, B, C>} is analogous to a transformation {@code F} from {@code A} to {@code C}
 * followed by a transformation {@code G} from {@code C} to {@code B}.
 *
 * @param <F> The witness type of the first transformation.
 * @param <G> The witness type of the second transformation.
 * @param <A> The input type.
 * @param <B> The output type.
 * @param <C> The intermediate type.
 * @dfu.shape %.Type.[%(0,2,4),%(1,4,3)]
 * @see Profunctor
 * @see Function#compose(Function)
 */
public final class Procompose<F extends K2, G extends K2, A, B, C> implements App2<Procompose.Mu<F, G>, A, B> {
    /**
     * Constructs a new {@link Procompose} that composes the given profunctors.
     *
     * @param first  The first profunctor.
     * @param second The second profunctor.
     */
    public Procompose(final Supplier<App2<F, A, C>> first, final App2<G, C, B> second) {
        this.first = first;
        this.second = second;
    }

    /**
     * The witness type of {@link Procompose}.
     *
     * @param <F> The type of the profunctor applied first.
     * @param <G> The type of the profunctor applied second.
     * @dfu.shape %.Mu.[%(0,^1,'?C'),%(1,'?C',^2)]
     * @dfu.hidden
     */
    public static final class Mu<F extends K2, G extends K2> implements K2 {}

    /**
     * Casts an applied {@link Procompose.Mu} to a {@link Procompose}.
     *
     * @param box The boxed {@link Procompose}.
     * @param <F> The type of the profunctor applied first.
     * @param <G> The type of the profunctor applied second.
     * @param <A> The input type.
     * @param <B> The output type.
     * @return The unboxed {@link Procompose}.
     * @dfu.hidden
     */
    public static <F extends K2, G extends K2, A, B> Procompose<F, G, A, B, ?> unbox(final App2<Mu<F, G>, A, B> box) {
        return (Procompose<F, G, A, B, ?>) box;
    }

    private final Supplier<App2<F, A, C>> first;
    private final App2<G, C, B> second;

    /**
     * The {@link Profunctor} type class instance for {@link Procompose}.
     *
     * @param <F> The type of the profunctor applied first.
     * @param <G> The type of the profunctor applied second.
     */
    static final class ProfunctorInstance<F extends K2, G extends K2> implements Profunctor<Mu<F, G>, Profunctor.Mu> {
        private final Profunctor<F, Mu> p1;
        private final Profunctor<G, Mu> p2;

        ProfunctorInstance(final Profunctor<F, Mu> p1, final Profunctor<G, Mu> p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public <A, B, C, D> FunctionType<App2<Procompose.Mu<F, G>, A, B>, App2<Procompose.Mu<F, G>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return cmp -> cap(Procompose.unbox(cmp), g, h);
        }

        private <A, B, C, D, E> App2<Procompose.Mu<F, G>, C, D> cap(final Procompose<F, G, A, B, E> cmp, final Function<C, A> g, final Function<B, D> h) {
            return new Procompose<>(() -> p1.dimap(g, Function.<E>identity()).apply(cmp.first.get()), p2.dimap(Function.<E>identity(), h).apply(cmp.second));
        }
    }

    /**
     * Returns the profunctor applied first.
     */
    public Supplier<App2<F, A, C>> first() {
        return first;
    }

    /**
     * Returns the profunctor applied second.
     */
    public App2<G, C, B> second() {
        return second;
    }
}

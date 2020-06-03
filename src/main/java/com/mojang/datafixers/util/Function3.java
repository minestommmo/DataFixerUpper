package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A function with three parameters.
 *
 * @param <T1> The first parameter type.
 * @param <T2> The second parameter type.
 * @param <T3> The third parameter type.
 * @param <R>  The return value.
 * @dfu.shape "(%0,%1,%2) %.->. %3"
 */
public interface Function3<T1, T2, T3, R> {
    /**
     * Applies this function to the parameters.
     *
     * @param t1 The first input value.
     * @param t2 The second input value.
     * @param t3 The third input value.
     * @return The result of this function.
     */
    R apply(T1 t1, T2 t2, T3 t3);

    /**
     * Curries this function into a {@link Function} returning a {@link BiFunction}.
     *
     * @return The curried function.
     */
    default Function<T1, BiFunction<T2, T3, R>> curry() {
        return t1 -> (t2, t3) -> apply(t1, t2, t3);
    }

    /**
     * Curries this function into a {@link BiFunction} returning a {@link Function}.
     *
     * @return The curried function.
     */
    default BiFunction<T1, T2, Function<T3, R>> curry2() {
        return (t1, t2) -> t3 -> apply(t1, t2, t3);
    }
}

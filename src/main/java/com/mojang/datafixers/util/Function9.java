package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A function with nine parameters.
 *
 * @param <T1> The first parameter type.
 * @param <T2> The second parameter type.
 * @param <T3> The third parameter type.
 * @param <T4> The fourth parameter type.
 * @param <T5> The fifth parameter type.
 * @param <T6> The sixth parameter type.
 * @param <T7> The seventh parameter type.
 * @param <T8> The eighth parameter type.
 * @param <T9> The ninth parameter type.
 * @param <R>  The return value.
 */
public interface Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> {
    /**
     * Applies this function to the parameters.
     *
     * @param t1 The first input value.
     * @param t2 The second input value.
     * @param t3 The third input value.
     * @param t4 The fourth input value.
     * @param t5 The fifth input value.
     * @param t6 The sixth input value.
     * @param t7 The seventh input value.
     * @param t8 The eighth input value.
     * @param t9 The ninth input value.
     * @return The result of this function.
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9);

    /**
     * Curries this function into a {@link Function} returning a {@link Function8}.
     *
     * @return The curried function.
     */
    default Function<T1, Function8<T2, T3, T4, T5, T6, T7, T8, T9, R>> curry() {
        return t1 -> (t2, t3, t4, t5, t6, t7, t8, t9) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * Curries this function into a {@link BiFunction} returning a {@link Function7}.
     *
     * @return The curried function.
     */
    default BiFunction<T1, T2, Function7<T3, T4, T5, T6, T7, T8, T9, R>> curry2() {
        return (t1, t2) -> (t3, t4, t5, t6, t7, t8, t9) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * Curries this function into a {@link Function3} returning a {@link Function6}.
     *
     * @return The curried function.
     */
    default Function3<T1, T2, T3, Function6<T4, T5, T6, T7, T8, T9, R>> curry3() {
        return (t1, t2, t3) -> (t4, t5, t6, t7, t8, t9) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * Curries this function into a {@link Function4} returning a {@link Function5}.
     *
     * @return The curried function.
     */
    default Function4<T1, T2, T3, T4, Function5<T5, T6, T7, T8, T9, R>> curry4() {
        return (t1, t2, t3, t4) -> (t5, t6, t7, t8, t9) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * Curries this function into a {@link Function5} returning a {@link Function4}.
     *
     * @return The curried function.
     */
    default Function5<T1, T2, T3, T4, T5, Function4<T6, T7, T8, T9, R>> curry5() {
        return (t1, t2, t3, t4, t5) -> (t6, t7, t8, t9) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * Curries this function into a {@link Function6} returning a {@link Function3}.
     *
     * @return The curried function.
     */
    default Function6<T1, T2, T3, T4, T5, T6, Function3<T7, T8, T9, R>> curry6() {
        return (t1, t2, t3, t4, t5, t6) -> (t7, t8, t9) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * Curries this function into a {@link Function7} returning a {@link BiFunction}.
     *
     * @return The curried function.
     */
    default Function7<T1, T2, T3, T4, T5, T6, T7, BiFunction<T8, T9, R>> curry7() {
        return (t1, t2, t3, t4, t5, t6, t7) -> (t8, t9) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * Curries this function into a {@link Function8} returning a {@link Function}.
     *
     * @return The curried function.
     */
    default Function8<T1, T2, T3, T4, T5, T6, T7, T8, Function<T9, R>> curry8() {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> t9 -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }
}

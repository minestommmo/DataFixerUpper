package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A function with eleven parameters.
 *
 * @param <T1>  The first parameter type.
 * @param <T2>  The second parameter type.
 * @param <T3>  The third parameter type.
 * @param <T4>  The fourth parameter type.
 * @param <T5>  The fifth parameter type.
 * @param <T6>  The sixth parameter type.
 * @param <T7>  The seventh parameter type.
 * @param <T8>  The eighth parameter type.
 * @param <T9>  The ninth parameter type.
 * @param <T10> The tenth parameter type.
 * @param <T11> The eleventh parameter type.
 * @param <R>   The return value.
 * @dfu.shape "(%0,%1,%2,%3,%4,%5,%6,%7,%8,%9,%10) %.->. %11"
 */
public interface Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> {
    /**
     * Applies this function to the parameters.
     *
     * @param t1  The first input value.
     * @param t2  The second input value.
     * @param t3  The third input value.
     * @param t4  The fourth input value.
     * @param t5  The fifth input value.
     * @param t6  The sixth input value.
     * @param t7  The seventh input value.
     * @param t8  The eighth input value.
     * @param t9  The ninth input value.
     * @param t10 The tenth input value.
     * @param t11 The eleventh input value.
     * @return The result of this function.
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11);

    default Function<T1, Function10<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R>> curry() {
        return t1 -> (t2, t3, t4, t5, t6, t7, t8, t9, t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default BiFunction<T1, T2, Function9<T3, T4, T5, T6, T7, T8, T9, T10, T11, R>> curry2() {
        return (t1, t2) -> (t3, t4, t5, t6, t7, t8, t9, t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default Function3<T1, T2, T3, Function8<T4, T5, T6, T7, T8, T9, T10, T11, R>> curry3() {
        return (t1, t2, t3) -> (t4, t5, t6, t7, t8, t9, t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default Function4<T1, T2, T3, T4, Function7<T5, T6, T7, T8, T9, T10, T11, R>> curry4() {
        return (t1, t2, t3, t4) -> (t5, t6, t7, t8, t9, t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default Function5<T1, T2, T3, T4, T5, Function6<T6, T7, T8, T9, T10, T11, R>> curry5() {
        return (t1, t2, t3, t4, t5) -> (t6, t7, t8, t9, t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default Function6<T1, T2, T3, T4, T5, T6, Function5<T7, T8, T9, T10, T11, R>> curry6() {
        return (t1, t2, t3, t4, t5, t6) -> (t7, t8, t9, t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default Function7<T1, T2, T3, T4, T5, T6, T7, Function4<T8, T9, T10, T11, R>> curry7() {
        return (t1, t2, t3, t4, t5, t6, t7) -> (t8, t9, t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default Function8<T1, T2, T3, T4, T5, T6, T7, T8, Function3<T9, T10, T11, R>> curry8() {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> (t9, t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, BiFunction<T10, T11, R>> curry9() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9) -> (t10, t11) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Function<T11, R>> curry10() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10) -> t11 -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }
}

package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A function with fifteen parameters.
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
 * @param <T12> The twelfth parameter type.
 * @param <T13> The thirteenth parameter type.
 * @param <T14> The fourteenth parameter type.
 * @param <T15> The fifteenth parameter type.
 * @param <R>   The return value.
 */
public interface Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> {
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
     * @param t12 The twelfth input value.
     * @param t13 The thirteenth input value.
     * @param t14 The fourteenth input value.
     * @param t15 The fifteenth input value.
     * @return The result of this function.
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13, T14 t14, T15 t15);

    default Function<T1, Function14<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> curry() {
        return t1 -> (t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default BiFunction<T1, T2, Function13<T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> curry2() {
        return (t1, t2) -> (t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function3<T1, T2, T3, Function12<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> curry3() {
        return (t1, t2, t3) -> (t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function4<T1, T2, T3, T4, Function11<T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> curry4() {
        return (t1, t2, t3, t4) -> (t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function5<T1, T2, T3, T4, T5, Function10<T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> curry5() {
        return (t1, t2, t3, t4, t5) -> (t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function6<T1, T2, T3, T4, T5, T6, Function9<T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> curry6() {
        return (t1, t2, t3, t4, t5, t6) -> (t7, t8, t9, t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function7<T1, T2, T3, T4, T5, T6, T7, Function8<T8, T9, T10, T11, T12, T13, T14, T15, R>> curry7() {
        return (t1, t2, t3, t4, t5, t6, t7) -> (t8, t9, t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function8<T1, T2, T3, T4, T5, T6, T7, T8, Function7<T9, T10, T11, T12, T13, T14, T15, R>> curry8() {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> (t9, t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, Function6<T10, T11, T12, T13, T14, T15, R>> curry9() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9) -> (t10, t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Function5<T11, T12, T13, T14, T15, R>> curry10() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10) -> (t11, t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, Function4<T12, T13, T14, T15, R>> curry11() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11) -> (t12, t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, Function3<T13, T14, T15, R>> curry12() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12) -> (t13, t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, BiFunction<T14, T15, R>> curry13() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13) -> (t14, t15) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, Function<T15, R>> curry14() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14) -> t15 -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }
}

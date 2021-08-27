// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * An optic is a generic tool used to inspect and modify part of a structure, called a focus. The strength of optics
 * lies in the fact that they are composable: they can be combined in order to "focus" on a specific sub-component
 * of a structure, with complete disregard to the remainder of the structure.
 *
 * <p>An optic can be thought to take some transformation between field types and embed it into a transformation between
 * object types. The object type is also known as a <em>source</em>, and the field type is also known as a <em>focus</em>.
 * The terms "object" and "field" are used throughout in this documentation in order to draw a parallel to the
 * object-oriented paradigm. Note, however, that an optic need not act on a literal field of a conventional object,
 * and that optics need not act on a single field, depending on the profunctor this optic takes its behavior from.
 *
 * @param <Proof> The type class this optic evaluates under.
 * @param <S>     The input object type.
 * @param <T>     The output object type.
 * @param <A>     The input field type.
 * @param <B>     The output field type.
 * @dfu.shape %.Type.[%0,%1::%3,%2::%4]
 * @see com.mojang.datafixers.optics.profunctors.Profunctor
 * @see <a href="https://medium.com/@gcanti/introduction-to-optics-lenses-and-prisms-3230e73bfcfe">An Introduction to Optics: Lenses and Prisms</a>
 */
public interface Optic<Proof extends K1, S, T, A, B> {
    /**
     * Produces a function that embeds a transformation between field types into a transformation between the
     * object types.
     *
     * @param proof The type class associated with this optic.
     * @param <P>   The type associated with the profunctor.
     * @return A function that embeds a transformation from the input field to the output field into a
     * transformation from the input object to the output object.
     */
    <P extends K2> Function<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Proof, P> proof);

    /**
     * Composes this optic with another optic that operates on the field types of this optic. This allows, for example,
     * to combine an optic for a field in some object with an optic for a subfield of that field.
     *
     * @param optic    The subfield optic.
     * @param <Proof2> The type class associated with the given optic, which must be a subclass of this optic's type
     *                 class.
     * @param <A1>     The subfield input type.
     * @param <B1>     The subfield output type.
     * @return An optic operating on the original object, but for the subfields.
     * @implSpec The default implementation returns a {@link CompositionOptic}.
     * @see CompositionOptic
     */
    default <Proof2 extends Proof, A1, B1> Optic<Proof2, S, T, A1, B1> compose(final Optic<? super Proof2, A, B, A1, B1> optic) {
        return new CompositionOptic<>(this, optic);
    }

    /**
     * Composes this optic with another optic that operates on the field types of this optic without checking for
     * type compatibility. This allows, for example, to combine an optic for a field in some object with an optic for
     * a subfield of that field.
     *
     * <p><strong>Warning: This method is unsound. Care must be taken that the profunctor types for this optic and the
     * given optic actually have a common descendant.</strong>
     *
     * @param optic    The subfield optic.
     * @param <Proof2> The type class that should be associated with the composition optic.
     * @param <A1>     The subfield input type.
     * @param <B1>     The subfield output type.
     * @return An optic operating on the original object, but for the subfields.
     * @implSpec The default implementation returns a {@link CompositionOptic}.
     * @see #compose(Optic)
     * @see CompositionOptic
     */
    @SuppressWarnings("unchecked")
    default <Proof2 extends K1, A1, B1> Optic<?, S, T, A1, B1> composeUnchecked(final Optic<?, A, B, A1, B1> optic) {
        return new CompositionOptic<Proof2, S, T, A, B, A1, B1>((Optic<? super Proof2, S, T, A, B>) this, (Optic<? super Proof2, A, B, A1, B1>) optic);
    }

    /**
     * An optic that represents the composition of two compatible optics. Two optics are compatible if
     *
     * <ol>
     *     <li>The type classes the two optics use have a common subtype.</li>
     *     <li>The fields types of the first optic correspond to the object types of the second optic.</li>
     * </ol>
     *
     * <p>The composition acts on the objects of the first optic and the fields of the second optic.
     *
     * @param <Proof> The type class associated with this optic, which must be a subtype of the outer and inner optics' type.
     * @param <S>     The input object type.
     * @param <T>     The output object type.
     * @param <A>     The input field type.
     * @param <B>     The output field type.
     * @param <A1>    The input subfield type.
     * @param <B1>    The output subfield type.
     * @dfu.shape %.Type.[%0,%1::%3::%%5,%2::%4::%6]
     */
    final class CompositionOptic<Proof extends K1, S, T, A, B, A1, B1> implements Optic<Proof, S, T, A1, B1> {
        /**
         * The outer optic, which acts on the object and field types.
         */
        protected final Optic<? super Proof, S, T, A, B> outer;
        /**
         * The inner optic, which acts on the field and subfield types.
         */
        protected final Optic<? super Proof, A, B, A1, B1> inner;

        /**
         * Constructs a new composition from the given compatible optics.
         *
         * @param outer The outer optic.
         * @param inner The inner optic.
         */
        public CompositionOptic(final Optic<? super Proof, S, T, A, B> outer, final Optic<? super Proof, A, B, A1, B1> inner) {
            this.outer = outer;
            this.inner = inner;
        }

        @Override
        public <P extends K2> Function<App2<P, A1, B1>, App2<P, S, T>> eval(final App<? extends Proof, P> proof) {
            return outer.eval(proof).compose(inner.eval(proof));
        }

        @Override
        public String toString() {
            return "(" + outer + " ◦ " + inner + ")";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CompositionOptic<?, ?, ?, ?, ?, ?, ?> that = (CompositionOptic<?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equals(outer, that.outer) && Objects.equals(inner, that.inner);
        }

        @Override
        public int hashCode() {
            return Objects.hash(outer, inner);
        }

        /**
         * Returns the outer optic, which acts on the object and field types.
         */
        public Optic<? super Proof, S, T, A, B> outer() {
            return outer;
        }

        /**
         * Returns the inner optic, which acts on the field and subfield types.
         */
        public Optic<? super Proof, A, B, A1, B1> inner() {
            return inner;
        }
    }

    /**
     * Refines the type class accepted by this optic to the given type, checking against the given bounds.
     *
     * @param proofBounds The lower bounds of {@code Proof2}.
     * @param proof       The type tag for the refined profunctor type.
     * @param <Proof2>    The refined type class.
     * @return This optic, refined to accept the given type class, or an absent optional if the bounds could
     * not be satisfied.
     */
    @SuppressWarnings("unchecked")
    default <Proof2 extends K1> Optional<Optic<? super Proof2, S, T, A, B>> upCast(final Set<TypeToken<? extends K1>> proofBounds, final TypeToken<Proof2> proof) {
        if (proofBounds.stream().allMatch(bound -> bound.isSupertypeOf(proof))) {
            return Optional.of((Optic<? super Proof2, S, T, A, B>) this);
        }
        return Optional.empty();
    }
}

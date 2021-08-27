/**
 * Interfaces for concrete optics, implementations of common optics, and utilities for constructing optics.
 *
 * <p>Optics are a generic tool used to inspect and modify part of a structure, called a focus. The strength of optics
 * lies in the fact that they are composable: they can be combined in order to "focus" on a specific sub-component
 * of a structure, with complete disregard to the remainder of the structure.
 *
 * <p>An optic can be thought to take some transformation between field types and embed it into a transformation between
 * object types. The object type is also known as a <em>source</em>, and the field type is also known as a <em>focus</em>.
 * The terms "object" and "field" are used throughout this documentation in order to draw a parallel to the
 * object-oriented paradigm. Note, however, that an optic need not act on a literal field of a conventional object,
 * and that optics need not act on a single field, depending on the profunctor this optic takes its behavior from.
 *
 * <h2>Types of Optic</h2>
 *
 * <p>In the most literal sense, an optic is a meta-transformation: a transformation over transformations.
 * Profunctor optics model all optics as functions from field-type transformations to object-type transformations.
 *
 * <pre>{@code
 * P<A, B> -> P<S, T>
 * }</pre>
 *
 * <p>The way in which field-type transformations are turned into object-type transformations varies among the different
 * optics.
 *
 * <ul>
 *     <li>
 *         {@link com.mojang.datafixers.optics.Adapter} performs an information-preserving transformation, such as
 *         boxing and unboxing.
 *     </li>
 *     <li>
 *         {@link com.mojang.datafixers.optics.Lens} accesses a field of a product (record) type.
 *     </li>
 *     <li>
 *         {@link com.mojang.datafixers.optics.Prism} accesses a field of a sum (variant) type.
 *     </li>
 *     <li>
 *         {@link com.mojang.datafixers.optics.Affine} is equivalent to a composition of a
 *         {@link com.mojang.datafixers.optics.Lens} and a {@link com.mojang.datafixers.optics.Prism}.
 *     </li>
 *     <li>
 *         {@link com.mojang.datafixers.optics.Traversal} combines an {@link com.mojang.datafixers.optics.Affine}
 *         with an {@link com.mojang.datafixers.kinds.Applicative Applicative} effect.
 *     </li>
 * </ul>
 *
 * <p>Different optics have different constraints on the transformation {@code P}. For example, the
 * {@link com.mojang.datafixers.optics.Lens} optic requires that {@code P} be able to inspect product types, while the
 * {@link com.mojang.datafixers.optics.Prism} optic requires that {@code P} be able to inspect sum types. These
 * constraints are represented using type classes defined in the profunctor subpackage.
 *
 * <h2>Using Optics</h2>
 *
 * <p>As each concrete optic is itself a transformation, each optic interface defines a type class instance
 * that corresponds to the constraints required to transform {@code P}. Optics may be composed using the dedicated
 * method {@link com.mojang.datafixers.optics.Optic#compose(com.mojang.datafixers.optics.Optic)} or they may be
 * evaluated using {@link com.mojang.datafixers.optics.Optic#eval} and composed via typical function composition.
 *
 * @see com.mojang.datafixers.optics.Optic
 * @see com.mojang.datafixers.optics.profunctors.Profunctor
 * @see <a href="https://github.com/hablapps/DontFearTheProfunctorOptics">Don't Fear the Profunctor Optics</a>
 */
package com.mojang.datafixers.optics;
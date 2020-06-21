// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A combined {@link MapEncoder} and {@link MapDecoder}.
 *
 * <p>A {@link MapCodec} is a specialized version of a {@link Codec} that serializes and deserializes a fixed set of
 * record fields. While a {@link MapCodec} is not itself a {@link Codec}, it may be turned into one via the
 * {@link #codec()} method.
 *
 * <p>Implementations of {@link MapCodec} are immutable once created. The methods defined in this interface never
 * mutate the codec in a way visible to the outside.
 *
 * @param <A> The type this {@link MapCodec} serializes and deserializes.
 * @implNote The default methods in this interface that return map codecs or {@linkplain Codec codecs}
 * wrap this codec without adding any debugging context. These methods should be overridden if deeply nested
 * codecs are undesirable or additional debugging context is desired.
 * @see Codec
 */
public abstract class MapCodec<A> extends CompressorHolder implements MapDecoder<A>, MapEncoder<A> {
    /**
     * Returns a {@link RecordCodecBuilder} that encodes and decodes the field defined by this map codec.
     *
     * <p>Multiple {@link RecordCodecBuilder} objects, each wrapping one map codec, may be combined in order
     * to create a codec that can serialize and deserialize whole objects.
     *
     * @param getter A function that extracts a value to encode from some object type.
     * @param <O>    The object type that holds a value this map codec can operate on.
     * @return A {@link RecordCodecBuilder} that encodes and decodes values extracted from {@code getter}.
     * @see RecordCodecBuilder
     */
    public final <O> RecordCodecBuilder<O, A> forGetter(final Function<O, A> getter) {
        return RecordCodecBuilder.of(getter, this);
    }

    /**
     * Creates a {@link MapCodec} given a map encoder and a map decoder.
     *
     * @param encoder The {@link MapEncoder} that the returned map codec uses to encode values.
     * @param decoder The {@link MapDecoder} that the returned map codec uses to decode values.
     * @param <A>     The type that the returned {@link MapCodec} operates on.
     * @return A {@link MapCodec} that encodes and decodes values based on the provided encoder and decoder.
     */
    public static <A> MapCodec<A> of(final MapEncoder<A> encoder, final MapDecoder<A> decoder) {
        return of(encoder, decoder, "MapCodec[" + encoder + " " + decoder + "]");
    }

    /**
     * Creates a named {@link MapCodec} given a map encoder and a map decoder. The returned map codec will use the
     * given name in its string representation.
     *
     * @param encoder The {@link MapEncoder} that the returned map codec uses to encode values.
     * @param decoder The {@link MapDecoder} that the returned map codec uses to decode values.
     * @param name    The name that is displayed in the {@link #toString()} representation of the returned map codec.
     * @param <A>     The type that the returned {@link MapCodec} operates on.
     * @return A {@link MapCodec} that encodes and decodes values based on the provided encoder and decoder.
     */
    public static <A> MapCodec<A> of(final MapEncoder<A> encoder, final MapDecoder<A> decoder, final String name) {
        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.concat(encoder.keys(ops), decoder.keys(ops));
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return encoder.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    /**
     * Returns a {@link MapCodec} that operates on the same type as this map codec, but that extracts the values from
     * a record field with the given name. This method can be used to nest records within one another.
     *
     * @param name The name of the field values are extracted from.
     * @return A {@link MapCodec} that extracts values to be passed to this map codec from a record field.
     * @implSpec This implementation is equivalent to {@code codec().fieldOf(name)}.
     */
    public MapCodec<A> fieldOf(final String name) {
        return codec().fieldOf(name);
    }

    /**
     * Sets the {@link Lifecycle} of the results this map codec returns.
     *
     * @param lifecycle The lifecycle to use.
     * @return A map codec equivalent to this map codec, but using the given lifecycle for the encoded and decoded
     * results.
     */
    @Override
    public MapCodec<A> withLifecycle(final Lifecycle lifecycle) {
        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapCodec.this.keys(ops);
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapCodec.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return MapCodec.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return MapCodec.this.toString();
            }
        };
    }

    /**
     * A {@link Codec} adapter for a {@link MapCodec}.
     *
     * @param <A> The type this codec serializes and deserializes.
     * @see MapCodec
     */
    public static final class MapCodecCodec<A> implements Codec<A> {
        private final MapCodec<A> codec;

        /**
         * Constructs a new {@link MapCodecCodec} with the given underlying map codec.
         *
         * @param codec The map codec that backs this codec.
         */
        public MapCodecCodec(final MapCodec<A> codec) {
            this.codec = codec;
        }

        /**
         * Returns the underlying {@link MapCodec} backing this codec.
         */
        public MapCodec<A> codec() {
            return codec;
        }

        @Override
        public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
            return codec.compressedDecode(ops, input).map(r -> Pair.of(r, input));
        }

        @Override
        public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
            return codec.encode(input, ops, codec.compressedBuilder(ops)).build(prefix);
        }

        @Override
        public String toString() {
            return codec.toString();
        }
    }

    /**
     * Returns a {@link Codec} that implements the same operations as this map codec.
     */
    public Codec<A> codec() {
        return new MapCodecCodec<>(this);
    }

    /**
     * Sets this map codec to produce {@linkplain Lifecycle#stable() stable} results.
     *
     * @return A map codec that produces results with the stable lifecycle.
     * @see #withLifecycle(Lifecycle)
     * @see Lifecycle#stable()
     */
    public MapCodec<A> stable() {
        return withLifecycle(Lifecycle.stable());
    }

    /**
     * Sets this map codec to produce {@linkplain Lifecycle#deprecated(int) deprecated} results.
     *
     * @param since The deprecation version.
     * @return A map codec that produces results with a deprecated lifecycle.
     * @see #withLifecycle(Lifecycle)
     * @see Lifecycle#deprecated(int)
     */
    public MapCodec<A> deprecated(final int since) {
        return withLifecycle(Lifecycle.deprecated(since));
    }

    /**
     * Transforms this map codec into another map codec using the given invertible mapping functions.
     *
     * <p>This method performs a {@code map} operation on both {@link MapDecoder} and {@link MapEncoder}.
     *
     * @param to   A function from this map codec's type to the new type.
     * @param from A function from the new type to this map codec's type.
     * @param <S>  The new type.
     * @return A map codec for the new type.
     * @implSpec The default implementation calls {@link #comap(Function)} and {@link #map(Function)} on this map codec.
     * @see #map(Function)
     * @see #comap(Function)
     */
    public <S> MapCodec<S> xmap(final Function<? super A, ? extends S> to, final Function<? super S, ? extends A> from) {
        return MapCodec.of(comap(from), map(to), toString() + "[xmapped]");
    }

    /**
     * Transforms this map codec into another map codec using the given partially invertible partial function.
     *
     * <p>This method performs a {@code flatMap} operation on both {@link MapDecoder} and {@link MapEncoder}.
     *
     * @param to   A partial function form this map codec's type to the new type.The value and any errors are wrapped in
     *             a {@link DataResult}.
     * @param from A partial function from the new type to this map codec's type. The value and any errors are wrapped in
     *             a {@link DataResult}.
     * @param <S>  The new type.
     * @return A codec for the new type.
     * @implSpec The default implementation calls {@link #flatComap(Function)} and {@link #flatMap(Function)}.
     * @see #flatMap(Function)
     * @see #flatComap(Function)
     */
    public <S> MapCodec<S> flatXmap(final Function<? super A, ? extends DataResult<? extends S>> to, final Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(flatComap(from), flatMap(to), toString() + "[flatXmapped]");
    }

    /**
     * Creates a map codec where the encoding and decoding of the given fields depends on the values of the fields
     * decoded using this map codec.
     *
     * <p>This is similar to {@link Codec#dispatch(String, Function, Function)}, except that the fields that are
     * extracted are the same fields that are dispatched.
     *
     * @param initialInstance A codec that specifies the set of dependent fields.
     * @param splitter        A function that takes an object and extracts both a set of fields and the codecs associated
     *                        with those fields, for that particular object.
     * @param combiner        A function that combines an object and a set of decoded fields.
     * @param <E>             The type representing the collection of dependent fields.
     * @return A map codec that delegates the given fields to some other codecs based on the values of fields decoded
     * using this map codec.
     * @see Codec#dispatch(Function, Function)
     * @see MapCodec.Dependent
     */
    public <E> MapCodec<A> dependent(final MapCodec<E> initialInstance, final Function<A, Pair<E, MapCodec<E>>> splitter, final BiFunction<A, E, A> combiner) {
        return new Dependent<>(this, initialInstance, splitter, combiner);
    }

    /**
     * A map codec that changes how some of its fields are encoded and decoded based on the values of other fields.
     * This allows one to decode some set of key fields, then decide based on those fields how to decode the remaining
     * fields.
     *
     * <p>The fields that are decoded based on other fields are called <em>dependent fields</em>.
     *
     * @param <O> The type this map codec serializes and deserializes.
     * @param <E> The set of dependent fields.
     */
    private static class Dependent<O, E> extends MapCodec<O> {
        private final MapCodec<E> initialInstance;
        private final Function<O, Pair<E, MapCodec<E>>> splitter;
        private final MapCodec<O> codec;
        private final BiFunction<O, E, O> combiner;

        /**
         * Creates a new {@link Dependent} using the given functionality.
         *
         * @param codec           A map codec that encodes and decodes the key fields.
         * @param initialInstance A map codec that defines the set of dependent fields.
         * @param splitter        A function that extracts the set of dependent fields and codecs for the dependent fields
         *                        based on the value of the key fields.
         * @param combiner        A function that combines the dependent fields and the key fields.
         */
        public Dependent(final MapCodec<O> codec, final MapCodec<E> initialInstance, final Function<O, Pair<E, MapCodec<E>>> splitter, final BiFunction<O, E, O> combiner) {
            this.initialInstance = initialInstance;
            this.splitter = splitter;
            this.codec = codec;
            this.combiner = combiner;
        }

        @Override
        public <T> Stream<T> keys(final DynamicOps<T> ops) {
            return Stream.concat(codec.keys(ops), initialInstance.keys(ops));
        }

        @Override
        public <T> DataResult<O> decode(final DynamicOps<T> ops, final MapLike<T> input) {
            return codec.decode(ops, input).flatMap((O base) ->
                splitter.apply(base).getSecond().decode(ops, input).map(e -> combiner.apply(base, e)).setLifecycle(Lifecycle.experimental())
            );
        }

        @Override
        public <T> RecordBuilder<T> encode(final O input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
            codec.encode(input, ops, prefix);
            final Pair<E, MapCodec<E>> e = splitter.apply(input);
            e.getSecond().encode(e.getFirst(), ops, prefix);
            return prefix.setLifecycle(Lifecycle.experimental());
        }
    }

    @Override
    public abstract <T> Stream<T> keys(final DynamicOps<T> ops);

    /**
     * A unary operator applied on the result obtained from decoding or encoding from a {@link MapCodec}. This can be
     * used to directly transform the {@link DataResult} returned from the decoding and encoding methods.
     *
     * @param <A> The type this operator transforms.
     */
    interface ResultFunction<A> {
        /**
         * Applies a transformation to the result of decoding an object. This transformation may read
         * more fields from the map, or transform successes into errors and visa-versa.
         *
         * @param ops   The {@link DynamicOps} instance defining the serialized form.
         * @param input The input map passed to {@link MapDecoder#decode(DynamicOps, MapLike)}.
         * @param a     The result returned from {@link MapDecoder#decode(DynamicOps, MapLike)}.
         * @param <T>   The type of the serialized form.
         * @return The transformed result.
         */
        <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a);

        /**
         * Applies a transformation to the result of encoding an object. This transformation may add more data to
         * the record builder, or transform successes into errors and visa-versa.
         *
         * @param ops   The {@link DynamicOps} instance defining the serialized form.
         * @param input The input object passed to {@link MapEncoder#encode(Object, DynamicOps, RecordBuilder)}.
         * @param t     The result returned from {@link MapEncoder#encode(Object, DynamicOps, RecordBuilder)}.
         * @param <T>   The type of the serialized form.
         * @return The transformed result.
         */
        <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t);
    }

    /**
     * Transforms the {@link DataResult} returned from this map codec's {@link #encode(Object, DynamicOps, RecordBuilder)}
     * and {@link #decode(DynamicOps, MapLike)} methods.
     *
     * @param function The transformation to apply to results.
     * @return A map codec that applies the given transformation after encoding and decoding values.
     */
    private MapCodec<A> mapResult(final ResultFunction<A> function) {
        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapCodec.this.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return function.coApply(ops, input, MapCodec.this.encode(input, ops, prefix));
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return function.apply(ops, input, MapCodec.this.decode(ops, input));
            }

            @Override
            public String toString() {
                return MapCodec.this + "[mapResult " + function + "]";
            }
        };
    }

    /**
     * Provides a callback to run and a default value to return if a decoding or encoding error occurs. The returned
     * map codec will attempt to promote a partial result before falling back to the provided default value.
     *
     * @param onError A callback to run when a decoding or encoding error occurs. The callback receives the error message.
     * @param value   The default value to use if a decoding error occurs and no partial result is present.
     * @return A map codec that returns the default value from its decode method if decoding returns an error.
     */
    public MapCodec<A> withDefault(final Consumer<String> onError, final A value) {
        return withDefault(DataFixUtils.consumerToFunction(onError), value);
    }

    /**
     * Provides an error transformation and a default value to return if a decoding or encoding error occurs. The
     * returned map codec will attempt to promote a partial result before falling back to the provided default value.
     *
     * @param onError A function that transforms the error message when a decoding or encoding error occurs.
     * @param value   The default value to use if a decoding error occurs and no partial result is present.
     * @return A map codec that returns the default value from its decode method if decoding returns an error.
     */
    public MapCodec<A> withDefault(final UnaryOperator<String> onError, final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.mapError(onError).result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t.mapError(onError);
            }

            @Override
            public String toString() {
                return "WithDefault[" + onError + " " + value + "]";
            }
        });
    }

    /**
     * Provides a callback to run and a default value to supply if a decoding or encoding error occurs. The returned map
     * codec will attempt to promote a partial result before falling back to the provided default value.
     *
     * @param onError A callback to run when a decoding or encoding error occurs.
     * @param value   A supplier of the default value to use if a decoding error occurs and no partial result is present.
     * @return A map codec that returns the default value from its decode method if decoding returns an error.
     */
    public MapCodec<A> withDefault(final Consumer<String> onError, final Supplier<? extends A> value) {
        return withDefault(DataFixUtils.consumerToFunction(onError), value);
    }

    /**
     * Provides an error transformation and a default supply to return if a decoding or encoding error occurs. The
     * returned map codec will attempt to promote a partial result before falling back to the provided default value.
     *
     * @param onError A function that transforms the error message when a decoding or encoding error occurs.
     * @param value   A supplier of default value to use if a decoding error occurs and no partial result is present.
     * @return A map codec that returns the default value from its decode method if decoding returns an error.
     */
    public MapCodec<A> withDefault(final UnaryOperator<String> onError, final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.mapError(onError).result().orElseGet(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t.mapError(onError);
            }

            @Override
            public String toString() {
                return "WithDefault[" + onError + " " + value.get() + "]";
            }
        });
    }

    /**
     * Provides a default value to return if a decoding or encoding error occurs. The returned map codec will attempt
     * to promote a partial result before falling back to the provided default value.
     *
     * @param value The default value to use if a decoding error occurs and no partial result is present.
     * @return A map codec that returns the default value from its decode method if decoding returns an error.
     */
    public MapCodec<A> withDefault(final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t;
            }

            @Override
            public String toString() {
                return "WithDefault[" + value + "]";
            }
        });
    }

    /**
     * Provides a default value to supply if a decoding or encoding error occurs. The returned map codec will attempt
     * to promote a partial result before falling back to the provided default value.
     *
     * @param value A supplier of the default value to use if a decoding error occurs and no partial result is present.
     * @return A map codec that returns the default value from its decode method if decoding returns an error.
     */
    public MapCodec<A> withDefault(final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.result().orElseGet(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t;
            }

            @Override
            public String toString() {
                return "WithDefault[" + value.get() + "]";
            }
        });
    }

    /**
     * Returns a map codec that always provides the same value. As the value is constant, the returned map codec does
     * not add any fields to the output.
     *
     * @param defaultValue The value to provide when decoding.
     * @param <A>          The type of object the returned map codec operates on.
     * @return A map codec that always encodes and decodes the given value.
     */
    static <A> MapCodec<A> unit(final A defaultValue) {
        return unit(() -> defaultValue);
    }

    /**
     * Returns a map codec that always provides the supplied value. As the value is constant, the returned map codec
     * does not add any fields to the output.
     *
     * @param defaultValue A supplier of the default value.
     * @param <A>          The type of object the returned map codec operates on.
     * @return A map codec that always encodes and decodes the given value.
     * @see #unit(Object)
     */
    static <A> MapCodec<A> unit(final Supplier<A> defaultValue) {
        return MapCodec.of(Encoder.empty(), Decoder.unit(defaultValue));
    }
}

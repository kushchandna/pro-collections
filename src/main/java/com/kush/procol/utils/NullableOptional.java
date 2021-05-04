package com.kush.procol.utils;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class NullableOptional<T> implements Serializable {

    private static final long serialVersionUID = 8050216993224563343L;

    private static final NullableOptional<?> EMPTY = new EmptyOptional();

    public static <T> NullableOptional<T> of(T value) {
        return new WithData<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> NullableOptional<T> empty() {
        return (NullableOptional<T>) EMPTY;
    }

    public abstract T get();

    public abstract boolean isPresent();

    public boolean isAbsent() {
        return !isPresent();
    }

    public T orElse(T defaultValue) {
        return isPresent() ? get() : defaultValue;
    }

    public T orElse(Supplier<T> defaultValueSupplier) {
        return isPresent() ? get() : defaultValueSupplier.get();
    }

    public static <T> Optional<T> toJavaOptional(NullableOptional<T> nullableOptional) {
        return nullableOptional.isPresent() ? Optional.ofNullable(nullableOptional.get()) : Optional.empty();
    }

    public static <T> NullableOptional<T> fromJavaOptional(Optional<T> optional) {
        return optional.isPresent() ? NullableOptional.of(optional.get()) : NullableOptional.empty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (isPresent()) {
            T value = get();
            result = prime * result + (value == null ? 0 : value.hashCode());
            result = prime * result + 1231;
        } else {
            result = prime * result + 1237;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NullableOptional<?> other = (NullableOptional<?>) obj;
        if (isPresent() != other.isPresent()) {
            return false;
        }
        if (!isPresent()) {
            return true;
        }
        T thisValue = get();
        Object otherValue = other.get();
        if (thisValue == null) {
            if (otherValue != null) {
                return false;
            }
        } else if (!thisValue.equals(otherValue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return isPresent() ? "NullableOptional [value = " + get() + "]" : "NullableOptional [<empty>]";
    }

    private static final class WithData<T> extends NullableOptional<T> implements Serializable {

        private static final long serialVersionUID = 160081137897172403L;

        private final T value;

        private WithData(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public boolean isPresent() {
            return true;
        }
    }

    private static final class EmptyOptional extends NullableOptional<Object> implements Serializable {

        private static final long serialVersionUID = -4321705464457308263L;

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public Object get() {
            throw new IllegalStateException("No value present");
        }
    }
}

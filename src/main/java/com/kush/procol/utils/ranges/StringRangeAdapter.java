package com.kush.procol.utils.ranges;

import java.util.function.Function;

public class StringRangeAdapter<T> implements RangeAdapter<String, T> {

    private final Function<String, T> valueReader;
    private final String separator;
    private final String emptyValueRepresentation;

    public StringRangeAdapter(Function<String, T> valueReader, String separator,
            String emptyValueRepresentation) {
        this.valueReader = valueReader;
        this.separator = separator;
        this.emptyValueRepresentation = emptyValueRepresentation;
    }

    @Override
    public Range<T> toRange(String text) {
        String[] valuesStartEnd = getStartEndValues(text);

        Range.Builder<T> rangeBuilder = Range.builder();

        String startValue = valuesStartEnd[0].trim();
        if (!emptyValueRepresentation.equals(startValue)) {
            T value = valueReader.apply(startValue);
            rangeBuilder = rangeBuilder.startingFrom(value, isStartInclusive(text));
        }

        String endValue = valuesStartEnd[1].trim();
        if (!emptyValueRepresentation.equals(endValue)) {
            T value = valueReader.apply(endValue);
            rangeBuilder = rangeBuilder.endingAt(value, isEndInclusive(text));
        }

        return rangeBuilder.build();
    }

    @Override
    public String fromRange(Range<T> range) {
        return new StringBuilder()
            .append(range.isStartInclusive() ? '[' : '(')
            .append(range.getStart().isPresent() ? range.getStart().get() : emptyValueRepresentation)
            .append(separator)
            .append(range.getEnd().isPresent() ? range.getEnd().get() : emptyValueRepresentation)
            .append(range.isEndInclusive() ? ']' : ')')
            .toString();
    }

    public static <T> String write(Range<T> range) {
        return new StringRangeAdapter<T>(null, " - ", "*").fromRange(range);
    }

    public static <T> Range<T> parse(String text, Function<String, T> valueReader) {
        return new StringRangeAdapter<>(valueReader, " - ", "*").toRange(text);
    }

    public static Range<Integer> parseInt(String text) {
        return parse(text, Integer::parseInt);
    }

    private String[] getStartEndValues(String text) {
        String valuesText = text.substring(1, text.length() - 1);
        String[] valuesStartEnd = valuesText.split(separator);
        if (valuesStartEnd.length != 2) {
            throw new IllegalArgumentException();
        }
        return valuesStartEnd;
    }

    private boolean isEndInclusive(String text) {
        char lastChar = text.charAt(text.length() - 1);
        if (lastChar == ']') {
            return true;
        } else if (lastChar == ')') {
            return false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private boolean isStartInclusive(String text) {
        char firstChar = text.charAt(0);
        if (firstChar == '[') {
            return true;
        } else if (firstChar == '(') {
            return false;
        } else {
            throw new IllegalArgumentException();
        }
    }
}

package org.example.validation;

import java.util.Arrays;

/**
 * Represents a single validation failure: message key and arguments for i18n resolution.
 */
public record ValidationError(String messageKey, Object[] args) {

    public static ValidationError of(String messageKey, Object... args) {
        return new ValidationError(messageKey, args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationError that = (ValidationError) o;
        return messageKey.equals(that.messageKey) && Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = messageKey.hashCode();
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }
}

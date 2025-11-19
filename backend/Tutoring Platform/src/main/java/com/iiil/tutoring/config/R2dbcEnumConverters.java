package com.iiil.tutoring.config;

import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.enums.SessionType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * R2DBC custom converters for enums
 */
public class R2dbcEnumConverters {

    @ReadingConverter
    @Component
    public static class SessionStatusReadConverter implements Converter<String, SessionStatus> {
        @Override
        public SessionStatus convert(@NonNull String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }
            
            // Map database values to enum constants
            return switch (source.toLowerCase()) {
                case "demandee" -> SessionStatus.DEMANDEE;
                case "confirmee" -> SessionStatus.CONFIRMEE;
                case "en_cours" -> SessionStatus.EN_COURS;
                case "terminee" -> SessionStatus.TERMINEE;
                case "annulee" -> SessionStatus.ANNULEE;
                default -> throw new IllegalArgumentException("Unknown SessionStatus value: " + source);
            };
        }
    }

    @WritingConverter
    @Component
    public static class SessionStatusWriteConverter implements Converter<SessionStatus, String> {
        @Override
        public String convert(@NonNull SessionStatus source) {
            return source != null ? source.getValue() : null;
        }
    }

    @ReadingConverter
    @Component
    public static class SessionTypeReadConverter implements Converter<String, SessionType> {
        @Override
        public SessionType convert(@NonNull String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }
            
            // Map database values to enum constants
            return switch (source.toLowerCase()) {
                case "en_ligne" -> SessionType.EN_LIGNE;
                case "presentiel" -> SessionType.PRESENTIEL;
                default -> throw new IllegalArgumentException("Unknown SessionType value: " + source);
            };
        }
    }

    @WritingConverter
    @Component
    public static class SessionTypeWriteConverter implements Converter<SessionType, String> {
        @Override
        public String convert(@NonNull SessionType source) {
            return source != null ? source.getValue() : null;
        }
    }

    /**
     * Get list of all custom converters
     */
    public static List<Object> getConverters() {
        return Arrays.asList(
            new SessionStatusReadConverter(),
            new SessionStatusWriteConverter(),
            new SessionTypeReadConverter(),
            new SessionTypeWriteConverter()
        );
    }
}
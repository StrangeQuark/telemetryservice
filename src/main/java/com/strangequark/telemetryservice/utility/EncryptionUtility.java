package com.strangequark.telemetryservice.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
public class EncryptionUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionUtility.class);
    private static final String ALGORITHM = "AES";
    private static final String ENCRYPTION_KEY = resolveKey();
    private static final boolean ENCRYPT_DATA_AT_REST = resolveEncryptionEnabled();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static String resolveKey() {
        LOGGER.info("Attempting to resolve encryption key");

        String key = System.getProperty("ENCRYPTION_KEY");
        if (key == null) {
            LOGGER.info("Unable to grab from properties, attempt with environment variables");
            key = System.getenv("ENCRYPTION_KEY");
        }
        if (key == null || key.length() != 32) {
            LOGGER.error("ENCRYPTION_KEY must be set and 32 chars long");
            throw new IllegalStateException("ENCRYPTION_KEY must be set and 32 chars long");
        }

        LOGGER.info("Encryption key successfully resolved");
        return key;
    }

    private static boolean resolveEncryptionEnabled() {
        LOGGER.info("Attempting to resolve ENCRYPT_DATA_AT_REST env var");

        String flag = System.getProperty("ENCRYPT_DATA_AT_REST");
        if (flag == null) {
            LOGGER.info("Unable to grab from properties, attempt with environment variables");
            flag = System.getenv("ENCRYPT_DATA_AT_REST");
        }
        if(flag == null) {
            LOGGER.info("ENCRYPT_DATA_AT_REST is not set, defaulting to TRUE");
            flag = "true";
        }

        boolean enabled = Boolean.parseBoolean(flag);
        LOGGER.info("Data-at-rest encryption is " +  (enabled ? "ENABLED" : "DISABLED"));

        return enabled;
    }

    public static String encrypt(String raw) {
        if (!ENCRYPT_DATA_AT_REST)
            return raw;

        try {
            SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return Base64.getEncoder().encodeToString(cipher.doFinal(raw.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    public static String decrypt(String encrypted) {
        if (!ENCRYPT_DATA_AT_REST)
            return encrypted;

        try {
            SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new StringWriter(),
                new StringReader(),
                new LocalDateTimeWriter(),
                new LocalDateTimeReader(),
                new MapWriter(),
                new MapReader()
        ));
    }

    @WritingConverter
    public static class StringWriter implements Converter<String, String> {
        @Override
        public String convert(String source) {
            return EncryptionUtility.encrypt(source);
        }
    }

    @ReadingConverter
    public static class StringReader implements Converter<String, String> {
        @Override
        public String convert(String source) {
            return EncryptionUtility.decrypt(source);
        }
    }

    @WritingConverter
    public static class LocalDateTimeWriter implements Converter<LocalDateTime, String> {
        @Override
        public String convert(LocalDateTime source) {
            if (source == null) return null;
            return EncryptionUtility.encrypt(source.format(FORMATTER));
        }
    }

    @ReadingConverter
    public static class LocalDateTimeReader implements Converter<String, LocalDateTime> {
        @Override
        public LocalDateTime convert(String source) {
            if (source == null) return null;
            String decrypted = EncryptionUtility.decrypt(source);
            return LocalDateTime.parse(decrypted, FORMATTER);
        }
    }

    @WritingConverter
    public static class MapWriter implements Converter<Map<String, Object>, String> {
        @Override
        public String convert(Map<String, Object> source) {
            if (source == null) return null;
            try {
                String json = objectMapper.writeValueAsString(source);
                return EncryptionUtility.encrypt(json);
            } catch (Exception e) {
                throw new RuntimeException("Error encrypting metadata map", e);
            }
        }
    }

    @ReadingConverter
    public static class MapReader implements Converter<String, Map<String, Object>> {
        @Override
        public Map<String, Object> convert(String source) {
            if (source == null) return null;
            try {
                String decrypted = EncryptionUtility.decrypt(source);
                return objectMapper.readValue(decrypted, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error decrypting metadata map", e);
            }
        }
    }
}

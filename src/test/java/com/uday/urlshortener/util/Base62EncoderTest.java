package com.uday.urlshortener.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Base62Encoder Tests")
class Base62EncoderTest {

    @Test
    @DisplayName("encode(0) should return '0'")
    void encode_zero_returnsZero() {
        assertThat(Base62Encoder.encode(0)).isEqualTo("0");
    }

    @Test
    @DisplayName("encode(1) should return '1'")
    void encode_one_returnsOne() {
        assertThat(Base62Encoder.encode(1)).isEqualTo("1");
    }

    @Test
    @DisplayName("encode(62) should return '10' (base-62 rollover)")
    void encode_62_returnsBase62Rollover() {
        assertThat(Base62Encoder.encode(62)).isEqualTo("10");
    }

    @ParameterizedTest(name = "encode({0}) should produce non-empty string")
    @CsvSource({"1", "10", "100", "1000", "999999", "123456789"})
    @DisplayName("encode should always produce non-empty, alphanumeric strings")
    void encode_positiveLong_producesNonEmptyAlphanumeric(long input) {
        String encoded = Base62Encoder.encode(input);
        assertThat(encoded)
                .isNotEmpty()
                .matches("[0-9A-Za-z]+");
    }

    @Test
    @DisplayName("Different inputs produce different codes (no collision)")
    void encode_differentInputs_produceDifferentCodes() {
        String code1 = Base62Encoder.encode(1L);
        String code2 = Base62Encoder.encode(2L);
        String code3 = Base62Encoder.encode(100L);
        assertThat(code1).isNotEqualTo(code2);
        assertThat(code2).isNotEqualTo(code3);
    }

    @Test
    @DisplayName("encode is deterministic — same input always gives same output")
    void encode_deterministic_sameInputSameOutput() {
        long input = 987654321L;
        assertThat(Base62Encoder.encode(input)).isEqualTo(Base62Encoder.encode(input));
    }

    @Test
    @DisplayName("Larger numbers produce longer or equal length codes")
    void encode_largerNumbers_longerCode() {
        String small = Base62Encoder.encode(1L);
        String large = Base62Encoder.encode(Long.MAX_VALUE);
        assertThat(large.length()).isGreaterThanOrEqualTo(small.length());
    }
}

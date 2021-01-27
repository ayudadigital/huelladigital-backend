package com.huellapositiva.unit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.huellapositiva.domain.util.StringUtils.maskEmailAddress;
import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsShould {

    @ParameterizedTest
    @MethodSource("emailAddressProvider")
    void should_mask_email_addresses(String emailAddress, String expectedMaskedEmailAddress) {
        assertThat(maskEmailAddress(emailAddress)).isEqualTo(expectedMaskedEmailAddress);
    }

    private static Stream<Arguments> emailAddressProvider() {
        return Stream.of(Arguments.of("a@a.com", "a*a*c*m"),
                Arguments.of("foo@gmail.com", "f*o*g*a*l*c*m")
        );
    }
}

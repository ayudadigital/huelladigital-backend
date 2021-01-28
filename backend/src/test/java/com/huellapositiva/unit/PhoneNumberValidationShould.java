package com.huellapositiva.unit;

import com.huellapositiva.domain.model.valueobjects.PhoneNumber;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberValidationShould {

    @ParameterizedTest
    @MethodSource("provideCorrectPhoneNumbers")
    void return_true_if_string_is_phone_number(PhoneNumber phoneNumber){
        boolean isPhoneNumber = !PhoneNumber.isNotPhoneNumber(phoneNumber.getPhone());
        assertThat(isPhoneNumber).isTrue();
    }

    private static Stream<PhoneNumber> provideCorrectPhoneNumbers() {
        return Stream.of(
                PhoneNumber.builder()
                        .phone("+1 112345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("+12 112345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("+123 112345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("+23 112345")
                        .build(),
                PhoneNumber.builder()
                        .phone("+23 1123456")
                        .build(),
                PhoneNumber.builder()
                        .phone("+23 11234567890123")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectPhoneNumbers")
    void return_false_if_string_is_not_phone_number(PhoneNumber phoneNumber){
        boolean isPhoneNumber = !PhoneNumber.isNotPhoneNumber(phoneNumber.getPhone());
        assertThat(isPhoneNumber).isFalse();
    }

    private static Stream<PhoneNumber> provideIncorrectPhoneNumbers() {
        return Stream.of(
                PhoneNumber.builder()
                        .phone("12 112345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("-12 112345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("+1234 112345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("+1x2 112345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("+ 112345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("+23 11234")
                        .build(),
                PhoneNumber.builder()
                        .phone("+23 1123c5678")
                        .build(),
                PhoneNumber.builder()
                        .phone("+23 1123456789012345678")
                        .build(),
                PhoneNumber.builder()
                        .phone("234 1123")
                        .build(),
                PhoneNumber.builder()
                        .phone("+2x3 11234124s41")
                        .build(),
                PhoneNumber.builder()
                        .phone("+1231123412441")
                        .build(),
                PhoneNumber.builder()
                        .phone("1231123412441")
                        .build()
        );
    }
}

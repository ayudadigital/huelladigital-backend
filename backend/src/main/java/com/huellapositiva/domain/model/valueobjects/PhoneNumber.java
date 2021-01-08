package com.huellapositiva.domain.model.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PhoneNumber {

    private String phone;

    /**
     * The phone number have the pattern '+123 123458...23235'
     *
     * @param number
     * @return
     */
    public static boolean isNotPhoneNumber(String number) {
        final String[] phone = number.split(" ");
        final String phonePreffix = phone[0];
        final String numberPhone = phone[1];
        final int phoneLength = phonePreffix.length();
        final boolean validationPreffix = phonePreffix.contains("+")
                && phoneLength >= 2
                && phoneLength <= 4
                && isNumeric(phonePreffix.substring(1));
        final boolean validationNumberPhone = numberPhone.length() <= 14
                && isNumeric(numberPhone);
        return !(validationPreffix && validationNumberPhone);
    }

    private static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }
}

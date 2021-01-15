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
     * @param number phone number
     * @return return a boolean if the phone number is a real phone number
     */
    public static boolean isNotPhoneNumber(String number) {
        final String[] phone = number.split(" ");
        if (phone.length != 2) {
            return true;
        }
        final String phonePreffix = phone[0];
        final String numberPhone = phone[1];
        final int preffixLength = phonePreffix.length();
        final boolean validationPreffix = phonePreffix.contains("+")
                && preffixLength >= 2
                && preffixLength <= 4
                && isNumeric(phonePreffix.substring(1));
        final boolean validationNumberPhone = numberPhone.length() >= 6
                && numberPhone.length() <= 14
                && isNumeric(numberPhone);
        return !(validationPreffix && validationNumberPhone);
    }

    private static boolean isNumeric(String s) {
        return s != null && s.matches("\\d*");
    }
}

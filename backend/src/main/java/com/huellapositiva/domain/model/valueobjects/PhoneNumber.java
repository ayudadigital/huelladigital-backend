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
        final String phonePrefix = phone[0];
        final String numberPhone = phone[1];
        final int prefixLength = phonePrefix.length();
        final boolean validationPrefix = phonePrefix.contains("+")
                && prefixLength >= 2
                && prefixLength <= 4
                && isNumeric(phonePrefix.substring(1));
        final boolean validationNumberPhone = numberPhone.length() >= 6
                && numberPhone.length() <= 14
                && isNumeric(numberPhone);
        return !(validationPrefix && validationNumberPhone);
    }

    private static boolean isNumeric(String s) {
        return s.matches("\\d*");
    }
}

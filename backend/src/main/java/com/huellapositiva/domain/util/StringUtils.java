package com.huellapositiva.domain.util;

import javax.validation.constraints.NotNull;

public class StringUtils {

    private StringUtils() {
    }

    /**
     * Mask an email address.
     *
     * Useful when logging user's PII (Personally Identifiable Infomation).
     *
     * @param emailAddress Email address to mask
     * @return
     */
    public static String maskEmailAddress(@NotNull String emailAddress) {
        char[] maskedEmailAddress = new char[emailAddress.length()];
        for (int i = 0; i < emailAddress.length(); i++) {
            maskedEmailAddress[i] = i % 2 == 0 ? emailAddress.charAt(i) : '*';
        }
        return new String(maskedEmailAddress);
    }
}

package com.huellapositiva.domain.model.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AdditionalInformation {
    private String information;

    public static boolean isLengthInvalid(String additionalInformation) {
        return additionalInformation != null && additionalInformation.length() > 500;
    }
}

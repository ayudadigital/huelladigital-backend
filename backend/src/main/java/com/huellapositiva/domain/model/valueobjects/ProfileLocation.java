package com.huellapositiva.domain.model.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProfileLocation {
    private final String province;
    private final String town;
    private final Integer zipCode;
    private final String address;
}

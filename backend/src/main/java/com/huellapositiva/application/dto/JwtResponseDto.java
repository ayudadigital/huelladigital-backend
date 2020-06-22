package com.huellapositiva.application.dto;

import lombok.Value;

@Value
public class JwtResponseDto {

    String accessToken;

    String refreshToken;
}

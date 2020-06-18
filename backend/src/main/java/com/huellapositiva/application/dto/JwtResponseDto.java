package com.huellapositiva.application.dto;

import lombok.Value;

import java.io.Serializable;

@Value
public class JwtResponseDto implements Serializable {

    String accessToken;

    String refreshToken;
}

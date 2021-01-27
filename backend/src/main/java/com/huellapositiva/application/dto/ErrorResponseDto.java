package com.huellapositiva.application.dto;

import lombok.Value;

import java.util.List;

@Value
public class ErrorResponseDto {

    String message;

    List<String> details;
}

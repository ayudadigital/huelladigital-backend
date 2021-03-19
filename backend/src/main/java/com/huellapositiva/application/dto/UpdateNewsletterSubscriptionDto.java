package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
public class UpdateNewsletterSubscriptionDto {
    @Schema(
            description = "Whether the user wants to be subscribed to the newsletter",
            example = "true"
    )
    boolean subscribed;
}

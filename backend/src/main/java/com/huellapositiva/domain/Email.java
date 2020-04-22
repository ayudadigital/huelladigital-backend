package com.huellapositiva.domain;

import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Email {
    private String subject;
    private String from;
    private String to;
    private String body;

    public static Email createFrom(EmailConfirmation emailConfirmation, EmailTemplate emailTemplate) {
        return new Email();
    }
    //                .builder()
//                .from("noreply@huellapositiva.com")
//                .to(dto.getEmail())
//                .subject("Confirmación de la cuenta en huellapositiva")
//                .body("Bienvenido a huella positiva, por favor confirme su email haciendo click aquí")
//                .build();
}

package com.huellapositiva.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PhysicalEmail {
    private String subject;
    private String from;
    private String to;
    private String body;

}

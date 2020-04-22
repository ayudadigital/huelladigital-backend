package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.PhysicalEmail;

public interface EmailService {

    boolean sendEmail (PhysicalEmail email);
}

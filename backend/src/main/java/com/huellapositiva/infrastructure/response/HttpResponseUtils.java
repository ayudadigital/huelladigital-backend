package com.huellapositiva.infrastructure.response;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Component
public class HttpResponseUtils {
    public void setForbidden(HttpServletResponse res) {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    public void setUnauthorized(HttpServletResponse res) {
        res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}

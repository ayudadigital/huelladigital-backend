package com.huellapositiva.infrastructure;

import com.huellapositiva.infrastructure.security.JwtAuthenticationFilter;
import com.huellapositiva.infrastructure.security.JwtAuthorizationFilter;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${cors.allow.origin}")
    private String origin;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigure() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")
                        .allowedOrigins(origin)
                        .maxAge(3600);
            }
        };
    }

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    private static final AntPathRequestMatcher[] AUTH_ALLOWSLIST = new AntPathRequestMatcher[]{
            new AntPathRequestMatcher("/swagger-ui/**", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/v3/api-docs/**", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/actuator/health", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/actuator/info", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/v1/email-confirmation/**", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/v1/esal", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/v1/volunteers", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/v1/authentication/refresh", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/v1/authentication/login", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/v1/proposals/**", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/v1/contactperson", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/v1/handling-password/sendRecoveryPasswordEmail", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/v1/handling-password/changePassword/{hash}", HttpMethod.POST.name())
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and()
                .csrf()
                .ignoringAntMatchers("/api/v1/authentication/login", "/api/v1/volunteers", "/api/v1/contactperson")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and().authorizeRequests()
                .requestMatchers(AUTH_ALLOWSLIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JwtAuthenticationFilter(authenticationManagerBean(), userDetailsService, jwtService))
                .addFilter(new JwtAuthorizationFilter(authenticationManagerBean(), jwtService, AUTH_ALLOWSLIST))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}


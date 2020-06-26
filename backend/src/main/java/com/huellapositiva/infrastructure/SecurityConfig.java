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
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        RequestMatcher csrfRequestMatcher = new RequestMatcher() {
            private final AntPathRequestMatcher[] csrfDisabledPaths = {
                    new AntPathRequestMatcher("/api/v1/volunteers/login"),
                    new AntPathRequestMatcher("/api/v1/volunteers/register"),
                    new AntPathRequestMatcher("/actuator/health")
            };
            @Override
            public boolean matches(HttpServletRequest request) {
                for (AntPathRequestMatcher rm : csrfDisabledPaths) {
                    if (rm.matches(request) || request.getMethod().equals("GET")) {
                        return false;
                    }
                }
                return true;
            }
        };

        http.cors().and()
                .csrf().requireCsrfProtectionMatcher(csrfRequestMatcher)
                .csrfTokenRepository(new CookieCsrfTokenRepository())
                .and().authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/volunteers/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/refresh").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/email-confirmation/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/volunteers/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JwtAuthenticationFilter(authenticationManagerBean(), userDetailsService, jwtService))
                .addFilter(new JwtAuthorizationFilter(authenticationManagerBean(), jwtService))
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


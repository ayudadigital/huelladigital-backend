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


    private static final String[] AUTH_ALLOWSLIST = {
            // api
            "/api/v1/volunteers/login",
            "/api/v1/volunteers/register",

            // -- swagger ui
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**"
            // other public endpoints of your API may be appended to this array
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/**").permitAll()
                    .anyRequest().permitAll();

//            http.authorizeRequests()
//                .antMatchers(AUTH_ALLOWSLIST).permitAll()
//                    .anyRequest().authenticated();


//
//                http.cors().and()
//                .csrf()
//                    .ignoringAntMatchers(AUTH_ALLOWSLIST)
//                    .csrfTokenRepository(new CookieCsrfTokenRepository())
//                .and().authorizeRequests()
//                .antMatchers(AUTH_ALLOWSLIST).permitAll()
//                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
//                .antMatchers(HttpMethod.POST, "/api/v1/volunteers/register").permitAll()
//                .antMatchers(HttpMethod.POST, "/api/v1/refresh").permitAll()
//                .antMatchers(HttpMethod.GET, "/api/v1/email-confirmation/**").permitAll()
//                .antMatchers(HttpMethod.POST, "/api/v1/volunteers/login").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .addFilter(new JwtAuthenticationFilter(authenticationManagerBean(), userDetailsService, jwtService))
//                .addFilter(new JwtAuthorizationFilter(authenticationManagerBean(), jwtService))
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
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


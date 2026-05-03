package pe.edu.upeu.sysalmacen.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

//Clase S7
@Configuration
@EnableWebSecurity
@EnableMethodSecurity //Importante para anotaciones @PreAuthorize
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService jwtUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        //Desde Spring Boot 3.0+
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                // ── HTTP Security Headers (ISO 27001, OWASP)
                .headers(headers -> headers
                        // Evita MIME-type sniffing (CWE-430)
                        .contentTypeOptions(Customizer.withDefaults())
                        // Protección contra clickjacking
                        .frameOptions(frame -> frame.deny())
                        // HSTS: fuerza HTTPS por 1 año, incluye subdominios
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        // Content Security Policy: solo permite recursos del mismo origen
                        // Ajusta 'connect-src' con tu dominio de API en producción
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self'; " +
                                                "img-src 'self' data: res.cloudinary.com; " +
                                                "connect-src 'self'; " +
                                                "frame-ancestors 'none'"
                                ))
                        // Headers adicionales de seguridad via headerWriter
                        .addHeaderWriter((request, response) -> {
                            response.setHeader("Referrer-Policy",    "strict-origin-when-cross-origin");
                            response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
                            response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
                        })
                )
                .authorizeHttpRequests(req -> req
                        .requestMatchers(HttpMethod.POST, "/users/login",
                                "users/register").permitAll()
                        .requestMatchers(antMatcher("/mail/**")).permitAll()
                        .requestMatchers(antMatcher("/doc/**")).permitAll()
                        .requestMatchers(antMatcher("/v3/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}

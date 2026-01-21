package com.example.system_tickets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. RUTAS PÚBLICAS (Login, Estilos, Link del correo)
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/img/**",
                                "/olvide-password", "/enviar-recuperacion",
                                "/restablecer", "/reset-password", "/guardar-nueva-password", "/error-token").permitAll()

                        // 2. RUTA OBLIGATORIA (Usuarios logueados nuevos)
                        .requestMatchers("/obligatorio/**").authenticated()

                        // 3. RUTAS POR ROL
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/soporte/**").hasAuthority("SOPORTE")
                        .requestMatchers("/tickets/**").hasAnyAuthority("ADMIN", "SOPORTE", "FUNCIONARIO")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
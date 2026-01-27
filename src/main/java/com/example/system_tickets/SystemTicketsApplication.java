package com.example.system_tickets;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling // Mantiene activo el reloj automático
@EnableAsync      // <--- VITAL: Permite que los correos no congelen la pantalla
public class SystemTicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemTicketsApplication.class, args);
    }

    // Generador de contraseñas (Útil para desarrollo)
    @Bean
    public CommandLineRunner crearPassword() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            // Puedes cambiar esto si necesitas generar otra clave
            String rawPassword = "1234";
            String encodedPassword = encoder.encode(rawPassword);

            // Solo imprimimos si es necesario, para no ensuciar el log
            // System.out.println("Pass: " + encodedPassword);
        };
    }
}
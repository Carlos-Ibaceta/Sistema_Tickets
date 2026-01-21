package com.example.system_tickets;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync; // <--- NUEVO: Para correos rápidos
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling // Mantiene activo el reloj automático
@EnableAsync      // <--- NUEVO: Permite tareas en segundo plano (Correos)
public class SystemTicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemTicketsApplication.class, args);
    }

    // ESTE BLOQUE TE DARÁ LA CLAVE CORRECTA EN CONSOLA AL INICIAR
    @Bean
    public CommandLineRunner crearPassword() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String rawPassword = "1234";
            String encodedPassword = encoder.encode(rawPassword);

            System.out.println("=========================================");
            System.out.println("TU CONTRASEÑA ENCRIPTADA PARA '1234' ES:");
            System.out.println(encodedPassword);
            System.out.println("=========================================");
        };
    }
}
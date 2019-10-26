package fr.unice.polytech.codemara.drone.drone_service;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DroneServiceConfiguration {
    @Bean
    public DroneCommander droneService(Environment env){
        return new DroneCommander(env);
    }
}

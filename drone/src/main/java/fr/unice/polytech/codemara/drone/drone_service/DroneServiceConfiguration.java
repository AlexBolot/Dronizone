package fr.unice.polytech.codemara.drone.drone_service;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroneServiceConfiguration {
    @Bean
    public DroneCommander droneService() {
        return new DroneCommander();
    }
}

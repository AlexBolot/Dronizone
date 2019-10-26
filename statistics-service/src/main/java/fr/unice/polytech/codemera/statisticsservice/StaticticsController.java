package fr.unice.polytech.codemera.statisticsservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()

public class StaticticsController {
    @GetMapping(path = "hello/")
    public String helloWorld() {
        return "hello";
    }
}

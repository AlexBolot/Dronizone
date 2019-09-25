package fr.unice.polytech.codemara.warehouse.controller;

import fr.unice.polytech.codemara.warehouse.entities.Warehouse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(path = "/warehouse", produces = "application/json")
public class WarehouseController {
    @GetMapping("/{hello}")

    public String order_ping(@PathVariable("hello") String hello) {
        if (hello.equals("hello")) {
            return "World";

        }
        return "Hello";
    }

    @RequestMapping(method = GET, path = "/pending")
    public String getPendings() {
        return "Warehouse.getPendings().toString()";
    }
}
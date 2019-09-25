package fr.unice.polytech.codemara.warehouse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/warehouse", produces = "application/json")
public class WarehouseController {
    @GetMapping("/{hello}")
    public String order_ping(@PathVariable("hello") String hello){
        if (hello.equals("hello")){
            return "World";

        }
        return "Hello";
    }
}

package fr.unice.polytech.controller;

import fr.unice.polytech.entities.Order;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(path = "/order", produces = "application/json")
public class OrderController {

    @GetMapping("/{hello}")
    public String order_ping(@PathVariable("hello") String hello){
        if (hello.equals("hello")){
            return "World";

        }
        return "Hello";
    }

    @RequestMapping(method = POST, path = "/new")
    public Order greeting(@RequestBody String address,
                          @RequestBody String item) {
        return new Order(address, item);
    }
}

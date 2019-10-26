package fr.unice.polytech.codemara.drone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.drone_service.DroneCommander;
import fr.unice.polytech.codemara.drone.entities.*;
import fr.unice.polytech.codemara.drone.entities.command.*;
import fr.unice.polytech.codemara.drone.entities.dto.DeliveryDTO;
import fr.unice.polytech.codemara.drone.order_service.OrderService;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import fr.unice.polytech.codemara.drone.repositories.WhereaboutsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

@RestController
@RequestMapping(path = "/drone", produces = "application/json")
public class DroneController {

    private static final Logger logger = LoggerFactory.getLogger(DroneController.class);
    private final DroneCommander droneCommander;
    private final DroneRepository droneRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderService orderService;
    private final KafkaTemplate kafkaTemplate;
    private final WhereaboutsRepository whereaboutsRepository;

    private final Location baseLocation;


    public DroneController(DroneCommander droneCommander, DroneRepository droneRepository, DeliveryRepository deliveryRepository, OrderService orderService, WhereaboutsRepository whereaboutsRepository, KafkaTemplate kafkaTemplate) {
        this.droneCommander = droneCommander;
        this.droneRepository = droneRepository;
        this.deliveryRepository = deliveryRepository;
        this.orderService = orderService;
        this.whereaboutsRepository = whereaboutsRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.baseLocation = new Location(45, 7);
    }

    /**
     * Route dedicated to ask for the fleet's battery statuses
     *
     * @return JSON map containing the battery level of each drone of the fleet
     * <p>
     * # US-4 Elena can query [Mom document] to ask for the battery levels of the drone fleet
     */
    @GetMapping(path = "/fleet_battery_status")
    public String fleetBatteryStatus() {
        Iterable<Drone> drones = droneRepository.findAll();
        HashMap<Long, Double> levels = new HashMap<>();
        drones.forEach(d -> levels.put(d.getDroneID(), d.getBatteryLevel()));
        try {
            return new ObjectMapper().writeValueAsString(levels);
        } catch (JsonProcessingException e) {
            logger.error(e.toString());
        }
        return "";
    }

    /**
     * Route dedicated to change a drone's status : active or aside from the fleet
     * # US-4 Elena can notify that a drone is set aside [Mom document]
     * # US-4 Elena can notify that sidelined drone is ready for service [Mom document]
     *
     * @param droneID Identifier of the Drone to remove from the fleet
     */
    @PutMapping(path = "/set_drone_aside/{droneID}/{status}")
    public void changeDroneStatus(@PathVariable long droneID, @PathVariable String status) {
        Optional<Drone> drone = droneRepository.findById(droneID);
        drone.ifPresent(d -> {
            d.setDroneStatus(DroneStatus.valueOf(status));
            droneRepository.save(d);
        });
    }


    @PostMapping(path = "/request_delivery")
    public void requireDelivery(@RequestBody DeliveryDTO deliveryDTO) {
        Delivery delivery = new Delivery();
        delivery.setPickup_location(deliveryDTO.getPickup_location());
        delivery.setTarget_location(deliveryDTO.getTarget_location());
        delivery.setItemId(deliveryDTO.getItemId());
        delivery.setOrderId(deliveryDTO.getOrderId());
        deliveryRepository.save(delivery);
        Iterator<Drone> drones = droneRepository.getDroneByCurrentDelivery(null).iterator();
        Drone drone = null;
        if (drones.hasNext())
            drone = drones.next();
        DeliveryCommand deliveryCommand = new DeliveryCommand(drone, delivery);
        droneCommander.sendCommand(deliveryCommand);
    }

    @PostMapping(path = "/fleet/command/callback")
    public void callbackFleet() {
        DroneCommand callbackCommand = new CallbackCommand(CommandType.CALLBACK, baseLocation);
        droneCommander.broadcast(callbackCommand);
        Iterable<Delivery> deliveries = deliveryRepository.findAll();
        deliveries.forEach(orderService::cancel);
        deliveries.forEach(delivery -> kafkaTemplate.send("deliveryPostponed", String.valueOf(delivery.getOrderId())));
    }

    @GetMapping(path = "/deliveries")
    public Iterable<Delivery> deliveries() {
        return deliveryRepository.findAll();
    }

    @GetMapping("/kafkaTest")
    public void kafkaTest() {
        Delivery d;
        d = new Delivery();
        d.setOrderId(2);
        orderService.notifyDelivery(d);
    }

    @GetMapping("/kafkaTest2")
    public void kafkaTest2() {
        Delivery delivery = new Delivery();
        delivery.setId(1);
        delivery.setItemId(2);
        delivery.setOrderId(3);
        delivery.setPickup_location(new Location(45, 6));
        delivery.setTarget_location(new Location(45, 8));
        Iterator<Drone> drones = droneRepository.getDroneByCurrentDelivery(null).iterator();
        Drone drone = null;
        if (drones.hasNext())
            drone = drones.next();
        DroneCommand deliveryCommand = new DeliveryCommand(drone, delivery);
    }

    @KafkaListener(topics = "drones")
    public void listen_to_drones(String message) {
        try {
            DroneState state = new ObjectMapper().readValue(message, DroneState.class);
            Optional<Drone> result = droneRepository.findById(state.getDrone_id());

            if (state.getWhereabouts().getDistanceToTarget() < 200 && result.isPresent() && !result.get().getCurrentDelivery().isNotified()) {
                Delivery delivery = result.get().getCurrentDelivery();
                orderService.notifyDelivery(delivery);
                delivery.setNotified(true);
                deliveryRepository.save(delivery);
            }

            result.ifPresent(drone -> {
                if (drone.getDroneStatus() != state.getDroneStatus()) {
                    drone.setDroneStatus(state.getDroneStatus());
                    droneRepository.save(drone);
                }
            });
            if (state.getDrone_id() < 0) {
                Drone oldDrone = new Drone();
                oldDrone.setDroneStatus(state.getDroneStatus());
                oldDrone.setBatteryLevel(state.getBattery());
                oldDrone.setWhereabouts(whereaboutsRepository.save(state.getWhereabouts()));
                long newId = droneRepository.save(oldDrone).getDroneID();
                oldDrone.setDroneID(state.getDrone_id());
                DroneCommand initCommand = new InitCommand(oldDrone, newId);
                droneCommander.sendCommand(initCommand);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }


    }
}

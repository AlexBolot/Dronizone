package fr.unice.polytech.codemara.drone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.drone_service.DroneCommander;
import fr.unice.polytech.codemara.drone.entities.*;
import fr.unice.polytech.codemara.drone.entities.command.CallbackCommand;
import fr.unice.polytech.codemara.drone.entities.command.DroneCommand;
import fr.unice.polytech.codemara.drone.entities.command.InitCommand;
import fr.unice.polytech.codemara.drone.entities.command.ShipmentCommand;
import fr.unice.polytech.codemara.drone.entities.dto.DeliveryStatus;
import fr.unice.polytech.codemara.drone.entities.dto.DeliveryUpdateDTO;
import fr.unice.polytech.codemara.drone.entities.dto.ShipmentDTO;
import fr.unice.polytech.codemara.drone.entities.dto.ShipmentRefusedDTO;
import fr.unice.polytech.codemara.drone.order_service.OrderService;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import fr.unice.polytech.codemara.drone.repositories.ShipmentRepository;
import fr.unice.polytech.codemara.drone.repositories.WhereaboutsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

import static fr.unice.polytech.codemara.drone.entities.dto.DeliveryStatus.DELIVERED;
import static fr.unice.polytech.codemara.drone.entities.dto.DeliveryStatus.PICKING_UP;

@RestController
@RequestMapping(path = "/drone", produces = "application/json")
public class DroneController {

    private static final Logger logger = LoggerFactory.getLogger(DroneController.class);

    private final KafkaTemplate kafkaTemplate;
    private final DroneCommander droneCommander;
    private final DroneRepository droneRepository;
    private final DeliveryRepository deliveryRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderService orderService;
    private final WhereaboutsRepository whereaboutsRepository;

    private final Location baseLocation;


    public DroneController(KafkaTemplate kafkaTemplate, DroneCommander droneCommander, DroneRepository droneRepository, DeliveryRepository deliveryRepository, ShipmentRepository shipmentRepository, OrderService orderService, WhereaboutsRepository whereaboutsRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.droneCommander = droneCommander;
        this.droneRepository = droneRepository;
        this.deliveryRepository = deliveryRepository;
        this.shipmentRepository = shipmentRepository;
        this.orderService = orderService;
        this.whereaboutsRepository = whereaboutsRepository;
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
    public String fleetBatteryStatus() throws JsonProcessingException {
        Iterable<Drone> drones = droneRepository.findAll();
        HashMap<Long, Double> levels = new HashMap<>();
        drones.forEach(d -> levels.put(d.getDroneID(), d.getBatteryLevel()));
        return new ObjectMapper().writeValueAsString(levels);
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

    /**
     * Method to cancel all deliveries for all drones to the base location and send a cancel notification event
     */
    @PostMapping(path = "/fleet/command/callback")
    public void callbackFleet() {
        DroneCommand callbackCommand = new CallbackCommand(baseLocation);
        droneCommander.broadcast(callbackCommand);
        Iterable<Shipment> shipments = shipmentRepository.findAll();
        shipments.forEach(shipment -> shipment.getDeliveries().forEach(orderService::notifyDeliveryCancel));
    }

    @GetMapping(path = "/shipments")
    public Iterable<Shipment> deliveries() {
        return shipmentRepository.findAll();
    }

    @GetMapping(path = "/drones")
    public Iterable<Drone> getDrones() {
        return droneRepository.findAll();
    }

    /**
     * Method that listen if new shipments are packed and ready to be picked up by an available drone we will choose
     *
     * @param message The delivery that the drone will go pick up and deliver to customer location
     */
    @KafkaListener(topics = "shipment-packed")
    public void newShipmentPacked(String message) {
        logger.debug(message);
        try {
            ShipmentDTO shipmentDTO = new ObjectMapper().readValue(message, ShipmentDTO.class);
            Shipment shipment = new Shipment(shipmentDTO);
            shipment.getDeliveries().forEach(deliveryRepository::save);
            shipmentRepository.save(shipment);

            double weight = shipment.getShipmentWeight();

            // Get available drones that can transport this delivery (maxWeight over delivery's weight)
            Iterator<Drone> drones = droneRepository.getDronesByCurrentShipmentAndMaxWeightIsGreaterThan(null, weight).iterator();
            if (drones.hasNext()) {
                Drone drone = drones.next();
                drone.setCurrentShipment(shipment);
                drone.setCurrentDelivery(shipment.next());
                droneRepository.save(drone);
                ShipmentCommand shipmentCommand = new ShipmentCommand(drone, shipment);
                droneCommander.sendCommand(shipmentCommand);
            } else {
                String logMessage = "No drone is available for shipment of id " + shipment.getId();
                logger.info("DroneController.newOrderPacked", logMessage);
                ShipmentRefusedDTO dto = new ShipmentRefusedDTO(shipment);
                kafkaTemplate.send("shipment-refused", new ObjectMapper().writeValueAsString(dto));
            }
        } catch (IOException e) {
            logger.error("DroneController.newOrderPacked", e);
        }
    }

    /**
     * Method that listen the update status of all drone
     * If the droneId is under 0, we will send a new INITIALISATION command to set a new Id to the drone
     * If the drone change state, we will save the new state
     * If the drone is under 200 meter from the target location, we will send a soon to be delivered notification event
     *
     * @param message The status that contain the whereabouts, the id, the battery level and a timestamp
     */
    @KafkaListener(topics = "drone-status")
    public void listenToDrones(String message) {
        logger.debug(message);
        try {
            DroneState state = new ObjectMapper().readValue(message, DroneState.class);
            Optional<Drone> result = droneRepository.findById(state.getDrone_id());

            result.ifPresent(drone -> {
                logger.debug(result.toString());

                Delivery delivery = drone.getCurrentDelivery();
                double distance = state.getWhereabouts().getDistanceToTarget();

                if (distance < 200 && delivery != null && delivery.mustNotify()) {
                    orderService.notifyDeliverySoon(delivery);
                    delivery.setNotified(true);
                    deliveryRepository.save(delivery);
                }

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
            logger.error("DroneController.listen_to_drones", e);
        }
    }

    @KafkaListener(topics = "drone-delivery-update")
    public void dronesPickupReceiver(String message) {
        logger.info("Drones has pickup delivery : {}", message);
        try {
            DeliveryUpdateDTO deliveryUpdate = new ObjectMapper().readValue(message, DeliveryUpdateDTO.class);
            DeliveryStatus status = deliveryUpdate.getDeliveryStatus();

            if (status == DELIVERED) {
                handleDeliveredOrder(deliveryUpdate);
            } else if (status == PICKING_UP) {
                handlePickingDeliveryOrder(deliveryUpdate);
            }

        } catch (IOException e) {
            logger.error("DroneController.dronesPickupReceiver", e);
        }
    }

    private void handleDeliveredOrder(DeliveryUpdateDTO update) {
        Drone drone = droneRepository.findById(update.getDroneId()).orElseThrow(IllegalArgumentException::new);
        drone.setCurrentDelivery(null);
        CallbackCommand callHomeCommand = new CallbackCommand(this.baseLocation);
        callHomeCommand.setTarget(drone);
        droneRepository.save(drone);
        droneCommander.sendCommand(callHomeCommand);
    }

    private void handlePickingDeliveryOrder(DeliveryUpdateDTO update) {
        Optional<Drone> result = droneRepository.findById(update.getDroneId());
        result.ifPresent(drone -> {
            Delivery currentDelivery = drone.getCurrentDelivery();
            currentDelivery.setPickedUp(update.getDeliveryStatus() == PICKING_UP);
        });
    }
}

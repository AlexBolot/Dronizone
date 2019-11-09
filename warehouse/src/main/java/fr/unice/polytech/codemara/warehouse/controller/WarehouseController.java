package fr.unice.polytech.codemara.warehouse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.warehouse.entities.Location;
import fr.unice.polytech.codemara.warehouse.entities.ParcelStatus;
import fr.unice.polytech.codemara.warehouse.entities.Parcel;
import fr.unice.polytech.codemara.warehouse.entities.Shipment;
import fr.unice.polytech.codemara.warehouse.entities.dto.CustomerOrder;
import fr.unice.polytech.codemara.warehouse.entities.dto.PackedOrder;
import fr.unice.polytech.codemara.warehouse.entities.dto.PackedShipment;
import fr.unice.polytech.codemara.warehouse.entities.dto.ShipmentRequest;
import fr.unice.polytech.codemara.warehouse.repositories.ParcelRepository;
import fr.unice.polytech.codemara.warehouse.repositories.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/warehouse", produces = "application/json")
public class WarehouseController {

    private static final double WAREHOUSE_LON = 10.0;
    private static final double WAREHOUSE_LAT = 10.0;
    private final ParcelRepository parcelRepository;
    private final ShipmentRepository shipmentRepository;

    private final KafkaTemplate kafkaTemplate;
    private Logger logger = LoggerFactory.getLogger(WarehouseController.class);

    public WarehouseController(ParcelRepository parcelRepository, KafkaTemplate kafkaTemplate, ShipmentRepository shipmentRepository) {
        this.parcelRepository = parcelRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.shipmentRepository = shipmentRepository;
    }

    @GetMapping("/parcel")
    public Iterable<Parcel> getAllOrders() {
        return parcelRepository.findAll();
    }

    @GetMapping("/parcel/{id}")
    public String getOrder(@PathVariable("id") int id) {
        return parcelRepository.findById(id).toString();
    }

    @PutMapping("/parcel/{id}")
    public Parcel parcelReady(@PathVariable("id") int id) {
        Optional<Parcel> ready = parcelRepository.findById(id);
        if (ready.isPresent()) {
            ready.get().setStatus(ParcelStatus.READY);
            parcelRepository.save(ready.get());

//            PackedOrder packedOrder = new PackedOrder(ready.get().getOrderId(),
//                    ready.get().getStatus().toString(),
//                    ready.get().getCustomerId(),
//                    ready.get().getDeliveryLocation(),
//                    ready.get().getDeliveryLocation(),
//                    System.currentTimeMillis());
//
//            try {
//
//
//                Map<String, Object> parameters = new HashMap<>();
//                parameters.put("orderid", "id");
//                Map<String, String> position = new HashMap<>();
//                position.put("latitude", WAREHOUSE_LAT);
//                position.put("longitude", WAREHOUSE_LON);
//                parameters.put("pickup_location", position);
//                position = new HashMap<>();
//                position.put("latitude", ready.get().getDeliveryLocation().getLatitude() + "");
//                position.put("longitude", ready.get().getDeliveryLocation().getLongitude() + "");
//                parameters.put("target_location", position);
//                parameters.put("itemId", ready.get().getItemId());
//
//                kafkaTemplate.send("order-packed", new ObjectMapper().writeValueAsString(packedOrder));
//            } catch (Exception e) {
//                logger.error("WarehouseController.OrderReady ", e);
//            }
//
            return ready.get();
        }


        return null;
    }

    @PostMapping("/shipment")
    public PackedShipment shipmentReady(@RequestBody ShipmentRequest request) throws JsonProcessingException {
        Shipment shipment = new Shipment(new Location(WAREHOUSE_LON, WAREHOUSE_LAT));
        for (int i : request.getParcelsId()) {
            parcelRepository.findById(i).ifPresent(parcel -> {
                if (shipment.getParcels().contains(parcel) && parcel.getStatus() != ParcelStatus.READY) return;
                shipment.getParcels().add(parcel);
            });
        }

        shipmentRepository.save(shipment);

        List<PackedOrder> orders = new ArrayList<>();
        double totalWeight = 0;
        for (Parcel p : shipment.getParcels()) {
            orders.add(new PackedOrder(p.getOrderId(), p.getStatus().name(), p.getCustomerId(), p.getDeliveryLocation()));
            totalWeight += p.getWeight();
        }

        PackedShipment packedShipment = new PackedShipment(shipment.getPickUpLocation(), orders, totalWeight, System.currentTimeMillis());

        this.kafkaTemplate.send("shipment-packed", new ObjectMapper().writeValueAsString(packedShipment));

        logger.info("Shipment sent");

        return packedShipment;
    }

    @KafkaListener(topics = {"order-create"})
    public void orderCreate(String message) {
        try {
            CustomerOrder order = new ObjectMapper().readValue(message, CustomerOrder.class);
            Parcel parcel = new Parcel(order.getOrderId(), order.getItemId(), order.getCustomerId(), order.getWeight(), order.getDeliveryLocation(), ParcelStatus.PENDING);
            parcelRepository.save(parcel);
            logger.info("Parcel created");
        } catch (Exception e) {
            logger.error("WarehouseController.orderCreate", e);
        }
    }


}
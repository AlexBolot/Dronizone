package fr.unice.polytech.codemara.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.warehouse.entities.ParcelStatus;
import fr.unice.polytech.codemara.warehouse.entities.Parcel;
import fr.unice.polytech.codemara.warehouse.entities.dto.CustomerOrder;
import fr.unice.polytech.codemara.warehouse.entities.dto.PackedOrder;
import fr.unice.polytech.codemara.warehouse.repositories.ParcelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/warehouse", produces = "application/json")
public class WarehouseController {

    private static final String WAREHOUSE_LON = "10.0";
    private static final String WAREHOUSE_LAT = "10.0";
    private final ParcelRepository parcelRepository;

    private final KafkaTemplate kafkaTemplate;
    private Logger logger = LoggerFactory.getLogger(WarehouseController.class);

    public WarehouseController(ParcelRepository parcelRepository, KafkaTemplate kafkaTemplate) {
        this.parcelRepository = parcelRepository;
        this.kafkaTemplate = kafkaTemplate;
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
    public PackedOrder parcelReady(@PathVariable("id") int id) {
        Optional<Parcel> ready = parcelRepository.findById(id);
        if (ready.isPresent()) {
            ready.get().setStatus(ParcelStatus.READY);
            parcelRepository.save(ready.get());

            PackedOrder packedOrder = new PackedOrder(ready.get().getOrderId(),
                    ready.get().getStatus().toString(),
                    ready.get().getCustomerId(),
                    ready.get().getDeliveryLocation(),
                    ready.get().getDeliveryLocation(),
                    System.currentTimeMillis());

            try {


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

                kafkaTemplate.send("order-packed", new ObjectMapper().writeValueAsString(packedOrder));
            } catch (Exception e) {
                logger.error("WarehouseController.OrderReady ", e);
            }

            return packedOrder;
        }


        return null;
    }

    @PostMapping("/shipment")
    public void shipmentReady(@RequestBody List<Integer> parcelsId) {
        List<Parcel> parcels = new ArrayList<>();
        for (int i : parcelsId) {
            parcelRepository.findById(i).ifPresent(parcels::add);
        }


    }

    @KafkaListener(topics = {"order-create"})
    public void orderCreate(String message) {
        try {
            CustomerOrder order = new ObjectMapper().readValue(message, CustomerOrder.class);
            Parcel parcel = new Parcel(order.getOrderId(), order.getItemId(), order.getCustomerId(), order.getDeliveryLocation(), ParcelStatus.PENDING);
            parcelRepository.save(parcel);
        } catch (Exception e) {
            logger.error("WarehouseController.orderCreate", e);
        }
    }


}
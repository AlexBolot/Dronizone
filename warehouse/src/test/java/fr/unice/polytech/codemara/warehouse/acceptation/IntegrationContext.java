package fr.unice.polytech.codemara.warehouse.acceptation;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

/**
 * This should serve as singleton context for acceptation tests
 * ⚠ this is a sensible class, modification should be thoughtfull
 * trust yourself to do the right choices,
 * but read that your usage do not overlap with previously implemented one that you could reuse
 */
class IntegrationContext {

    private static IntegrationContext instance;

    KafkaMessageListenerContainer<String, String> container;

    KafkaTemplate<String, String> kafkaTemplate;

    static IntegrationContext getInstance() {
        if (instance == null) {
            instance = new IntegrationContext();
        }
        return instance;
    }


}

package com.ecommerce.discovery_service.config;

import com.ecommerce.discovery_service.search.event.ProductChangedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Bu bolumde tek bir Kafka listener var (search.listener.CatalogEventListener,
 * catalog-events topic'i, ProductChangedEvent). application.yml'de global bir
 * spring.json.value.default.type ayari YOK (eski search-service'te vardi, ama
 * bu yeni yml spec'inde yer almiyor) - bu yuzden JsonDeserializer'in hedef
 * tipini burada, tek bir ConsumerFactory + ConcurrentKafkaListenerContainerFactory
 * bean'i olarak, dogrudan koda sabitliyoruz (core-service/fulfillment-service'te
 * birden fazla listener icin uyguladigimiz desenin tek-listener'lik hali).
 * @KafkaListener containerFactory parametresiyle bu factory'yi kullaniyor.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, ProductChangedEvent> productChangedEventConsumerFactory() {
        JsonDeserializer<ProductChangedEvent> deserializer = new JsonDeserializer<>(ProductChangedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductChangedEvent> catalogEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProductChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productChangedEventConsumerFactory());
        return factory;
    }

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}

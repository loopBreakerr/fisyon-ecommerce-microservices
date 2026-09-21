package com.ecommerce.core_service.config;

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
 * order-events topic'i iki farkli listener tarafindan dinleniyor (payment.listener.OrderEventListener
 * ve inventory.listener.OrderEventListener), payment-events topic'i bir listener tarafindan
 * (order.listener.OrderPaymentEventListener). Ucu de kendi paketinde AYRI birer OrderCreatedEvent/
 * PaymentCompletedEvent class'i kullaniyor (bkz. payment.event.OrderCreatedEvent vs
 * inventory.event.OrderCreatedEvent vs order.event.PaymentCompletedEvent - bilincli olarak
 * BIRLESTIRILMEDI, izolasyon icin). Tek bir global
 * spring.kafka.consumer.properties.spring.json.value.default.type ayari BUNLARIN HICBIRI icin
 * gecerli olamaz (uc farkli hedef tip var). Cozum: her listener icin AYRI bir ConsumerFactory +
 * ConcurrentKafkaListenerContainerFactory bean'i, her biri kendi hedef event class'ina sabitlenmis
 * bir JsonDeserializer kullaniyor (ignoreTypeHeaders() ile __TypeId__ header'i yok sayiliyor - bu,
 * eski 5 serviste de ayni sebeple gerekmisti). @KafkaListener'lar containerFactory parametresiyle
 * hangi factory'yi kullanacagini belirtiyor (bkz. notification-service'teki ayni desen).
 *
 * groupId her @KafkaListener uzerinde AYRICA (payment-service-group / inventory-service-group /
 * order-service-group) belirtildigi icin - bu deger asagidaki factory'nin group-id'sini ezer -
 * uc listener merge sonrasi da farkli consumer group'larda kalmaya devam ediyor; yani order-events
 * hem payment hem inventory tarafindan tam kopya olarak islenmeye devam ediyor (fan-out korunuyor).
 *
 * catalog-events topic'i (urun olusturuldugunda/guncellendiginde ProductEventPublisher'in yayinladigi
 * ProductChangedEvent), inventory.listener.CatalogEventListener tarafindan dinleniyor - sadece
 * eventType="CREATED" olanlar islenip yeni urun icin InventoryItem olusturuluyor (initialStock ile).
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String defaultGroupId;

    @Bean
    public ConsumerFactory<String, com.ecommerce.core_service.payment.event.OrderCreatedEvent> paymentOrderEventConsumerFactory() {
        JsonDeserializer<com.ecommerce.core_service.payment.event.OrderCreatedEvent> deserializer =
                new JsonDeserializer<>(com.ecommerce.core_service.payment.event.OrderCreatedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.core_service.payment.event.OrderCreatedEvent> paymentOrderEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.core_service.payment.event.OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentOrderEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, com.ecommerce.core_service.inventory.event.OrderCreatedEvent> inventoryOrderEventConsumerFactory() {
        JsonDeserializer<com.ecommerce.core_service.inventory.event.OrderCreatedEvent> deserializer =
                new JsonDeserializer<>(com.ecommerce.core_service.inventory.event.OrderCreatedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.core_service.inventory.event.OrderCreatedEvent> inventoryOrderEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.core_service.inventory.event.OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(inventoryOrderEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, com.ecommerce.core_service.catalog.product.event.ProductChangedEvent> catalogEventConsumerFactory() {
        JsonDeserializer<com.ecommerce.core_service.catalog.product.event.ProductChangedEvent> deserializer =
                new JsonDeserializer<>(com.ecommerce.core_service.catalog.product.event.ProductChangedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.core_service.catalog.product.event.ProductChangedEvent> catalogEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.core_service.catalog.product.event.ProductChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(catalogEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, com.ecommerce.core_service.order.event.PaymentCompletedEvent> orderPaymentEventConsumerFactory() {
        JsonDeserializer<com.ecommerce.core_service.order.event.PaymentCompletedEvent> deserializer =
                new JsonDeserializer<>(com.ecommerce.core_service.order.event.PaymentCompletedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.core_service.order.event.PaymentCompletedEvent> orderPaymentEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.core_service.order.event.PaymentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderPaymentEventConsumerFactory());
        return factory;
    }

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}

package com.ecommerce.fulfillment_service.config;

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
 * order-events (notification.event.OrderCreatedEvent) topic'i notification.listener.
 * NotificationEventListener tarafindan dinleniyor. payment-events topic'i ise iki AYRI
 * listener tarafindan dinleniyor: notification.listener.NotificationEventListener
 * (notification.event.PaymentCompletedEvent) ve shipping.listener.ShippingEventListener
 * (shipping.event.PaymentCompletedEvent) - ikisi de AYNI topic'i, farkli birer Java
 * tipiyle, farkli birer bean olarak dinliyor (core-service'te payment/inventory
 * OrderEventListener'larinda uyguladigimiz ayni desen). catalog-events topic'i
 * notification.listener.CatalogEventListener tarafindan dinleniyor (seller urun
 * CRUD islemlerinde admin'lere bildirim) - notification.event.ProductChangedEvent,
 * core-service'in catalog.product.event.ProductChangedEvent'inin minimal bir kopyasi.
 *
 * Tek bir global spring.kafka.consumer.properties.spring.json.value.default.type
 * ayari BUNLARIN HICBIRI icin gecerli olamaz (uc farkli hedef tip var). Cozum: her
 * listener icin AYRI bir ConsumerFactory + ConcurrentKafkaListenerContainerFactory
 * bean'i, her biri kendi hedef event class'ina sabitlenmis bir JsonDeserializer
 * kullaniyor (ignoreTypeHeaders() ile __TypeId__ header'i yok sayiliyor).
 * @KafkaListener'lar containerFactory parametresiyle hangi factory'yi kullanacagini
 * belirtiyor.
 *
 * groupId her @KafkaListener uzerinde AYRICA belirtildigi icin (notification-service-group
 * / fulfillment-service-group) bu deger asagidaki factory'lerin group-id'sini ezer;
 * notification'in kendi group'u (notification-service-group, eski projeden aynen
 * korundu) ile shipping'in group'u (fulfillment-service-group) farkli kaldigi icin
 * payment-events her iki listener tarafindan da TAM KOPYA olarak islenmeye devam ediyor.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String defaultGroupId;

    @Bean
    public ConsumerFactory<String, com.ecommerce.fulfillment_service.notification.event.OrderCreatedEvent> orderEventConsumerFactory() {
        JsonDeserializer<com.ecommerce.fulfillment_service.notification.event.OrderCreatedEvent> deserializer =
                new JsonDeserializer<>(com.ecommerce.fulfillment_service.notification.event.OrderCreatedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.fulfillment_service.notification.event.OrderCreatedEvent> orderEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.fulfillment_service.notification.event.OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, com.ecommerce.fulfillment_service.notification.event.PaymentCompletedEvent> paymentEventConsumerFactory() {
        JsonDeserializer<com.ecommerce.fulfillment_service.notification.event.PaymentCompletedEvent> deserializer =
                new JsonDeserializer<>(com.ecommerce.fulfillment_service.notification.event.PaymentCompletedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.fulfillment_service.notification.event.PaymentCompletedEvent> paymentEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.fulfillment_service.notification.event.PaymentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, com.ecommerce.fulfillment_service.notification.event.ProductChangedEvent> catalogEventConsumerFactory() {
        JsonDeserializer<com.ecommerce.fulfillment_service.notification.event.ProductChangedEvent> deserializer =
                new JsonDeserializer<>(com.ecommerce.fulfillment_service.notification.event.ProductChangedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.fulfillment_service.notification.event.ProductChangedEvent> catalogEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.fulfillment_service.notification.event.ProductChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(catalogEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, com.ecommerce.fulfillment_service.shipping.event.PaymentCompletedEvent> shippingPaymentEventConsumerFactory() {
        JsonDeserializer<com.ecommerce.fulfillment_service.shipping.event.PaymentCompletedEvent> deserializer =
                new JsonDeserializer<>(com.ecommerce.fulfillment_service.shipping.event.PaymentCompletedEvent.class);
        deserializer.trustedPackages("*");
        deserializer.ignoreTypeHeaders();
        return new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.fulfillment_service.shipping.event.PaymentCompletedEvent> shippingPaymentEventsContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, com.ecommerce.fulfillment_service.shipping.event.PaymentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(shippingPaymentEventConsumerFactory());
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

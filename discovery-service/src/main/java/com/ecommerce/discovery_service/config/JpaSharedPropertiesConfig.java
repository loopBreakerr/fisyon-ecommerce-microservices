package com.ecommerce.discovery_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * application.yml'de TEK bir spring.jpa bloğu (ddl-auto) var, 3 datasource'un
 * (profile/review/recommendation) hepsi bunu paylaşıyor. Bu iki bean, spring.jpa.*
 * ve spring.jpa.hibernate.* property'lerini standart Spring Boot binding'iyle
 * okuyup, her domain'in kendi DataSourceConfig sınıfındaki EntityManagerFactory
 * tanımına enjekte edilmek üzere burada TEK SEFER oluşturuluyor (core-service ve
 * fulfillment-service'teki aynı desen).
 */
@Configuration(proxyBeanMethods = false)
public class JpaSharedPropertiesConfig {

    @Bean
    @ConfigurationProperties("spring.jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @ConfigurationProperties("spring.jpa.hibernate")
    public HibernateProperties hibernateProperties() {
        return new HibernateProperties();
    }
}

package com.ecommerce.fulfillment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * 2 ayri "spring.datasource.<domain>" prefix'i kullanildigi icin (bkz.
 * application.yml), Spring Boot'un standart TEK "spring.datasource" prefix'ine
 * dayanan otomatik DataSource/JPA konfigurasyonu burada devre disi birakiliyor;
 * her domain kendi DataSource/EntityManagerFactory/TransactionManager ucgenini
 * core_service.config.*DataSourceConfig siniflarinda elle taniyor (core-service'te
 * kullandigimiz ayni yontem - resmi Spring Boot "Configure Two DataSources" rehberi).
 */
@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataJpaRepositoriesAutoConfiguration.class
})
@EnableKafka
@ComponentScan(basePackages = {"com.ecommerce.fulfillment_service", "com.ecommerce.fulfillment_client"})
public class FulfillmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FulfillmentServiceApplication.class, args);
	}

}

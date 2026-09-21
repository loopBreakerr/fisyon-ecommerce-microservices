package com.ecommerce.core_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * 5 ayri "spring.datasource.<domain>" prefix'i kullanildigi icin (
 * application.yml), Spring Boot'un standart TEK "spring.datasource" prefix'ine
 * dayanan otomatik DataSource/JPA konfigurasyonu burada devre disi birakiliyor;
 * her domain kendi DataSource/EntityManagerFactory/TransactionManager ucgenini
 * core_service.config.*DataSourceConfig siniflarinda elle taniyor (resmi Spring
 * Boot "Configure Two DataSources" rehberindeki yontem).
 */
@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataJpaRepositoriesAutoConfiguration.class
})
@EnableKafka
@EnableFeignClients(basePackages = {"com.ecommerce.core_service", "com.ecommerce.client"})
@ComponentScan(basePackages = {"com.ecommerce.core_service", "com.ecommerce.client"})
public class CoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreServiceApplication.class, args);
	}

}

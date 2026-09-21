package com.ecommerce.discovery_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * 3 ayri "spring.datasource.<domain>" prefix'i kullanildigi icin (bkz.
 * application.yml - profile/review/recommendation), Spring Boot'un standart
 * TEK "spring.datasource" prefix'ine dayanan otomatik DataSource/JPA
 * konfigurasyonu burada devre disi birakiliyor; her domain kendi DataSource/
 * EntityManagerFactory/TransactionManager ucgenini config.*DataSourceConfig
 * siniflarinda elle taniyor (core-service ve fulfillment-service'te kullandigimiz
 * ayni yontem). search modulu SADECE Elasticsearch kullandigi icin bu exclude'lardan
 * etkilenmiyor - ElasticsearchRepository otomatik konfigurasyonu ayrida kalir.
 */
@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataJpaRepositoriesAutoConfiguration.class
})
@EnableKafka
public class DiscoveryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryServiceApplication.class, args);
	}

}

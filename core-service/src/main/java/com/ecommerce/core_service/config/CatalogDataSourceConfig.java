package com.ecommerce.core_service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateSettings;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

/**
 * catalog domain'i (category + product) icin ayri DataSource/EntityManagerFactory/
 * TransactionManager ucgeni. Standart Spring Boot otomatik konfigurasyonu TEK bir
 * "spring.datasource" prefix'i okuyup TEK bir DataSource bean'i uretir; burada
 * "spring.datasource.catalog" gibi custom bir prefix kullanildigi icin bu ucgen
 * elle tanimlaniyor (resmi Spring Boot "Configure Two DataSources" rehberindeki
 * yontem - bkz. docs.spring.io/spring-boot/how-to/data-access.html).
 *
 * getVendorProperties() ve jpaVendorAdapter() burada JpaBaseConfiguration/
 * HibernateJpaConfiguration'in kendi otomatik konfigurasyonda yaptigi ayni
 * mantigi elle uyguluyor (showSql/generateDdl/database/databasePlatform vendor
 * adapter'a, ddl-auto ve naming strategy'ler HibernateProperties.determineHibernateProperties
 * uzerinden) - boylece otomatik konfigurasyon devre disi birakilsa da (bkz.
 * CoreServiceApplication'daki exclude listesi) davranis birebir ayni kalir.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
        basePackages = "com.ecommerce.core_service.catalog",
        entityManagerFactoryRef = "catalogEntityManagerFactory",
        transactionManagerRef = "catalogTransactionManager"
)
public class CatalogDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.catalog")
    public DataSourceProperties catalogDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.catalog.hikari")
    public DataSource catalogDataSource(
            @Qualifier("catalogDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean catalogEntityManagerFactory(
            @Qualifier("catalogDataSource") DataSource dataSource,
            JpaProperties jpaProperties,
            HibernateProperties hibernateProperties) {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(jpaProperties.isShowSql());
        if (jpaProperties.getDatabase() != null) {
            vendorAdapter.setDatabase(jpaProperties.getDatabase());
        }
        if (jpaProperties.getDatabasePlatform() != null) {
            vendorAdapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
        }
        vendorAdapter.setGenerateDdl(jpaProperties.isGenerateDdl());

        Map<String, Object> vendorProperties = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());

        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
                vendorAdapter, ds -> vendorProperties, null);

        return builder
                .dataSource(dataSource)
                .packages("com.ecommerce.core_service.catalog")
                .persistenceUnit("catalog")
                .build();
    }

    @Bean
    public PlatformTransactionManager catalogTransactionManager(
            @Qualifier("catalogEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

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
 * inventory domain'i icin ayri DataSource/EntityManagerFactory/TransactionManager
 * ucgeni. Detayli aciklama icin bkz. CatalogDataSourceConfig.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
        basePackages = "com.ecommerce.core_service.inventory",
        entityManagerFactoryRef = "inventoryEntityManagerFactory",
        transactionManagerRef = "inventoryTransactionManager"
)
public class InventoryDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.inventory")
    public DataSourceProperties inventoryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.inventory.hikari")
    public DataSource inventoryDataSource(
            @Qualifier("inventoryDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean inventoryEntityManagerFactory(
            @Qualifier("inventoryDataSource") DataSource dataSource,
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
                .packages("com.ecommerce.core_service.inventory")
                .persistenceUnit("inventory")
                .build();
    }

    @Bean
    public PlatformTransactionManager inventoryTransactionManager(
            @Qualifier("inventoryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

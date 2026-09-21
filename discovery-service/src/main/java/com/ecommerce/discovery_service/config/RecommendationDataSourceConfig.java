package com.ecommerce.discovery_service.config;

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
 * recommendation domain'i icin ayri DataSource/EntityManagerFactory/TransactionManager
 * ucgeni. Detayli aciklama icin bkz. core-service/CatalogDataSourceConfig.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
        basePackages = "com.ecommerce.discovery_service.recommendation",
        entityManagerFactoryRef = "recommendationEntityManagerFactory",
        transactionManagerRef = "recommendationTransactionManager"
)
public class RecommendationDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.recommendation")
    public DataSourceProperties recommendationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.recommendation.hikari")
    public DataSource recommendationDataSource(
            @Qualifier("recommendationDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean recommendationEntityManagerFactory(
            @Qualifier("recommendationDataSource") DataSource dataSource,
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
                .packages("com.ecommerce.discovery_service.recommendation")
                .persistenceUnit("recommendation")
                .build();
    }

    @Bean
    public PlatformTransactionManager recommendationTransactionManager(
            @Qualifier("recommendationEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

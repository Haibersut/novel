package com.wtl.novel.Config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(name = "database.mode", havingValue = "dual", matchIfMissing = false)
@EnableJpaRepositories(
        basePackages = "com.wtl.novel.scalingUp.repository",
        entityManagerFactoryRef = "secondaryEntityManagerFactory",
        transactionManagerRef = "secondaryTransactionManager")
public class SecondaryJpaConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(secondaryDataSource())
                .packages("com.wtl.novel.scalingUp.entity")
                .persistenceUnit("secondaryPU")
                .properties(jpaProperties())
                .build();
    }

    @Bean
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    private Map<String, Object> jpaProperties() {
        Map<String, Object> p = new HashMap<>();

        p.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        p.put("hibernate.hbm2ddl.auto", "none");
        p.put("hibernate.show_sql", "false");
        
        // 生产环境配置
        p.put("hibernate.jdbc.batch_size", "20");
        p.put("hibernate.order_inserts", "true");
        p.put("hibernate.order_updates", "true");
        p.put("hibernate.jdbc.batch_versioned_data", "true");
        
        // 连接池和性能配置
        p.put("hibernate.connection.provider_disables_autocommit", "true");
        p.put("hibernate.query.fail_on_pagination_over_collection_fetch", "true");
        p.put("hibernate.query.in_clause_parameter_padding", "true");
        
        return p;
    }
}
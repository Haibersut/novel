package com.wtl.novel.Config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "database.mode", havingValue = "single", matchIfMissing = true)
@EnableJpaRepositories(
        basePackages = {"com.wtl.novel.scalingUp.repository"},
        entityManagerFactoryRef = "scalingUpEntityManagerFactory",
        transactionManagerRef = "scalingUpTransactionManager")
public class SingleDatabaseJpaConfig {
    
    @Bean
    public LocalContainerEntityManagerFactoryBean scalingUpEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource primaryDataSource) {
        return builder
                .dataSource(primaryDataSource)
                .packages("com.wtl.novel.scalingUp.entity")
                .persistenceUnit("scalingUpPU")
                .build();
    }

    @Bean
    public PlatformTransactionManager scalingUpTransactionManager(
            @Qualifier("scalingUpEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
}

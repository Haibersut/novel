package com.wtl.novel.Config;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
@EnableJpaRepositories(
        basePackages = "com.wtl.novel.repository",   // 主库 Repository 所在包
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager")
public class PrimaryJpaConfig {

    @Primary  // 必须有一个主数据源
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(primaryDataSource())
                .packages("com.wtl.novel.entity")      // 主库实体所在包
                .persistenceUnit("primaryPU")
                .properties(jpaProperties())
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    private Map<String, Object> jpaProperties() {
        Map<String, Object> p = new HashMap<>();
        // 使用 MariaDB 专用方言以获得更好的性能和兼容性
        p.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        p.put("hibernate.hbm2ddl.auto", "none");
        p.put("hibernate.show_sql", "false");
        
        // 生产环境推荐配置
        p.put("hibernate.jdbc.batch_size", "20");  // 批量操作优化
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
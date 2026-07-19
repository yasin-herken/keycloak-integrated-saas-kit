package com.archcore.core.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
@ConditionalOnBooleanProperty(name = "spring.liquibase.enabled", matchIfMissing = true)
public class LiquibaseConfiguration {

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}

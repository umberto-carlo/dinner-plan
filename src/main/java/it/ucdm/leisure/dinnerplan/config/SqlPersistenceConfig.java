package it.ucdm.leisure.dinnerplan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Profile("sql")
@EnableJpaRepositories(basePackages = "it.ucdm.leisure.dinnerplan.persistence.sql")
@EnableTransactionManagement
public class SqlPersistenceConfig {
}

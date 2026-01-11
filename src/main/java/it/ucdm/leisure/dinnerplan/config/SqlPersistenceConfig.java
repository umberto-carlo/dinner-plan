package it.ucdm.leisure.dinnerplan.config;

import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
// import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
// import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
// import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;

@Configuration
// @Profile("sql")
// @EnableJpaRepositories(basePackages =
// "it.ucdm.leisure.dinnerplan.persistence.sql")
// @EnableTransactionManagement
// @Import(...)
public class SqlPersistenceConfig {
}

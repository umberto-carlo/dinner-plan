package it.ucdm.leisure.dinnerplan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@Profile("mongo")
@EnableMongoRepositories(basePackages = "it.ucdm.leisure.dinnerplan.persistence.mongo")
@EnableMongoAuditing
public class MongoPersistenceConfig {
}

package edu.dlut.software.cage.deeprouterec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "edu.dlut.software.cage.deeprouterec.repository")
public class MongoTemplateConfig {

    @Bean
    public MongoClientFactoryBean mongo() {
        return new MongoClientFactoryBean();
    }
}

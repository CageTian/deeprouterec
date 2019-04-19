package edu.dlut.software.cage.deeprouterec.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "station")
@Data
public class StationConfig {
    private List<String> cities;
}

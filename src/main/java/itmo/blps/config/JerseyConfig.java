package itmo.blps.config;

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfig {

    @Bean
    public CamundaJerseyResourceConfig createRestConfig() {
        CamundaJerseyResourceConfig config = new CamundaJerseyResourceConfig();
        config.property(ServerProperties.RESOURCE_VALIDATION_DISABLE, true);
        return config;
    }
}

package itmo.blps.config;

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a custom CamundaJerseyResourceConfig bean with resource model
 * validation disabled. This replaces the default auto-configured bean
 * (via @ConditionalOnMissingBean) and prevents Jersey's
 * ModelValidationException caused by Camunda 7.21's history REST API
 * having conflicting sub-resource paths.
 */
@Configuration
public class JerseyConfig {

    @Bean
    public CamundaJerseyResourceConfig createRestConfig() {
        CamundaJerseyResourceConfig config = new CamundaJerseyResourceConfig();
        config.property(ServerProperties.RESOURCE_VALIDATION_DISABLE, true);
        return config;
    }
}

package itmo.blps.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfigPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ResourceConfig) {
            ResourceConfig config = (ResourceConfig) bean;
            config.property("jersey.config.server.resource.validation.disable", true);
        }
        return bean;
    }
}

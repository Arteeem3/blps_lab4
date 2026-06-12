package itmo.blps.config;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ListingScheduler {

    private final RuntimeService runtimeService;

    public ListingScheduler(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void triggerExpiryCheck() {
        runtimeService.startProcessInstanceByKey(
                "listing-expiry-check",
                "scheduled-expiry-" + System.currentTimeMillis(),
                Collections.emptyMap()
        );
    }
}

package itmo.blps.service.delegate;

import itmo.blps.entity.Listing;
import itmo.blps.entity.NotificationType;
import itmo.blps.entity.RelatedEntityType;
import itmo.blps.service.ListingService;
import itmo.blps.service.NotificationService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component("listingExpiryCheckDelegate")
public class ListingExpiryCheckDelegate implements JavaDelegate {

    private final ListingService listingService;
    private final NotificationService notificationService;

    public ListingExpiryCheckDelegate(ListingService listingService, NotificationService notificationService) {
        this.listingService = listingService;
        this.notificationService = notificationService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        List<Listing> expired = listingService.findActiveWithExpiresAtBefore(Instant.now());
        int notified = 0;
        for (Listing listing : expired) {
            notificationService.create(
                    listing.getSeller(),
                    NotificationType.ARCHIVATION_SOON,
                    "Срок размещения истекает",
                    "Срок размещения объявления '" + listing.getTitle() + "' истёк. Подтвердите актуальность.",
                    RelatedEntityType.LISTING,
                    listing.getId()
            );
            notified++;
        }
        execution.setVariable("processedCount", expired.size());
        execution.setVariable("notifiedCount", notified);
    }
}

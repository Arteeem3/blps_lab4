package itmo.blps.service.delegate;

import itmo.blps.entity.Listing;
import itmo.blps.entity.NotificationType;
import itmo.blps.entity.RelatedEntityType;
import itmo.blps.repository.ListingRepository;
import itmo.blps.service.NotificationService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("listingArchivationSoonDelegate")
public class ListingArchivationSoonDelegate implements JavaDelegate {

    private final ListingRepository listingRepository;
    private final NotificationService notificationService;

    public ListingArchivationSoonDelegate(ListingRepository listingRepository, NotificationService notificationService) {
        this.listingRepository = listingRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Object idObj = execution.getVariable("listingId");
        Long listingId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
        Listing l = listingRepository.findById(listingId).orElse(null);
        if (l != null) {
            notificationService.create(
                    l.getSeller(),
                    NotificationType.ARCHIVATION_SOON,
                    "Срок размещения истекает",
                    "Срок размещения объявления «" + l.getTitle() + "» истёк. Подтвердите актуальность или объявление будет архивировано.",
                    RelatedEntityType.LISTING,
                    l.getId()
            );
        }
    }
}

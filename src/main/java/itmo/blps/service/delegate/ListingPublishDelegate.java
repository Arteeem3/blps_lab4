package itmo.blps.service.delegate;

import itmo.blps.entity.Listing;
import itmo.blps.service.ListingService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("listingPublishDelegate")
public class ListingPublishDelegate implements JavaDelegate {

    private final ListingService listingService;

    public ListingPublishDelegate(ListingService listingService) {
        this.listingService = listingService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            Object idObj = execution.getVariable("listingId");
            Long listingId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());

            Boolean forceReject = (Boolean) execution.getVariable("forceReject");
            if (forceReject == null) forceReject = false;

            Listing listing = listingService.publish(listingId, forceReject);
            execution.setVariable("resultMessage", "Объявление #" + listingId + " опубликовано, статус: " + listing.getStatus());
        } catch (Exception e) {
            execution.setVariable("resultMessage", "Ошибка: " + e.getMessage());
            throw new BpmnError("PUBLISH_ERROR", e.getMessage());
        }
    }
}

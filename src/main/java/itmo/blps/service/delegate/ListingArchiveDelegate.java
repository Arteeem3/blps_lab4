package itmo.blps.service.delegate;

import itmo.blps.entity.Listing;
import itmo.blps.entity.ListingStatus;
import itmo.blps.repository.ListingRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("listingArchiveDelegate")
public class ListingArchiveDelegate implements JavaDelegate {

    private final ListingRepository listingRepository;

    public ListingArchiveDelegate(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Object idObj = execution.getVariable("listingId");
        Long listingId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
        Listing l = listingRepository.findById(listingId).orElse(null);
        if (l != null) {
            l.setStatus(ListingStatus.ARCHIVED);
            listingRepository.save(l);
        }
    }
}

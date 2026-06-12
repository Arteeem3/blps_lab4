package itmo.blps.service.delegate;

import itmo.blps.entity.Listing;
import itmo.blps.entity.ListingStatus;
import itmo.blps.repository.ListingRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("inquiryFetchListingsDelegate")
public class InquiryFetchListingsDelegate implements JavaDelegate {

    private final ListingRepository listingRepository;

    public InquiryFetchListingsDelegate(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String starterId = (String) execution.getVariable("starter");
        Long buyerId = Long.valueOf(starterId);

        List<Listing> activeListings = listingRepository.findAllByStatus(ListingStatus.ACTIVE);
        
        // Filter out own listings
        List<Map<String, String>> options = activeListings.stream()
                .filter(l -> !l.getSeller().getId().equals(buyerId))
                .map(l -> Map.of(
                        "value", String.valueOf(l.getId()),
                        "label", l.getTitle() + " (" + l.getPrice() + " руб.)"
                ))
                .collect(Collectors.toList());

        String json = Spin.JSON(options).toString();
        
        // availableListings variable will be used in Camunda Forms
        execution.setVariable("availableListings", json);
    }
}

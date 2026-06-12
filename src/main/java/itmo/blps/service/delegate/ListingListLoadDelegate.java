package itmo.blps.service.delegate;

import itmo.blps.entity.Listing;
import itmo.blps.repository.ListingRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("listingListLoadDelegate")
public class ListingListLoadDelegate implements JavaDelegate {

    private final ListingRepository listingRepository;

    public ListingListLoadDelegate(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        List<Listing> listings = listingRepository.findAll();
        StringBuilder sb = new StringBuilder();
        for (Listing l : listings) {
            sb.append("ID: ").append(l.getId())
              .append(" | ").append(l.getTitle())
              .append(" | Цена: ").append(l.getPrice())
              .append(" | Статус: ").append(l.getStatus())
              .append("\n");
        }
        if (sb.isEmpty()) {
            sb.append("Объявлений пока нет.");
        }
        execution.setVariable("listingsText", sb.toString());
    }
}

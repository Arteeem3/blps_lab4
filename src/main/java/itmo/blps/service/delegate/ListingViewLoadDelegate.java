package itmo.blps.service.delegate;

import itmo.blps.entity.Listing;
import itmo.blps.repository.ListingRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("listingViewLoadDelegate")
public class ListingViewLoadDelegate implements JavaDelegate {

    private final ListingRepository listingRepository;

    public ListingViewLoadDelegate(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Object lIdObj = execution.getVariable("listingId");
        if (lIdObj == null) {
            execution.setVariable("listingDetails", "ID не указан");
            return;
        }
        Long listingId = Long.valueOf(lIdObj.toString());
        Listing l = listingRepository.findById(listingId).orElse(null);
        
        if (l == null) {
            execution.setVariable("listingDetails", "Объявление не найдено.");
        } else {
            String details = "Заголовок: " + l.getTitle() + "\n" +
                             "Описание: " + l.getDescription() + "\n" +
                             "Цена: " + l.getPrice() + "\n" +
                             "Комнат: " + l.getRooms() + "\n" +
                             "Статус: " + l.getStatus();
            execution.setVariable("listingDetails", details);
        }
    }
}

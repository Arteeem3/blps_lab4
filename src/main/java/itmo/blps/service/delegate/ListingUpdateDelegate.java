package itmo.blps.service.delegate;

import itmo.blps.dto.ListingCreateRequest;
import itmo.blps.entity.Listing;
import itmo.blps.entity.User;
import itmo.blps.repository.ListingRepository;
import itmo.blps.service.ListingService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("listingUpdateDelegate")
public class ListingUpdateDelegate implements JavaDelegate {

    private final ListingService listingService;
    private final ListingRepository listingRepository;

    public ListingUpdateDelegate(ListingService listingService, ListingRepository listingRepository) {
        this.listingService = listingService;
        this.listingRepository = listingRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long listingId = Long.valueOf(execution.getVariable("listingId").toString());
        Listing listing = listingRepository.findById(listingId).orElseThrow();

        ListingCreateRequest req = new ListingCreateRequest();
        req.setTitle((String) execution.getVariable("title"));
        req.setDescription((String) execution.getVariable("description"));
        req.setAddress((String) execution.getVariable("address"));
        req.setRegion((String) execution.getVariable("region"));
        
        Object priceObj = execution.getVariable("price");
        if (priceObj != null) req.setPrice(new BigDecimal(priceObj.toString()));

        Object areaObj = execution.getVariable("areaSqm");
        if (areaObj != null) req.setAreaSqm(new BigDecimal(areaObj.toString()));

        Object roomsObj = execution.getVariable("rooms");
        if (roomsObj != null) req.setRooms(Integer.valueOf(roomsObj.toString()));

        String starterId = (String) execution.getVariable("starter");
        User seller = listing.getSeller();
        if (!seller.getId().toString().equals(starterId)) {
            throw new RuntimeException("Not owner");
        }

        listingService.update(listingId, seller, req);
        execution.setVariable("updateResult", "Объявление успешно обновлено.");
    }
}

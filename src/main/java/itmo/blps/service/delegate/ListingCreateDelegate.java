package itmo.blps.service.delegate;

import itmo.blps.dto.ListingCreateRequest;
import itmo.blps.entity.Listing;
import itmo.blps.entity.User;
import itmo.blps.repository.UserRepository;
import itmo.blps.service.ListingService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("listingCreateDelegate")
public class ListingCreateDelegate implements JavaDelegate {

    private final ListingService listingService;
    private final UserRepository userRepository;

    public ListingCreateDelegate(ListingService listingService, UserRepository userRepository) {
        this.listingService = listingService;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String starterId = (String) execution.getVariable("starter");
        User seller = userRepository.findById(Long.valueOf(starterId))
                .orElseThrow(() -> new RuntimeException("User not found: " + starterId));

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

        Listing listing = listingService.create(seller, req);
        execution.setVariable("listingId", listing.getId());
    }
}

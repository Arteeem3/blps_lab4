package itmo.blps.service.delegate;

import itmo.blps.entity.User;
import itmo.blps.repository.UserRepository;
import itmo.blps.service.ListingService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("listingDeleteDelegate")
public class ListingDeleteDelegate implements JavaDelegate {

    private final ListingService listingService;
    private final UserRepository userRepository;

    public ListingDeleteDelegate(ListingService listingService, UserRepository userRepository) {
        this.listingService = listingService;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long listingId = Long.valueOf(execution.getVariable("listingId").toString());
        String starterId = (String) execution.getVariable("starter");
        User seller = userRepository.findById(Long.valueOf(starterId)).orElseThrow();
        
        listingService.close(listingId, seller);
        execution.setVariable("deleteResult", "Объявление успешно закрыто.");
    }
}

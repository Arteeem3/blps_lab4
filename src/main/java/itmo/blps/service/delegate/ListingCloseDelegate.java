package itmo.blps.service.delegate;

import itmo.blps.entity.User;
import itmo.blps.repository.UserRepository;
import itmo.blps.service.ListingService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("listingCloseDelegate")
public class ListingCloseDelegate implements JavaDelegate {

    private final ListingService listingService;
    private final UserRepository userRepository;

    public ListingCloseDelegate(ListingService listingService, UserRepository userRepository) {
        this.listingService = listingService;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            Object idObj = execution.getVariable("listingId");
            Long listingId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
            String starterId = (String) execution.getVariable("starter");
            User seller = userRepository.findById(Long.valueOf(starterId)).orElseThrow();
            listingService.close(listingId, seller);
            execution.setVariable("resultMessage", "Объявление #" + listingId + " закрыто");
        } catch (Exception e) {
            execution.setVariable("resultMessage", "Ошибка: " + e.getMessage());
            throw new BpmnError("CLOSE_ERROR", e.getMessage());
        }
    }
}

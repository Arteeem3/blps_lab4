package itmo.blps.service.delegate;

import itmo.blps.entity.Inquiry;
import itmo.blps.entity.User;
import itmo.blps.repository.UserRepository;
import itmo.blps.service.InquiryService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("inquiryCreateDelegate")
public class InquiryCreateDelegate implements JavaDelegate {

    private final InquiryService inquiryService;
    private final UserRepository userRepository;

    public InquiryCreateDelegate(InquiryService inquiryService, UserRepository userRepository) {
        this.inquiryService = inquiryService;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            String starterId = (String) execution.getVariable("starter");
            User buyer = userRepository.findById(Long.valueOf(starterId)).orElseThrow();

            Object lIdObj = execution.getVariable("listingId");
            Long listingId = null;
            if (lIdObj != null) {
                listingId = Long.valueOf(lIdObj.toString());
            }

            String message = (String) execution.getVariable("message");

            Inquiry inquiry = inquiryService.create(buyer, listingId, message);

            execution.setVariable("inquiryId", inquiry.getId());
            execution.setVariable("sellerId", inquiry.getListing().getSeller().getId().toString());
            execution.setVariable("resultMessage", "Заявка #" + inquiry.getId() + " создана");
        } catch (Exception e) {
            execution.setVariable("resultMessage", "Ошибка: " + e.getMessage());
            throw new BpmnError("INQUIRY_CREATE_ERROR", e.getMessage());
        }
    }
}


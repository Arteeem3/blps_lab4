package itmo.blps.service.delegate;

import itmo.blps.entity.User;
import itmo.blps.repository.UserRepository;
import itmo.blps.service.InquiryService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("inquiryVisitResultDelegate")
public class InquiryVisitResultDelegate implements JavaDelegate {

    private final InquiryService inquiryService;
    private final UserRepository userRepository;

    public InquiryVisitResultDelegate(InquiryService inquiryService, UserRepository userRepository) {
        this.inquiryService = inquiryService;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            Object idObj = execution.getVariable("inquiryId");
            Long inquiryId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
            String starterId = (String) execution.getVariable("starter");
            User buyer = userRepository.findById(Long.valueOf(starterId)).orElseThrow();

            Boolean willBuy = (Boolean) execution.getVariable("willBuy");
            if (willBuy == null) willBuy = false;

            inquiryService.visitResult(inquiryId, buyer, willBuy);
            execution.setVariable("resultMessage", willBuy ? "Решение: покупаю" : "Решение: не покупаю");
        } catch (Exception e) {
            execution.setVariable("resultMessage", "Ошибка: " + e.getMessage());
            throw new BpmnError("VISIT_ERROR", e.getMessage());
        }
    }
}


package itmo.blps.service.delegate;

import itmo.blps.dto.ShowingDecisionRequest;
import itmo.blps.entity.User;
import itmo.blps.repository.UserRepository;
import itmo.blps.service.InquiryService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component("inquiryResolveDelegate")
public class InquiryResolveDelegate implements JavaDelegate {

    private final InquiryService inquiryService;
    private final UserRepository userRepository;

    public InquiryResolveDelegate(InquiryService inquiryService, UserRepository userRepository) {
        this.inquiryService = inquiryService;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            Long inquiryId = Long.valueOf(execution.getVariable("inquiryId").toString());
            String starterId = (String) execution.getVariable("starter");
            User seller = userRepository.findById(Long.valueOf(starterId)).orElseThrow();
            String decisionStr = (String) execution.getVariable("decision");
            ShowingDecisionRequest.Decision decision = ShowingDecisionRequest.Decision.valueOf(decisionStr);

            Instant scheduledAt = null;
            Object schedObj = execution.getVariable("scheduledAt");
            if (schedObj != null && !schedObj.toString().isBlank()) {
                scheduledAt = Instant.parse(schedObj.toString());
            }

            String contactInfo = (String) execution.getVariable("contactInfo");
            String reason = (String) execution.getVariable("reason");

            inquiryService.resolveShowing(inquiryId, seller, decision, scheduledAt, contactInfo, reason);
            execution.setVariable("resultMessage", "Заявка #" + inquiryId + ": " + decision);
        } catch (Exception e) {
            execution.setVariable("resultMessage", "Ошибка: " + e.getMessage());
            throw new BpmnError("RESOLVE_ERROR", e.getMessage());
        }
    }
}


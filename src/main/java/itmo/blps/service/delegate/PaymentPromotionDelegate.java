package itmo.blps.service.delegate;

import itmo.blps.entity.PromotionType;
import itmo.blps.entity.User;
import itmo.blps.repository.UserRepository;
import itmo.blps.service.PaymentService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("paymentPromotionDelegate")
public class PaymentPromotionDelegate implements JavaDelegate {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentPromotionDelegate(PaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            Object idObj = execution.getVariable("listingId");
            Long listingId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
            String starterId = (String) execution.getVariable("starter");
            User user = userRepository.findById(Long.valueOf(starterId)).orElseThrow();

            String promTypeStr = (String) execution.getVariable("promotionType");
            PromotionType promotionType = PromotionType.NONE;
            if (promTypeStr != null && !promTypeStr.isBlank()) {
                promotionType = PromotionType.valueOf(promTypeStr);
            }

            if (promotionType != PromotionType.NONE) {
                PaymentService.PaymentResult result = paymentService.pay(listingId, user, promotionType);
                execution.setVariable("paymentStatus", result.status().name());
                execution.setVariable("paymentMessage", result.message());
                execution.setVariable("resultMessage", "Оплата: " + result.status() + " — " + result.message());
            } else {
                execution.setVariable("paymentStatus", "SKIPPED");
                execution.setVariable("resultMessage", "Продвижение не выбрано");
            }
        } catch (Exception e) {
            execution.setVariable("resultMessage", "Ошибка оплаты: " + e.getMessage());
            throw new BpmnError("PAYMENT_ERROR", e.getMessage());
        }
    }
}


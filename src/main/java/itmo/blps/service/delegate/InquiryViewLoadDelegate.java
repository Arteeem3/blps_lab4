package itmo.blps.service.delegate;

import itmo.blps.entity.Inquiry;
import itmo.blps.repository.InquiryRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("inquiryViewLoadDelegate")
public class InquiryViewLoadDelegate implements JavaDelegate {

    private final InquiryRepository inquiryRepository;

    public InquiryViewLoadDelegate(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Object inqIdObj = execution.getVariable("inquiryId");
        if (inqIdObj == null) {
            execution.setVariable("inquiryDetails", "ID не указан");
            return;
        }
        Long inquiryId = Long.valueOf(inqIdObj.toString());
        Inquiry inq = inquiryRepository.findById(inquiryId).orElse(null);
        
        if (inq == null) {
            execution.setVariable("inquiryDetails", "Заявка не найдена.");
        } else {
            String details = "Объявление: " + inq.getListing().getTitle() + "\n" +
                             "Покупатель: " + inq.getBuyer().getEmail() + "\n" +
                             "Продавец: " + inq.getListing().getSeller().getEmail() + "\n" +
                             "Сообщение: " + inq.getMessage() + "\n" +
                             "Согласованное время: " + inq.getScheduledAt() + "\n" +
                             "Статус: " + inq.getStatus();
            execution.setVariable("inquiryDetails", details);
        }
    }
}

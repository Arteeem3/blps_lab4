package itmo.blps.service.delegate;

import itmo.blps.entity.Inquiry;
import itmo.blps.repository.InquiryRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("inquiryListLoadDelegate")
public class InquiryListLoadDelegate implements JavaDelegate {

    private final InquiryRepository inquiryRepository;

    public InquiryListLoadDelegate(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String starterId = (String) execution.getVariable("starter");
        Long userId = Long.valueOf(starterId);

        List<Inquiry> inquiries = inquiryRepository.findAll();
        StringBuilder sb = new StringBuilder();
        for (Inquiry inq : inquiries) {
            if (inq.getBuyer().getId().equals(userId) || inq.getListing().getSeller().getId().equals(userId)) {
                sb.append("ID: ").append(inq.getId())
                  .append(" | Объявление: ").append(inq.getListing().getTitle())
                  .append(" | Статус: ").append(inq.getStatus())
                  .append(" | Роль: ").append(inq.getBuyer().getId().equals(userId) ? "Покупатель" : "Продавец")
                  .append("\n");
            }
        }
        if (sb.isEmpty()) {
            sb.append("Заявок нет.");
        }
        execution.setVariable("inquiriesText", sb.toString());
    }
}

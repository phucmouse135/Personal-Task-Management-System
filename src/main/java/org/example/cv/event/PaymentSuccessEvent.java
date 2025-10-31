package org.example.cv.event;

import lombok.Getter;
import org.example.cv.models.entities.PaymentEntity;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentSuccessEvent extends ApplicationEvent {
    private final PaymentEntity payment;

    public PaymentSuccessEvent(Object source, PaymentEntity payment) {
        super(source);
        this.payment = payment;
    }
}

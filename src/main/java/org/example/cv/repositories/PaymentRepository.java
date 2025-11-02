package org.example.cv.repositories;

import org.example.cv.constants.PaymentStatus;
import org.example.cv.models.entities.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepository extends BaseRepository<PaymentEntity, Long> {
    Page<PaymentEntity> findByStatus(PaymentStatus status, Pageable pageable);
}

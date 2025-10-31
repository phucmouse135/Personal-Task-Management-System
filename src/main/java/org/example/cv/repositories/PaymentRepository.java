package org.example.cv.repositories;

import org.example.cv.models.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends BaseRepository<PaymentEntity, Long> {
}

package org.example.cv.models.entities;

import jakarta.persistence.*;

import org.example.cv.constants.PaymentStatus;
import org.example.cv.models.entities.base.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NamedEntityGraph(
        name = "Payment.withUserAndProject",
        attributeNodes = {@NamedAttributeNode("user"), @NamedAttributeNode("project")})
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id") // Có thể thanh toán cho 1 project
    ProjectEntity project;

    @Column(nullable = false)
    Long amount; // Số tiền (VND)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    PaymentStatus status = PaymentStatus.PENDING;

    // ID giao dịch của VNPAY (vnp_TransactionNo)
    @Column(name = "vnp_transaction_no")
    String vnpTransactionNo;

    // Mã ngân hàng (vnp_BankCode)
    @Column(name = "vnp_bank_code")
    String vnpBankCode;

    // Thời gian thanh toán (vnp_PayDate)
    @Column(name = "vnp_pay_date")
    String vnpPayDate;
}

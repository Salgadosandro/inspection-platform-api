package com.vectorlabs.payments.model;

import com.vectorlabs.model.enuns.PaymentProvider;
import com.vectorlabs.model.enuns.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment_intent")
public class PaymentIntent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "inspection_id", nullable = false)
    private UUID inspectionId;

    @Column(name = "machine_count_snapshot", nullable = false)
    private int machineCountSnapshot;

    @Column(name = "report_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal reportFee;

    @Column(name = "price_per_machine", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerMachine;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "provider_payment_id", length = 120)
    private String providerPaymentId;

    @Column(name = "provider_checkout_url", length = 500)
    private String providerCheckoutUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) this.status = PaymentStatus.PENDING;
        if (this.provider == null) this.provider = PaymentProvider.MERCADO_PAGO;
    }
}

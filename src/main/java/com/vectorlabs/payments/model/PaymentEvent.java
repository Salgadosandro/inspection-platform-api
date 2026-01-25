package com.vectorlabs.payments.model;

import com.vectorlabs.model.enuns.PaymentProvider;
import com.vectorlabs.model.enuns.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "payment_event",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_event_provider_event",
                        columnNames = {"provider", "provider_event_id"}
                )
        }
)
public class PaymentEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private PaymentProvider provider;

    @Column(name = "provider_event_id", nullable = false, length = 120)
    private String providerEventId;

    @Column(name = "provider_payment_id", length = 120)
    private String providerPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Lob
    @Column(name = "raw_payload", nullable = false)
    private String rawPayload;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    private void onCreate() {
        this.receivedAt = Instant.now();
    }
}

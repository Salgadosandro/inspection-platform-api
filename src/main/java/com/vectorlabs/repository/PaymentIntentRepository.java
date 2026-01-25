package com.vectorlabs.repository;

import com.vectorlabs.model.enuns.PaymentProvider;
import com.vectorlabs.model.enuns.PaymentStatus;
import com.vectorlabs.payments.model.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID>, JpaSpecificationExecutor<PaymentIntent> {

    Optional<PaymentIntent> findTopByInspectionIdOrderByCreatedAtDesc(UUID inspectionId);

    Optional<PaymentIntent> findByProviderAndProviderPaymentId(PaymentProvider provider, String providerPaymentId);

    boolean existsByProviderAndProviderPaymentId(PaymentProvider provider, String providerPaymentId);

    long countByInspectionIdAndStatus(UUID inspectionId, PaymentStatus status);
}

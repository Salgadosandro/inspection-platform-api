package com.vectorlabs.repository;

import com.vectorlabs.model.enuns.PaymentProvider;
import com.vectorlabs.payments.model.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID>, JpaSpecificationExecutor<PaymentEvent> {

    boolean existsByProviderAndProviderEventId(PaymentProvider provider, String providerEventId);

    Optional<PaymentEvent> findByProviderAndProviderEventId(PaymentProvider provider, String providerEventId);

    long countByProviderAndProviderPaymentId(PaymentProvider provider, String providerPaymentId);
}

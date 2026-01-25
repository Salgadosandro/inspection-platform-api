package com.vectorlabs.payments.service;

import com.vectorlabs.payments.dto.mercadopago.PaymentAnswerDTO;
import com.vectorlabs.model.enuns.PaymentStatus;
import com.vectorlabs.payments.model.PaymentIntent;
import com.vectorlabs.payments.gateway.mercadopago.PaymentGatewayClient;
import com.vectorlabs.repository.PaymentIntentRepository;
import com.vectorlabs.service.InspectionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentOrchestratorService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final PricingService pricingService;
    private final PaymentGatewayClient paymentGatewayClient;
    private final InspectionService inspectionService;

    @Transactional
    public PaymentAnswerDTO createPaymentForInspection(UUID inspectionId, UUID requesterUserId) {

        // 1) valida que a inspeção existe e pertence ao usuário (ou que ele tem permissão)
        inspectionService.assertUserCanPay(inspectionId, requesterUserId);

        // 2) pega a quantidade atual de máquinas
        int machineCount = inspectionService.countMachines(inspectionId);
        if (machineCount <= 0) {
            throw new IllegalStateException("Inspection must have at least 1 machine to be charged");
        }

        // 3) evita criar várias cobranças iguais: se já existe uma PENDING recente, reaproveita
        paymentIntentRepository.findTopByInspectionIdOrderByCreatedAtDesc(inspectionId)
                .filter(pi -> pi.getStatus() == PaymentStatus.PENDING)
                .ifPresent(existing -> {
                    throw new IllegalStateException("There is already a pending payment for this inspection");
                });

        // 4) calcula o valor (taxa fixa + preço por máquina)
        BigDecimal reportFee = pricingService.reportFee();
        BigDecimal pricePerMachine = pricingService.pricePerMachine();
        BigDecimal totalAmount = pricingService.calculateTotal(machineCount);

        // 5) cria uma cobrança no provedor (Mercado Pago) e recebe dados (id e link)
        PaymentGatewayClient.CreateChargeResult charge = paymentGatewayClient.createCharge(
                inspectionId,
                machineCount,
                totalAmount
        );

        // 6) cria e salva o PaymentIntent (o “papel do pedido”)
        PaymentIntent intent = new PaymentIntent();
        intent.setInspectionId(inspectionId);
        intent.setMachineCountSnapshot(machineCount);
        intent.setReportFee(reportFee);
        intent.setPricePerMachine(pricePerMachine);
        intent.setTotalAmount(totalAmount);
        intent.setProvider(charge.provider());
        intent.setStatus(PaymentStatus.PENDING);
        intent.setProviderPaymentId(charge.providerPaymentId());
        intent.setProviderCheckoutUrl(charge.checkoutUrl());

        paymentIntentRepository.save(intent);

        // 7) devolve para o frontend (pra abrir checkout / mostrar QR)
        return new PaymentAnswerDTO(
                intent.getId(),
                inspectionId,
                machineCount,
                totalAmount,
                intent.getStatus(),
                intent.getProvider(),
                intent.getProviderCheckoutUrl()
        );
    }
}

package com.vectorlabs.payments.controller;

import com.vectorlabs.payments.model.PaymentIntent;
import com.vectorlabs.payments.dto.mercadopago.PaymentAnswerDTO;
import com.vectorlabs.payments.dto.mercadopago.PricingPreviewDTO;
import com.vectorlabs.payments.service.PaymentOrchestratorService;
import com.vectorlabs.payments.service.PaymentReconciliationService;
import com.vectorlabs.payments.service.PricingService;
import com.vectorlabs.repository.PaymentIntentRepository;
import com.vectorlabs.service.InspectionService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private final InspectionService inspectionService;
    private final PricingService pricingService;
    private final PaymentOrchestratorService paymentOrchestratorService;
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentReconciliationService paymentReconciliationService;

    /**
     * Preview do preço baseado na quantidade atual de máquinas da inspeção.
     * Bom pra mostrar na UI antes do usuário clicar em "Pagar".
     */
    @GetMapping("/inspections/{inspectionId}/pricing")
    public ResponseEntity<PricingPreviewDTO> pricingPreview(@PathVariable UUID inspectionId) {

        int machineCount = inspectionService.countMachines(inspectionId);
        if (machineCount <= 0) {
            // você pode preferir retornar 200 com total=0, mas eu acho melhor bloquear
            throw new IllegalStateException("Inspection must have at least 1 machine");
        }

        return ResponseEntity.ok(
                new PricingPreviewDTO(
                        machineCount,
                        pricingService.reportFee(),
                        pricingService.pricePerMachine(),
                        pricingService.calculateTotal(machineCount)
                )
        );
    }

    /**
     * Cria a cobrança (checkout link/QR) para a inspeção.
     * O frontend chama isso quando o usuário decide pagar.
     */
    @PostMapping("/inspections/{inspectionId}/payments")
    public ResponseEntity<PaymentAnswerDTO> createPayment(
            @PathVariable UUID inspectionId,
            @RequestHeader(name = "X-User-Id", required = false) UUID requesterUserId
    ) {
        // no seu projeto real, pegue o usuário do SecurityContext (JWT) e não de header.
        // deixei assim só pra não travar seu copy/paste agora.
        if (requesterUserId == null) {
            throw new IllegalStateException("Missing requester user id");
        }

        PaymentAnswerDTO answer = paymentOrchestratorService.createPaymentForInspection(inspectionId, requesterUserId);
        return ResponseEntity.ok(answer);
    }

    /**
     * Retorna o último PaymentIntent da inspeção (status + checkoutUrl).
     * Útil pra tela "aguardando pagamento".
     */
    @GetMapping("/inspections/{inspectionId}/payments/latest")
    public ResponseEntity<PaymentAnswerDTO> latestPayment(@PathVariable UUID inspectionId) {
        PaymentIntent intent = paymentIntentRepository.findTopByInspectionIdOrderByCreatedAtDesc(inspectionId)
                .orElseThrow(() -> new IllegalStateException("No payment found for this inspection"));

        return ResponseEntity.ok(
                new PaymentAnswerDTO(
                        intent.getId(),
                        intent.getInspectionId(),
                        intent.getMachineCountSnapshot(),
                        intent.getTotalAmount(),
                        intent.getStatus(),
                        intent.getProvider(),
                        intent.getProviderCheckoutUrl()
                )
        );
    }

    /**
     * Opcional: força reconciliação manual (consulta o provedor e atualiza status).
     * Útil em MVP pra quando webhook falhar.
     */
    @PostMapping("/payments/{paymentIntentId}/reconcile")
    public ResponseEntity<PaymentAnswerDTO> reconcile(@PathVariable UUID paymentIntentId) {
        PaymentIntent intent = paymentReconciliationService.reconcileByIntentId(paymentIntentId);

        return ResponseEntity.ok(
                new PaymentAnswerDTO(
                        intent.getId(),
                        intent.getInspectionId(),
                        intent.getMachineCountSnapshot(),
                        intent.getTotalAmount(),
                        intent.getStatus(),
                        intent.getProvider(),
                        intent.getProviderCheckoutUrl()
                )
        );
    }
}

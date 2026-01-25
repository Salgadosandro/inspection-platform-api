package com.vectorlabs.payments.service;

import com.vectorlabs.exception.PaymentRequiredException;
import com.vectorlabs.model.enuns.PaymentStatus;
import com.vectorlabs.payments.model.PaymentIntent;
import com.vectorlabs.repository.PaymentIntentRepository;
import com.vectorlabs.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InspectionBillingService {

    private final InspectionService inspectionService; // seu service atual da inspeção
    private final PaymentGuardService paymentGuardService;
    private final PaymentIntentRepository paymentIntentRepository;

    /**
     * Decide se a inspeção deve ficar “bloqueada” para ações finais (ex: gerar PDF final).
     */
    public boolean isFinalOutputLocked(UUID inspectionId) {
        // se não estiver pago, está bloqueado
        return !paymentGuardService.isInspectionPaid(inspectionId);
    }

    /**
     * Regra principal: só pode gerar relatório final se estiver PAGO
     * E se a quantidade de máquinas não mudou depois do pagamento (snapshot).
     */
    public void requireCanGenerateFinalReport(UUID inspectionId) {

        PaymentIntent paidIntent = paymentIntentRepository.findTopByInspectionIdOrderByCreatedAtDesc(inspectionId)
                .orElseThrow(PaymentRequiredException::new);

        if (paidIntent.getStatus() != PaymentStatus.PAID) {
            throw new PaymentRequiredException();
        }

        int currentMachineCount = inspectionService.countMachines(inspectionId);

        // se o usuário adicionou máquinas depois de pagar, bloqueia e pede complemento
        if (currentMachineCount != paidIntent.getMachineCountSnapshot()) {
            throw new IllegalStateException(
                    "Machine count changed after payment. Please create a new payment to cover the difference."
            );
        }
    }

    /**
     * Útil para UI/Frontend: mostra se está tudo ok para emitir o relatório final.
     */
    public BillingSummary getBillingSummary(UUID inspectionId) {

        int currentMachineCount = inspectionService.countMachines(inspectionId);

        var lastIntentOpt = paymentIntentRepository.findTopByInspectionIdOrderByCreatedAtDesc(inspectionId);

        if (lastIntentOpt.isEmpty()) {
            return new BillingSummary(false, false, currentMachineCount, null);
        }

        PaymentIntent last = lastIntentOpt.get();

        boolean paid = last.getStatus() == PaymentStatus.PAID;
        boolean machineCountMatches = currentMachineCount == last.getMachineCountSnapshot();

        return new BillingSummary(paid, machineCountMatches, currentMachineCount, last.getMachineCountSnapshot());
    }

    public record BillingSummary(
            boolean paid,
            boolean machineCountMatchesPaidSnapshot,
            int currentMachineCount,
            Integer paidSnapshotMachineCount
    ) {}
}

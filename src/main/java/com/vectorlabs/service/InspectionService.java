package com.vectorlabs.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InspectionService {
    public int countMachines(UUID inspectionId) {
        return -1;
    }

    public void assertUserCanPay(UUID inspectionId, UUID requesterUserId) {
    }
}

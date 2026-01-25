package com.vectorlabs.payments.dto.payments;

import java.math.BigDecimal;
import java.util.List;

public record MpCreatePreferenceRequestDTO(
        List<Item> items,
        BackUrls back_urls,
        String external_reference,
        boolean auto_return
) {

    public record Item(
            String title,
            int quantity,
            BigDecimal unit_price,
            String currency_id
    ) {}

    public record BackUrls(
            String success,
            String pending,
            String failure
    ) {}
}

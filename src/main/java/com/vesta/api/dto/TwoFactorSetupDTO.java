package com.vesta.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TwoFactorSetupDTO {
    private String secret;
    private String qrCodeUrl;
}

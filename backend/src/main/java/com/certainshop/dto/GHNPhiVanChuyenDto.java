package com.certainshop.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO phí vận chuyển từ GHN
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GHNPhiVanChuyenDto {
    
    private BigDecimal total; // Tổng phí
    private BigDecimal serviceFee; // Phí dịch vụ
    private BigDecimal insuranceFee; // Phí bảo hiểm
    private BigDecimal pickStationFee;
    private BigDecimal couponValue; // Giảm giá
    private BigDecimal r2sFee;

    public BigDecimal getThamGiaGiam() {
        return couponValue != null ? couponValue : BigDecimal.ZERO;
    }
}

package com.certainshop.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO biến thể sản phẩm
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BienTheDto {

    private Long id;

    private Long sanPhamId;

    private Long kichThuocId;

    private Long mauSacId;

    private Long chatLieuId;

    @NotNull(message = "Giá biến thể không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal gia;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer soLuongTon;

    private Boolean macDinh = false;

    private Boolean trangThai = true;
}

package com.certainshop.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tạo/cập nhật sản phẩm
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanPhamDto {

    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String tenSanPham;

    private String moTaChiTiet;

    @NotNull(message = "Giá gốc không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá gốc phải lớn hơn 0")
    private BigDecimal giaGoc;

    @NotNull(message = "Danh mục không được để trống")
    private Long danhMucId;

    private Long thuongHieuId;

    private Long chatLieuId;

    private Boolean trangThai = true;

    // Danh sách biến thể
    private List<BienTheDto> danhSachBienThe;
}

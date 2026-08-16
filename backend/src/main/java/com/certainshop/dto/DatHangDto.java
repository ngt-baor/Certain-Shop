package com.certainshop.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO đặt hàng online
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DatHangDto {

    // ID địa chỉ đã lưu (nếu chọn từ danh sách)
    private Long diaChiId;

    // Hoặc nhập địa chỉ mới
    @NotBlank(message = "Tên người nhận không được để trống")
    private String tenNguoiNhan;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String diaChiCuThe;

    // GHN codes
    private Integer maTinhGHN;
    private Integer maHuyenGHN;
    private String maXaGHN;

    // Tên hiển thị
    private String tenTinh;
    private String tenHuyen;
    private String tenXa;

    private String phuongThucThanhToan; // COD hoặc VNPAY

    private Long khuyenMaiId;

    // Mã voucher (Voucher entity) - frontend gửi lên khi áp dụng voucher
    private String maVoucher;

    private String ghiChu;

    private Boolean luuDiaChi = false;

}

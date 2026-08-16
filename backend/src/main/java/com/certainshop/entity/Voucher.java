package com.certainshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Voucher")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaVoucher", length = 50, unique = true, nullable = false)
    private String maVoucher; // e.g., "SUMMER2026", "NEWYEAR"

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa; // Description

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true; // true = active, false = deleted

    // Validity period
    @Column(name = "NgayBatDau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "NgayKetThuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    // Conditions
    @Column(name = "GiaTriToiThieu", precision = 18, scale = 2)
    private BigDecimal giaTriToiThieu; // Minimum order value to apply voucher

    @Column(name = "GiaTriGiamToiDa", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaTriGiamToiDa; // Max discount amount

    // Discount type and value
    @Column(name = "LoaiGiam", length = 20, nullable = false)
    private String loaiGiam; // "PERCENT" (%) or "FIXED" (đ)

    @Column(name = "GiaTriGiam", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaTriGiam; // Discount value (% for PERCENT, đ for FIXED)

    // Usage tracking
    @Column(name = "SoLuongSuDung")
    private Integer soLuongSuDung = 0; // Number of times used

    @Column(name = "SoLuongToiDa")
    private Integer soLuongToiDa; // Max uses allowed (null = unlimited)

    // Metadata
    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @Column(name = "ThoiGianCapNhat")
    private LocalDateTime thoiGianCapNhat;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        thoiGianTao = now;
        if (thoiGianCapNhat == null) thoiGianCapNhat = now;
        if (trangThai == null) trangThai = true;
    }

    @PreUpdate
    protected void onUpdate() {
        thoiGianCapNhat = LocalDateTime.now();
    }

    /**
     * Kiểm tra voucher có được áp dụng được không (bỏ qua điều kiện giá trị đơn hàng)
     */
    public boolean isValid() {
        if (!trangThai) return false;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(ngayBatDau) || now.isAfter(ngayKetThuc)) return false;
        if (soLuongToiDa != null && soLuongSuDung >= soLuongToiDa) return false;
        return true;
    }

    /**
     * Tính tiền giảm dựa vào giá trị đơn hàng
     */
    public BigDecimal tinhGiaTriGiam(BigDecimal giaTriDonHang) {
        if (!isValid()) return BigDecimal.ZERO;
        if (giaTriToiThieu != null && giaTriDonHang.compareTo(giaTriToiThieu) < 0) {
            return BigDecimal.ZERO; // Không đủ giá trị tối thiểu
        }

        BigDecimal giaTriGiamThuc;
        if ("PERCENT".equals(loaiGiam)) {
            giaTriGiamThuc = giaTriDonHang.multiply(giaTriGiam).divide(new BigDecimal("100"));
        } else {
            giaTriGiamThuc = giaTriGiam;
        }

        // Không vượt quá giá trị giảm tối đa
        if (giaTriGiamThuc.compareTo(giaTriGiamToiDa) > 0) {
            giaTriGiamThuc = giaTriGiamToiDa;
        }

        return giaTriGiamThuc;
    }
}

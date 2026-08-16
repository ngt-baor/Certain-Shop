package com.certainshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "KhuyenMai")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaKhuyenMai", length = 50)
    private String maKhuyenMai;

    @Column(name = "TenKhuyenMai", length = 150)
    private String tenKhuyenMai;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    // PERCENT hoặc FIXED
    @Column(name = "LoaiGiam", length = 50)
    private String loaiGiamGia;

    @Column(name = "GiaTriGiam", precision = 18, scale = 2)
    private BigDecimal giaTriGiam;

    // Giá trị đơn hàng tối thiểu để áp dụng
    @Column(name = "DonHangToiThieu", precision = 18, scale = 2)
    private BigDecimal giaTriDonHangToiThieu;

    // Giá trị giảm tối đa (chỉ áp dụng cho PERCENT)
    @Column(name = "GiaTriToiDa", precision = 18, scale = 2)
    private BigDecimal giaTriGiamToiDa;

    @Column(name = "NgayBatDau")
    private LocalDateTime ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "SoLuongToiDa")
    private Integer soLanSuDungToiDa;

    @Column(name = "SoLuongDaDung")
    private Integer soLanDaSuDung = 0;

    @Column(name = "TrangThaiKhuyenMai", length = 30)
    private String trangThaiKhuyenMai = "HOAT_DONG";

    @Column(name = "ThoiGianTao")
    private LocalDateTime ngayTao;

    @Column(name = "ThoiGianCapNhat")
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void truocKhiTao() {
        ngayTao = LocalDateTime.now();
        soLanDaSuDung = 0;
    }

    @PreUpdate
    protected void truocKhiCapNhat() {
        ngayCapNhat = LocalDateTime.now();
    }

    /**
     * Kiểm tra voucher có còn hợp lệ không
     */
    public boolean laHopLe() {
        LocalDateTime now = LocalDateTime.now();
        if (!"HOAT_DONG".equals(trangThaiKhuyenMai)) return false;
        if (ngayBatDau != null && now.isBefore(ngayBatDau)) return false;
        if (ngayKetThuc != null && now.isAfter(ngayKetThuc)) return false;
        if (soLanSuDungToiDa != null && soLanDaSuDung != null && soLanDaSuDung >= soLanSuDungToiDa) return false;
        return true;
    }

    /**
     * Tính số tiền giảm
     */
    public BigDecimal tinhSoTienGiam(BigDecimal tongTienGoc) {
        if (!laHopLe()) return BigDecimal.ZERO;
        if (giaTriDonHangToiThieu != null && tongTienGoc.compareTo(giaTriDonHangToiThieu) < 0) return BigDecimal.ZERO;

        BigDecimal soTienGiam;
        if ("PERCENT".equalsIgnoreCase(loaiGiamGia)) {
            soTienGiam = tongTienGoc.multiply(giaTriGiam).divide(BigDecimal.valueOf(100));
            if (giaTriGiamToiDa != null && soTienGiam.compareTo(giaTriGiamToiDa) > 0) {
                soTienGiam = giaTriGiamToiDa;
            }
        } else {
            soTienGiam = giaTriGiam;
        }
        if (soTienGiam.compareTo(tongTienGoc) > 0) soTienGiam = tongTienGoc;
        return soTienGiam;
    }
}

package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ThanhToan")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DonHangId", nullable = false)
    @JsonIgnore
    private DonHang donHang;

    @Column(name = "PhuongThucThanhToan", length = 50)
    private String phuongThucThanhToan;

    @Column(name = "TrangThaiThanhToan", length = 50)
    private String trangThaiThanhToan;

    @Column(name = "SoTienThanhToan", precision = 18, scale = 2)
    private BigDecimal soTienThanhToan;

    @Column(name = "ThoiDiemThanhToan")
    private LocalDateTime thoiDiemThanhToan;

    // VNPay fields
    @Column(name = "MaGiaoDichVNPay", length = 100)
    private String maGiaoDichVNPay;

    @Column(name = "MaNganHang", length = 50)
    private String maNganHang;

    @Column(name = "ThongTinGiaoDich", length = 500)
    private String thongTinGiaoDich;
}

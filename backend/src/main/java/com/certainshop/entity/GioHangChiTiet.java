package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "GioHangChiTiet",
       uniqueConstraints = @UniqueConstraint(columnNames = {"GioHangId", "BienTheId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GioHangChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GioHangId", nullable = false)
    @JsonIgnore
    private GioHang gioHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BienTheId", nullable = false)
    private BienThe bienThe;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "DonGia", precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @Column(name = "ThoiGianCapNhat")
    private LocalDateTime thoiGianCapNhat;

    @PrePersist
    protected void truocKhiTao() {
        thoiGianTao = LocalDateTime.now();
    }

    @PreUpdate
    protected void truocKhiCapNhat() {
        thoiGianCapNhat = LocalDateTime.now();
    }

    public BigDecimal getThanhTien() {
        BigDecimal gia = donGia;
        // Fallback to BienThe.gia if donGia is null or zero
        if ((gia == null || gia.signum() == 0) && bienThe != null) {
            gia = bienThe.getGia();
        }
        if (gia != null && soLuong != null) {
            return gia.multiply(BigDecimal.valueOf(soLuong));
        }
        return BigDecimal.ZERO;
    }
}

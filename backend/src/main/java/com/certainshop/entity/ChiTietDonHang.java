package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietDonHang")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChiTietDonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DonHangId", nullable = false)
    @JsonIgnore
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BienTheId", nullable = false)
    private BienThe bienThe;

    @Column(name = "GiaTaiThoiDiemMua", precision = 18, scale = 2)
    private BigDecimal giaTaiThoiDiemMua;

    @Column(name = "SoLuong")
    private Integer soLuong;

    // ThanhTien computed: not stored in DB
    public java.math.BigDecimal getThanhTien() {
        if (giaTaiThoiDiemMua != null && soLuong != null) {
            return giaTaiThoiDiemMua.multiply(java.math.BigDecimal.valueOf(soLuong));
        }
        return java.math.BigDecimal.ZERO;
    }
}

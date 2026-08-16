package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DiaChiNguoiDung")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiaChiNguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiDungId", nullable = false)
    @JsonIgnore
    private NguoiDung nguoiDung;

    @Column(name = "TenNguoiNhan", length = 150)
    private String hoTen;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "DiaChiCuThe", length = 500)
    private String diaChiDong1;

    // Phường/Xã - tên
    @Column(name = "PhuongXa", length = 100)
    private String phuongXa;

    // Quận/Huyện - tên
    @Column(name = "QuanHuyen", length = 100)
    private String quanHuyen;

    // Tỉnh/Thành - tên
    @Column(name = "TinhThanh", length = 100)
    private String tinhThanh;

    // Mã GHN
    @Column(name = "MaTinhGHN")
    private Integer maTinhGHN;

    @Column(name = "MaHuyenGHN")
    private Integer maHuyenGHN;

    @Column(name = "MaXaGHN", length = 20)
    private String maXaGHN;

    @Column(name = "LaMacDinh")
    private Boolean laMacDinh = false;

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

    // Địa chỉ đầy đủ
    public String getDiaChiDayDu() {
        StringBuilder sb = new StringBuilder();
        if (diaChiDong1 != null && !diaChiDong1.isBlank()) sb.append(diaChiDong1);
        if (phuongXa != null && !phuongXa.isBlank()) sb.append(", ").append(phuongXa);
        if (quanHuyen != null && !quanHuyen.isBlank()) sb.append(", ").append(quanHuyen);
        if (tinhThanh != null && !tinhThanh.isBlank()) sb.append(", ").append(tinhThanh);
        return sb.toString();
    }
}

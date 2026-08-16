package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DanhMuc")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenDanhMuc", length = 150)
    private String tenDanhMuc;

    @Column(name = "DuongDan", length = 255)
    private String duongDan;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    @Column(name = "ThuTuHienThi")
    private Integer thuTuHienThi;

    @Column(name = "DangHoatDong")
    private Boolean dangHoatDong = true;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @OneToMany(mappedBy = "danhMuc", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPham> danhSachSanPham;

    @PrePersist
    protected void truocKhiTao() {
        thoiGianTao = LocalDateTime.now();
    }
}

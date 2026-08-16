package com.certainshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ThuongHieu")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ThuongHieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenThuongHieu", length = 150)
    private String tenThuongHieu;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @PrePersist
    protected void truocKhiTao() {
        thoiGianTao = LocalDateTime.now();
    }
}

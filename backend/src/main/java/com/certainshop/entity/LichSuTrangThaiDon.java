package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LichSuTrangThaiDon")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LichSuTrangThaiDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DonHangId")
    @JsonIgnore
    private DonHang donHang;

    @Column(name = "TrangThaiCu", length = 50)
    private String trangThaiCu;

    @Column(name = "TrangThaiMoi", length = 50)
    private String trangThaiMoi;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiThayDoiId")
    private NguoiDung nguoiThayDoi;

    @Column(name = "ThoiGian")
    private LocalDateTime thoiGian;

    @PrePersist
    protected void truocKhiTao() {
        thoiGian = LocalDateTime.now();
    }
}

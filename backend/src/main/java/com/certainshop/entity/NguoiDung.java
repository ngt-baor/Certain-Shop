package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "NguoiDung")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenDangNhap", unique = true, nullable = false, length = 100)
    private String tenDangNhap;

    @Column(name = "Email", unique = true, length = 150)
    private String email;

    @Column(name = "MatKhauMaHoa", nullable = false, length = 255)
    @JsonIgnore
    private String matKhauMaHoa;

    @Column(name = "HoTen", length = 150)
    private String hoTen;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "GioiTinh")
    private Boolean gioiTinh; // true=Nam, false=Nu, null=Khong xac dinh

    @Column(name = "TrangThai", length = 30)
    private String trangThai = "HOAT_DONG";

    @Column(name = "AnhDaiDien", length = 255)
    private String anhDaiDien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VaiTroId")
    private VaiTro vaiTro;

    @Column(name = "DangHoatDong")
    private Boolean dangHoatDong = true;

    @Column(name = "LanDangNhapCuoi")
    private LocalDateTime lanDangNhapCuoi;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @Column(name = "ThoiGianCapNhat")
    private LocalDateTime thoiGianCapNhat;

    @Column(name = "MaDatLaiMatKhau", length = 255)
    @JsonIgnore
    private String maDatLaiMatKhau;

    @Column(name = "LanDoiMatKhauCuoi")
    private LocalDateTime lanDoiMatKhauCuoi;

    @OneToMany(mappedBy = "nguoiDung", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiaChiNguoiDung> danhSachDiaChi;

    @PrePersist
    protected void truocKhiTao() {
        thoiGianTao = LocalDateTime.now();
        dangHoatDong = true;
        if (trangThai == null) trangThai = "HOAT_DONG";
    }

    @PreUpdate
    protected void truocKhiCapNhat() {
        thoiGianCapNhat = LocalDateTime.now();
    }
}

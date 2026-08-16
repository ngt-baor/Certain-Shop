package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "BienThe")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BienThe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SanPhamId", nullable = false)
    @JsonIgnore
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "KichThuocId")
    private KichThuoc kichThuoc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MauSacId")
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ChatLieuId")
    private ChatLieu chatLieu;

    @Column(name = "GiaBan", precision = 18, scale = 2)
    private BigDecimal gia;

    @Column(name = "SoLuongTon")
    private Integer soLuongTon = 0;

    @Column(name = "MacDinh")
    private Boolean macDinh = false;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    @Column(name = "ThoiGianTao")
    private LocalDateTime ngayTao;

    @Column(name = "ThoiGianCapNhat")
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "bienThe", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<HinhAnhBienThe> danhSachHinhAnh;

    @PrePersist
    protected void truocKhiTao() {
        ngayTao = LocalDateTime.now();
    }

    @PreUpdate
    protected void truocKhiCapNhat() {
        ngayCapNhat = LocalDateTime.now();
    }

    // Lấy ảnh chính
    public String getAnhChinh() {
        if (danhSachHinhAnh == null || danhSachHinhAnh.isEmpty()) return "/img/no-image.png";
        return danhSachHinhAnh.stream()
                .filter(HinhAnhBienThe::getLaAnhChinh)
                .map(HinhAnhBienThe::getDuongDan)
                .findFirst()
                .orElse(danhSachHinhAnh.get(0).getDuongDan());
    }
}

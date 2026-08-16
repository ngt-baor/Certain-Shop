package com.certainshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DonHang")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiDungId")
    private NguoiDung nguoiDung;

    @Column(name = "MaDonHang", length = 50)
    private String maDonHang;

    @Column(name = "TongTienHang", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "SoTienGiamGia", precision = 18, scale = 2)
    private BigDecimal soTienGiamGia = BigDecimal.ZERO;

    @Column(name = "PhiVanChuyen", precision = 18, scale = 2)
    private BigDecimal phiVanChuyen = BigDecimal.ZERO;

    @Column(name = "TongTienThanhToan", precision = 18, scale = 2)
    private BigDecimal tongTienThanhToan;

    @Column(name = "TrangThaiDonHang", length = 50)
    private String trangThaiDonHang;

    // ONLINE hoặc TAI_QUAY
    @Column(name = "LoaiDonHang", length = 50)
    private String loaiDonHang;

    // COD hoặc VNPAY
    @Column(name = "PhuongThucThanhToan", length = 50)
    private String phuongThucThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "KhuyenMaiId")
    private KhuyenMai khuyenMai;

    // Mã voucher áp dụng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VoucherId")
    private Voucher voucher;

    // Thông tin giao hàng
    @Column(name = "TenNguoiNhan", length = 150)
    private String tenNguoiNhan;

    @Column(name = "SdtNguoiNhan", length = 20)
    private String sdtNguoiNhan;

    @Column(name = "DiaChiGiaoHang", length = 500)
    private String diaChiGiaoHang;

    @Column(name = "DaThanhToan")
    private Boolean daThanhToan = false;

    @Column(name = "MaTinhGHN")
    private Integer maTinhGHN;

    @Column(name = "MaHuyenGHN")
    private Integer maHuyenGHN;

    @Column(name = "MaXaGHN", length = 20)
    private String maXaGHN;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    @Column(name = "GhiChuThuNgan", length = 255)
    private String ghiChuThuNgan;

    // Nhân viên tạo/xử lí (cho bán tại quầy)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NhanVienId")
    private NguoiDung nhanVien;

    // VNPay transaction ref
    @Column(name = "VnpayTxnRef", length = 100)
    private String vnPayTransactionRef;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @Column(name = "ThoiGianCapNhat")
    private LocalDateTime thoiGianCapNhat;

    // Thời gian tự hủy (cho hóa đơn chờ tại quầy)
    @Column(name = "ThoiGianTuHuy")
    private LocalDateTime thoiGianTuHuy;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ChiTietDonHang> danhSachChiTiet;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("thoiGian ASC")
    private List<LichSuTrangThaiDon> lichSuTrangThai;

    @OneToOne(mappedBy = "donHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ThanhToan thanhToan;

    @PrePersist
    protected void truocKhiTao() {
        thoiGianTao = LocalDateTime.now();
    }

    @PreUpdate
    protected void truocKhiCapNhat() {
        thoiGianCapNhat = LocalDateTime.now();
    }
}

package com.certainshop.service;

import com.certainshop.entity.*;
import com.certainshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GioHangService {

    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final BienTheRepository bienTheRepository;

    @Value("${app.sanpham.soLuongMuaToiDa:5}")
    private int soLuongMuaToiDa;

    /**
     * Lấy giỏ hàng của người dùng (tạo mới nếu chưa có)
     */
    public GioHang layHoacTaoGioHang(NguoiDung nguoiDung) {
        return gioHangRepository.findByNguoiDungId(nguoiDung.getId())
                .orElseGet(() -> {
                    GioHang gioHang = GioHang.builder()
                            .nguoiDung(nguoiDung)
                            .build();
                    return gioHangRepository.save(gioHang);
                });
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public GioHangChiTiet themVaoGio(Long nguoiDungId, Long bienTheId, int soLuong) {
        GioHang gioHang = gioHangRepository.findByNguoiDungId(nguoiDungId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        BienThe bienThe = bienTheRepository.findById(bienTheId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (!bienThe.getTrangThai()) {
            throw new IllegalArgumentException("Sản phẩm đã ngừng kinh doanh");
        }

        // Determine price: prefer variant price, fall back to product.giaBan
        BigDecimal price = bienThe.getGia();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            price = bienThe.getSanPham() != null ? bienThe.getSanPham().getGiaBan() : null;
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá biến thể không hợp lệ");
        }

        Optional<GioHangChiTiet> chiTietHienTai =
                gioHangChiTietRepository.findByGioHangIdAndBienTheId(gioHang.getId(), bienTheId);

        if (chiTietHienTai.isPresent()) {
            GioHangChiTiet chiTiet = chiTietHienTai.get();
            int soLuongMoi = chiTiet.getSoLuong() + soLuong;

            // Kiểm tra giới hạn mua
            if (soLuongMoi > soLuongMuaToiDa) {
                throw new IllegalArgumentException(
                        "Chỉ được mua tối đa " + soLuongMuaToiDa +
                        " sản phẩm. Liên hệ " + "SĐT 0123456789 nếu cần mua số lượng lớn hơn.");
            }

            // Kiểm tra tồn kho
            if (soLuongMoi > bienThe.getSoLuongTon()) {
                throw new IllegalArgumentException(
                        "Chỉ còn " + bienThe.getSoLuongTon() + " sản phẩm trong kho");
            }

            chiTiet.setSoLuong(soLuongMoi);
            chiTiet.setDonGia(price);
            return gioHangChiTietRepository.save(chiTiet);
        } else {
            if (soLuong > soLuongMuaToiDa) {
                throw new IllegalArgumentException(
                        "Chỉ được mua tối đa " + soLuongMuaToiDa + " sản phẩm mỗi loại");
            }
            if (soLuong > bienThe.getSoLuongTon()) {
                throw new IllegalArgumentException(
                        "Chỉ còn " + bienThe.getSoLuongTon() + " sản phẩm trong kho");
            }

            GioHangChiTiet chiTiet = GioHangChiTiet.builder()
                    .gioHang(gioHang)
                    .bienThe(bienThe)
                    .soLuong(soLuong)
                    .donGia(price)
                    .build();
            return gioHangChiTietRepository.save(chiTiet);
        }
    }

    /**
     * Cập nhật số lượng trong giỏ
     */
    public GioHangChiTiet capNhatSoLuong(Long chiTietId, int soLuongMoi, Long nguoiDungId) {
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng"));

        // Xác minh quyền sở hữu
        if (!chiTiet.getGioHang().getNguoiDung().getId().equals(nguoiDungId)) {
            throw new SecurityException("Không có quyền cập nhật giỏ hàng này");
        }

        if (soLuongMoi <= 0) {
            gioHangChiTietRepository.delete(chiTiet);
            return null;
        }

        if (soLuongMoi > soLuongMuaToiDa) {
            throw new IllegalArgumentException("Chỉ được mua tối đa " + soLuongMuaToiDa + " sản phẩm mỗi loại");
        }

        if (soLuongMoi > chiTiet.getBienThe().getSoLuongTon()) {
            throw new IllegalArgumentException("Chỉ còn " + chiTiet.getBienThe().getSoLuongTon() + " trong kho");
        }

        chiTiet.setSoLuong(soLuongMoi);
        return gioHangChiTietRepository.save(chiTiet);
    }

    /**
     * Xóa 1 sản phẩm khỏi giỏ
     */
    public void xoaKhoiGio(Long chiTietId, Long nguoiDungId) {
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng"));

        if (!chiTiet.getGioHang().getNguoiDung().getId().equals(nguoiDungId)) {
            throw new SecurityException("Không có quyền xóa mục này");
        }
        gioHangChiTietRepository.delete(chiTiet);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void xoaToanBoGio(Long gioHangId) {
        gioHangChiTietRepository.deleteByGioHangId(gioHangId);
    }

    /**
     * Lấy giỏ hàng theo người dùng ID
     */
    @Transactional(readOnly = true)
    public Optional<GioHang> layGioHang(Long nguoiDungId) {
        return gioHangRepository.findByNguoiDungId(nguoiDungId);
    }

    /**
     * Đếm số sản phẩm trong giỏ
     */
    @Transactional(readOnly = true)
    public int demSoLuongGio(Long nguoiDungId) {
        return gioHangRepository.findByNguoiDungId(nguoiDungId)
                .map(gh -> gh.getDanhSachChiTiet() != null ? gh.getDanhSachChiTiet().size() : 0)
                .orElse(0);
    }

    /**
     * Tính tổng tiền giỏ hàng
     */
    @Transactional(readOnly = true)
    public BigDecimal tinhTongTien(Long nguoiDungId) {
        return gioHangRepository.findByNguoiDungId(nguoiDungId)
                .map(gh -> {
                    if (gh.getDanhSachChiTiet() == null) return BigDecimal.ZERO;
                    return gh.getDanhSachChiTiet().stream()
                            .map(GioHangChiTiet::getThanhTien)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                })
                .orElse(BigDecimal.ZERO);
    }

    // === API-friendly wrappers ===

    public GioHangChiTiet themVaoGioHang(NguoiDung nguoiDung, Long bienTheId, int soLuong) {
        return themVaoGio(nguoiDung.getId(), bienTheId, soLuong);
    }

    public void xoaHetGioHang(Long gioHangId) {
        gioHangChiTietRepository.deleteByGioHangId(gioHangId);
    }
}

package com.certainshop.service;

import com.certainshop.entity.Voucher;
import com.certainshop.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VoucherService {

    private final VoucherRepository voucherRepository;

    /**
     * Tìm voucher theo mã
     */
    public Optional<Voucher> timTheoMa(String maVoucher) {
        if (maVoucher == null || maVoucher.isBlank()) return Optional.empty();
        return voucherRepository.findByMaVoucher(maVoucher.trim().toUpperCase());
    }

    /**
     * Kiểm tra & áp dụng voucher cho đơn hàng
     * @param maVoucher Mã voucher
     * @param giaTriDonHang Giá trị đơn hàng
     * @return Giá trị giảm, 0 nếu voucher không hợp lệ
     */
    public BigDecimal tinhGiaTriGiam(String maVoucher, BigDecimal giaTriDonHang) {
        Optional<Voucher> voucherOpt = timTheoMa(maVoucher);
        if (voucherOpt.isEmpty()) {
            log.warn("[Voucher] Voucher không tồn tại: {}", maVoucher);
            return BigDecimal.ZERO;
        }

        Voucher voucher = voucherOpt.get();

        // Validate voucher
        if (!voucher.isValid()) {
            log.warn("[Voucher] Voucher không hợp lệ hoặc hết hạn: {}", maVoucher);
            return BigDecimal.ZERO;
        }

        // Check minimum order value
        if (voucher.getGiaTriToiThieu() != null &&
                giaTriDonHang.compareTo(voucher.getGiaTriToiThieu()) < 0) {
            log.warn("[Voucher] Đơn hàng ({}) không đủ giá trị tối thiểu ({})",
                    giaTriDonHang, voucher.getGiaTriToiThieu());
            return BigDecimal.ZERO;
        }

        return voucher.tinhGiaTriGiam(giaTriDonHang);
    }

    /**
     * Danh sách voucher còn hiệu lực (cho customers)
     */
    @Transactional(readOnly = true)
    public List<Voucher> danhSachVoucherHoatDong() {
        return voucherRepository.findAllValidVouchers(LocalDateTime.now());
    }

    /**
     * Danh sách TẤT CẢ vouchers hoạt động (không bị xóa) cho admin
     */
    @Transactional(readOnly = true)
    public List<Voucher> danhSachTatCaVoucher() {
        return voucherRepository.findAllActive();
    }

    /**
     * Tăng số lần sử dụng voucher (gọi khi đơn hàng được xác nhận)
     */
    public void tangSoLanSuDung(String maVoucher) {
        Optional<Voucher> voucherOpt = timTheoMa(maVoucher);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setSoLuongSuDung(voucher.getSoLuongSuDung() != null ? voucher.getSoLuongSuDung() + 1 : 1);
            voucherRepository.save(voucher);
            log.info("[Voucher] Đã tăng lần sử dụng: {} (total: {})", maVoucher, voucher.getSoLuongSuDung());
        }
    }

    /**
     * Giảm số lần sử dụng voucher (khi hủy đơn)
     */
    public void giamSoLanSuDung(String maVoucher) {
        Optional<Voucher> voucherOpt = timTheoMa(maVoucher);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            int current = voucher.getSoLuongSuDung() != null ? voucher.getSoLuongSuDung() : 0;
            voucher.setSoLuongSuDung(Math.max(0, current - 1));
            voucherRepository.save(voucher);
            log.info("[Voucher] Đã giảm lần sử dụng: {} (total: {})", maVoucher, voucher.getSoLuongSuDung());
        }
    }

    /**
     * Tạo voucher mới (admin only)
     */
    public Voucher taoVoucher(Voucher voucher) {
        validateVoucher(voucher);
        voucher.setMaVoucher(voucher.getMaVoucher().trim().toUpperCase());
        if (voucherRepository.existsByMaVoucher(voucher.getMaVoucher())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại: " + voucher.getMaVoucher());
        }
        return voucherRepository.save(voucher);
    }

    /**
     * Cập nhật voucher (admin only)
     */
    public Voucher capNhatVoucher(Long id, Voucher updates) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        updates.setMaVoucher(voucher.getMaVoucher());
        validateVoucher(updates);

        voucher.setMoTa(updates.getMoTa());
        voucher.setNgayBatDau(updates.getNgayBatDau());
        voucher.setNgayKetThuc(updates.getNgayKetThuc());
        voucher.setGiaTriToiThieu(updates.getGiaTriToiThieu());
        voucher.setGiaTriGiam(updates.getGiaTriGiam());
        voucher.setGiaTriGiamToiDa(updates.getGiaTriGiamToiDa());
        voucher.setLoaiGiam(updates.getLoaiGiam());
        voucher.setSoLuongToiDa(updates.getSoLuongToiDa());
        voucher.setTrangThai(updates.getTrangThai());

        return voucherRepository.save(voucher);
    }

    private void validateVoucher(Voucher voucher) {
        if (voucher == null || voucher.getMaVoucher() == null || voucher.getMaVoucher().isBlank()) {
            throw new IllegalArgumentException("Mã voucher không được để trống");
        }
        if (voucher.getNgayBatDau() == null || voucher.getNgayKetThuc() == null
                || !voucher.getNgayBatDau().isBefore(voucher.getNgayKetThuc())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (!"PERCENT".equals(voucher.getLoaiGiam()) && !"FIXED".equals(voucher.getLoaiGiam())) {
            throw new IllegalArgumentException("Loại giảm phải là PERCENT hoặc FIXED");
        }
        if (voucher.getGiaTriGiam() == null || voucher.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
        }
        if ("PERCENT".equals(voucher.getLoaiGiam())
                && voucher.getGiaTriGiam().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Giá trị giảm phần trăm không được vượt quá 100");
        }
        if (voucher.getGiaTriGiamToiDa() == null
                || voucher.getGiaTriGiamToiDa().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm tối đa phải lớn hơn 0");
        }
        if (voucher.getGiaTriToiThieu() != null
                && voucher.getGiaTriToiThieu().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị đơn tối thiểu không được âm");
        }
        if (voucher.getSoLuongToiDa() != null && voucher.getSoLuongToiDa() <= 0) {
            throw new IllegalArgumentException("Số lượt sử dụng tối đa phải lớn hơn 0");
        }
    }

    /**
     * Xóa voucher (soft delete)
     */
    public void xoaVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
        voucher.setTrangThai(false);
        voucherRepository.save(voucher);
    }
}

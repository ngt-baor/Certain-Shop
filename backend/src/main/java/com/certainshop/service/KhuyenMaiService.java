package com.certainshop.service;

import com.certainshop.entity.KhuyenMai;
import com.certainshop.repository.KhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class KhuyenMaiService {

    private final KhuyenMaiRepository khuyenMaiRepository;

    public KhuyenMai taoKhuyenMai(KhuyenMai khuyenMai) {
        if (khuyenMaiRepository.existsByMaKhuyenMai(khuyenMai.getMaKhuyenMai())) {
            throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại");
        }
        validateKhuyenMai(khuyenMai);
        return khuyenMaiRepository.save(khuyenMai);
    }

    public KhuyenMai capNhatKhuyenMai(Long id, KhuyenMai khuyenMaiMoi) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

        if (khuyenMaiRepository.existsByMaKhuyenMaiAndIdNot(khuyenMaiMoi.getMaKhuyenMai(), id)) {
            throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại");
        }

        validateKhuyenMai(khuyenMaiMoi);

        km.setMaKhuyenMai(khuyenMaiMoi.getMaKhuyenMai());
        km.setTenKhuyenMai(khuyenMaiMoi.getTenKhuyenMai());
        km.setMoTa(khuyenMaiMoi.getMoTa());
        km.setLoaiGiamGia(khuyenMaiMoi.getLoaiGiamGia());
        km.setGiaTriGiam(khuyenMaiMoi.getGiaTriGiam());
        km.setGiaTriDonHangToiThieu(khuyenMaiMoi.getGiaTriDonHangToiThieu());
        km.setGiaTriGiamToiDa(khuyenMaiMoi.getGiaTriGiamToiDa());
        km.setNgayBatDau(khuyenMaiMoi.getNgayBatDau());
        km.setNgayKetThuc(khuyenMaiMoi.getNgayKetThuc());
        km.setSoLanSuDungToiDa(khuyenMaiMoi.getSoLanSuDungToiDa());
        km.setTrangThaiKhuyenMai(khuyenMaiMoi.getTrangThaiKhuyenMai());

        return khuyenMaiRepository.save(km);
    }

    /**
     * Xóa mềm voucher
     */
    public void xoaKhuyenMai(Long id) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
        km.setTrangThaiKhuyenMai("DA_XOA");
        khuyenMaiRepository.save(km);
    }

    /**
     * Áp dụng voucher tốt nhất tự động
     */
    @Transactional(readOnly = true)
    public Optional<KhuyenMai> timVoucherTotNhat(BigDecimal tongTien) {
        List<KhuyenMai> danh = khuyenMaiRepository.findVoucherHopLe(LocalDateTime.now(), tongTien);
        if (danh.isEmpty()) return Optional.empty();

        // Chọn voucher giảm nhiều nhất
        return danh.stream()
                .max((a, b) -> a.tinhSoTienGiam(tongTien).compareTo(b.tinhSoTienGiam(tongTien)));
    }

    @Transactional(readOnly = true)
    public List<KhuyenMai> layVoucherHopLe(BigDecimal tongTien) {
        return khuyenMaiRepository.findVoucherHopLe(LocalDateTime.now(), tongTien);
    }

    @Transactional(readOnly = true)
    public Optional<KhuyenMai> timTheoMa(String maKhuyenMai) {
        return khuyenMaiRepository.findByMaKhuyenMai(maKhuyenMai);
    }

    @Transactional(readOnly = true)
    public Optional<KhuyenMai> timTheoId(Long id) {
        return khuyenMaiRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<KhuyenMai> timKiem(String tuKhoa, String trangThai, Pageable pageable) {
        return khuyenMaiRepository.timKiem(tuKhoa, trangThai, pageable);
    }

    /**
     * Tăng số lần đã sử dụng
     */
    public void tangSoLanSuDung(Long id) {
        khuyenMaiRepository.findById(id).ifPresent(km -> {
            km.setSoLanDaSuDung(km.getSoLanDaSuDung() + 1);
            khuyenMaiRepository.save(km);
        });
    }

    /**
     * Giảm số lần đã sử dụng (khi hủy đơn)
     */
    public void giamSoLanSuDung(Long id) {
        khuyenMaiRepository.findById(id).ifPresent(km -> {
            if (km.getSoLanDaSuDung() > 0) {
                km.setSoLanDaSuDung(km.getSoLanDaSuDung() - 1);
                khuyenMaiRepository.save(km);
            }
        });
    }

    private void validateKhuyenMai(KhuyenMai km) {
        if (km.getNgayKetThuc() != null && km.getNgayBatDau() != null
                && km.getNgayKetThuc().isBefore(km.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if ("PERCENT".equals(km.getLoaiGiamGia())) {
            if (km.getGiaTriGiam().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Phần trăm giảm không được vượt quá 100%");
            }
        }
    }
}

package com.certainshop.controller;

import com.certainshop.dto.DatHangDto;
import com.certainshop.entity.*;
import com.certainshop.service.*;
import com.certainshop.util.NguoiDungHienTai;
import com.certainshop.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/dat-hang")
@RequiredArgsConstructor
public class DatHangController {

    private final DonHangService donHangService;
    private final GioHangService gioHangService;
    private final KhuyenMaiService khuyenMaiService;
    private final DiaChiService diaChiService;
    private final GHNApiService ghnApiService;
    private final NguoiDungHienTai nguoiDungHienTai;
    private final VNPayUtil vnPayUtil;

    @Value("${app.shop.soDienThoai:0123456789}")
    private String sdtShop;

    @GetMapping
    public String hienThiDatHang(Model model) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();

        Optional<GioHang> gioHang = gioHangService.layGioHang(nguoiDung.getId());
        if (gioHang.isEmpty() || gioHang.get().getDanhSachChiTiet() == null
                || gioHang.get().getDanhSachChiTiet().isEmpty()) {
            return "redirect:/gio-hang?loi=Giỏ hàng trống";
        }

        BigDecimal tongTien = gioHangService.tinhTongTien(nguoiDung.getId());

        List<DiaChiNguoiDung> danhSachDiaChi = diaChiService.layDanhSachDiaChi(nguoiDung.getId());
        List<KhuyenMai> voucherHopLe = khuyenMaiService.layVoucherHopLe(tongTien);

        model.addAttribute("nguoiDung", nguoiDung);
        model.addAttribute("gioHang", gioHang.get());
        model.addAttribute("tongTien", tongTien);
        model.addAttribute("danhSachDiaChi", danhSachDiaChi);
        model.addAttribute("voucherHopLe", voucherHopLe);
        model.addAttribute("datHangDto", new DatHangDto());
        model.addAttribute("sdtShop", sdtShop);

        return "khach/dat-hang";
    }

    @PostMapping
    public String xuLyDatHang(
            @ModelAttribute DatHangDto dto,
            HttpServletRequest request,
            RedirectAttributes ra) {

        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();

        try {
            DonHang donHang = donHangService.datHangOnline(nguoiDung.getId(), dto);

            // VNPay redirect
            if ("VNPAY".equalsIgnoreCase(dto.getPhuongThucThanhToan())) {
                String ip = VNPayUtil.layIpKhachHang(request);
                String moTa = "Thanh toan don hang " + donHang.getMaDonHang();
                long soTien = donHang.getTongTienThanhToan().longValue();
                String urlThanhToan = vnPayUtil.taoUrlThanhToan(donHang.getMaDonHang(), soTien, moTa, ip);
                return "redirect:" + urlThanhToan;
            }

            ra.addFlashAttribute("thanhCong",
                    "Đặt hàng thành công! Mã đơn: " + donHang.getMaDonHang());
            return "redirect:/don-hang/cua-toi/" + donHang.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
            return "redirect:/dat-hang";
        }
    }

    // Tính phí vận chuyển (AJAX)
    @GetMapping("/tinh-phi-ship")
    @ResponseBody
    public ResponseEntity<?> tinhPhiShip(
            @RequestParam int maHuyenGHN,
            @RequestParam String maXaGHN) {
        BigDecimal phi = ghnApiService.tinhPhiVanChuyen(maHuyenGHN, maXaGHN, 300);
        return ResponseEntity.ok(Map.of("phiVanChuyen", phi));
    }

    // Áp dụng voucher (AJAX)
    @PostMapping("/ap-voucher")
    @ResponseBody
    public ResponseEntity<?> apVoucher(
            @RequestParam String maVoucher,
            @RequestParam BigDecimal tongTien) {
        try {
            Optional<KhuyenMai> km = khuyenMaiService.timTheoMa(maVoucher);
            if (km.isEmpty() || !km.get().laHopLe()) {
                return ResponseEntity.badRequest().body(Map.of("thanhCong", false,
                        "thongBao", "Mã voucher không hợp lệ hoặc đã hết hạn"));
            }

            KhuyenMai voucher = km.get();
            if (voucher.getGiaTriDonHangToiThieu() != null
                    && tongTien.compareTo(voucher.getGiaTriDonHangToiThieu()) < 0) {
                return ResponseEntity.badRequest().body(Map.of("thanhCong", false,
                        "thongBao", "Đơn hàng tối thiểu " +
                                String.format("%,.0f", voucher.getGiaTriDonHangToiThieu()) + "đ để áp dụng"));
            }

            BigDecimal soTienGiam = voucher.tinhSoTienGiam(tongTien);
            return ResponseEntity.ok(Map.of(
                    "thanhCong", true,
                    "soTienGiam", soTienGiam,
                    "khuyenMaiId", voucher.getId(),
                    "tenKhuyenMai", voucher.getTenKhuyenMai()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }
}

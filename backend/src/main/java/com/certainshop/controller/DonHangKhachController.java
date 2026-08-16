package com.certainshop.controller;

import com.certainshop.entity.DonHang;
import com.certainshop.entity.NguoiDung;
import com.certainshop.service.DonHangService;
import com.certainshop.util.NguoiDungHienTai;
import com.certainshop.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class DonHangKhachController {

    private final DonHangService donHangService;
    private final NguoiDungHienTai nguoiDungHienTai;
    private final VNPayUtil vnPayUtil;

    @Value("${app.fe.url:http://localhost:5173}")
    private String feUrl;

    @GetMapping("/don-hang/cua-toi")
    public String danhSachDonHang(Model model) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        List<DonHang> danhSach = donHangService.layDonHangCuaKhach(nguoiDung.getId());
        model.addAttribute("danhSachDonHang", danhSach);
        return "khach/don-hang-cua-toi";
    }

    @GetMapping("/don-hang/cua-toi/{id}")
    public String chiTietDonHang(@PathVariable Long id, Model model) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        Optional<DonHang> donHangOpt = donHangService.timTheoId(id);

        if (donHangOpt.isEmpty()) {
            return "redirect:/don-hang/cua-toi?loi=Không tìm thấy đơn hàng";
        }

        DonHang donHang = donHangOpt.get();
        // Đảm bảo đơn là của người dùng này
        if (donHang.getNguoiDung() == null || !donHang.getNguoiDung().getId().equals(nguoiDung.getId())) {
            return "redirect:/don-hang/cua-toi?loi=Không có quyền xem đơn hàng này";
        }

        model.addAttribute("donHang", donHang);
        return "khach/chi-tiet-don-hang";
    }

    @PostMapping("/don-hang/huy/{id}")
    public String khachHuyDon(
            @PathVariable Long id,
            @RequestParam String lyDo,
            RedirectAttributes ra) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        try {
            donHangService.khachHuyDon(id, lyDo, nguoiDung.getId());
            ra.addFlashAttribute("thanhCong", "Đã hủy đơn hàng thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/don-hang/cua-toi/" + id;
    }

    // ========== VNPay Return URL ==========

    @GetMapping("/thanh-toan/vnpay-return")
    public String vnPayReturn(
            HttpServletRequest request,
            RedirectAttributes ra) {

        // Thu thập tất cả tham số từ VNPay
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) params.put(key, values[0]);
        });

        String responseCode = params.get("vnp_ResponseCode");
        String maDonHang = params.get("vnp_TxnRef");
        String maGiaoDich = params.get("vnp_TransactionNo");

        // Xác thực chữ ký
        boolean chuKyHopLe = vnPayUtil.xacThucChuKy(params);

        if (!chuKyHopLe) {
            log.warn("VNPay callback chữ ký không hợp lệ cho đơn: {}", maDonHang);
            ra.addFlashAttribute("loiThongBao", "Xác thực thanh toán thất bại");
            return "redirect:" + feUrl + "/don-hang-cua-toi";
        }

        if ("00".equals(responseCode)
                && "00".equals(params.get("vnp_TransactionStatus"))) {
            // Thanh toán thành công
            try {
                if (!"VND".equals(params.get("vnp_CurrCode"))) {
                    throw new IllegalArgumentException("Đơn vị tiền tệ VNPay không hợp lệ");
                }
                BigDecimal soTienThanhToan = new BigDecimal(params.get("vnp_Amount")).movePointLeft(2);
                DonHang donHang = donHangService.xacNhanThanhToanVNPay(
                        maDonHang, maGiaoDich, soTienThanhToan);
                ra.addFlashAttribute("thanhCong",
                        "Thanh toán thành công! Mã giao dịch: " + maGiaoDich);
                return "redirect:" + feUrl + "/don-hang-cua-toi/" + donHang.getMaDonHang();
            } catch (Exception e) {
                log.error("Lỗi xác nhận VNPay: {}", e.getMessage());
                ra.addFlashAttribute("loiThongBao", "Lỗi xử lý thanh toán: " + e.getMessage());
                return "redirect:" + feUrl + "/don-hang-cua-toi";
            }
        } else {
            // Thanh toán thất bại hoặc hủy
            ra.addFlashAttribute("loiThongBao",
                    "Thanh toán không thành công (Mã phản hồi: " + responseCode + "). Đơn hàng sẽ tự hủy.");
            return "redirect:" + feUrl + "/don-hang-cua-toi";
        }
    }
}

package com.certainshop.controller;

import com.certainshop.entity.GioHang;
import com.certainshop.entity.NguoiDung;
import com.certainshop.service.GioHangService;
import com.certainshop.util.NguoiDungHienTai;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/gio-hang")
public class GioHangController {

    private final GioHangService gioHangService;
    private final NguoiDungHienTai nguoiDungHienTai;

    @GetMapping
    public String hienThiGioHang(Model model) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        Optional<GioHang> gioHang = gioHangService.layGioHang(nguoiDung.getId());
        model.addAttribute("gioHang", gioHang.orElse(null));
        model.addAttribute("tongTien", gioHangService.tinhTongTien(nguoiDung.getId()));
        return "khach/gio-hang";
    }

    @PostMapping("/them")
    @ResponseBody
    public ResponseEntity<?> themVaoGio(
            @RequestParam Long bienTheId,
            @RequestParam(defaultValue = "1") int soLuong) {
        try {
            NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
            gioHangService.themVaoGio(nguoiDung.getId(), bienTheId, soLuong);
            int soPhanTuGio = gioHangService.demSoLuongGio(nguoiDung.getId());
            return ResponseEntity.ok(Map.of("thanhCong", true, "soLuongGio", soPhanTuGio,
                    "thongBao", "Đã thêm vào giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    @PostMapping("/cap-nhat")
    @ResponseBody
    public ResponseEntity<?> capNhatSoLuong(
            @RequestParam Long chiTietId,
            @RequestParam int soLuong) {
        try {
            NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
            gioHangService.capNhatSoLuong(chiTietId, soLuong, nguoiDung.getId());
            return ResponseEntity.ok(Map.of("thanhCong", true,
                    "tongTien", gioHangService.tinhTongTien(nguoiDung.getId())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    @PostMapping("/xoa")
    public String xoaKhoiGio(
            @RequestParam Long chiTietId,
            RedirectAttributes ra) {
        try {
            NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
            gioHangService.xoaKhoiGio(chiTietId, nguoiDung.getId());
            ra.addFlashAttribute("thongBao", "Đã xóa sản phẩm khỏi giỏ hàng");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/gio-hang";
    }
}

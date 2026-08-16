package com.certainshop.controller;

import com.certainshop.entity.*;
import com.certainshop.service.*;
import com.certainshop.util.NguoiDungHienTai;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller trang chủ và hiển thị sản phẩm cho khách
 */
@Controller
@RequiredArgsConstructor
public class TrangChuController {

    private final SanPhamService sanPhamService;
    private final com.certainshop.repository.DanhMucRepository dmRepo;
    private final com.certainshop.repository.ThuongHieuRepository thuongHieuRepo;
    private final GioHangService gioHangService;
    private final NguoiDungHienTai nguoiDungHienTai;

    @GetMapping({"/", "/trang-chu"})
    public String trangChu(Model model) {
        // Sản phẩm mới - áo phông (trang 1, 8 sản phẩm)
        Pageable trang = PageRequest.of(0, 8);
        Page<SanPham> sanPhamMoi = sanPhamService.timKiemChoKhachHang(null, null, null, trang);

        // Danh mục con áo phông từ DB
        List<DanhMuc> danhSachDanhMuc = dmRepo.findByDangHoatDongTrueOrderByThuTuHienThiAsc();

        model.addAttribute("sanPhamMoi", sanPhamMoi.getContent());
        model.addAttribute("danhSachDanhMuc", danhSachDanhMuc);
        themThongTinChung(model);
        return "khach/trang-chu";
    }

    @GetMapping("/san-pham/{id}")
    public String chiTietSanPham(@PathVariable Long id, Model model) {
        Optional<SanPham> sanPhamOpt = sanPhamService.timTheoId(id);
        if (sanPhamOpt.isEmpty() || !sanPhamOpt.get().isTrangThai()) {
            return "redirect:/?loi=Sản phẩm không tồn tại";
        }

        SanPham sanPham = sanPhamOpt.get();
        List<BienThe> danhSachBienThe = sanPhamService.danhSachBienTheCuaSanPham(id);

        // Sản phẩm liên quan cùng danh mục
        Pageable trang = PageRequest.of(0, 4);
        List<SanPham> sanPhamLienQuan = sanPhamService
                .timKiemChoKhachHang(null, sanPham.getDanhMuc().getId(), null, trang)
                .getContent();

        model.addAttribute("sanPham", sanPham);
        model.addAttribute("danhSachBienThe", danhSachBienThe);
        model.addAttribute("sanPhamLienQuan", sanPhamLienQuan);
        themThongTinChung(model);
        return "khach/chi-tiet-san-pham";
    }

    @GetMapping("/tim-kiem")
    public String timKiem(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long danhMucId,
            @RequestParam(required = false) Long thuongHieuId,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "12") int soLuong,
            Model model) {

        Pageable pageable = PageRequest.of(trang, soLuong);
        Page<SanPham> ketQua = sanPhamService.timKiemChoKhachHang(q, danhMucId, thuongHieuId, pageable);

        model.addAttribute("ketQua", ketQua);
        model.addAttribute("tuKhoa", q);
        model.addAttribute("danhMucId", danhMucId);
        model.addAttribute("thuongHieuId", thuongHieuId);
        model.addAttribute("danhSachThuongHieu", thuongHieuRepo.findByTrangThaiTrueOrderByTenThuongHieuAsc());
        themThongTinChung(model);
        return "khach/tim-kiem";
    }

    @GetMapping("/danh-muc/{duongDan}")
    public String danhMuc(
            @PathVariable String duongDan,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {

        Pageable pageable = PageRequest.of(trang, 12);
        Page<SanPham> sanPham = sanPhamService.layTheoCategory(duongDan, pageable);

        Optional<DanhMuc> danhMuc = dmRepo.findByDuongDan(duongDan);

        model.addAttribute("sanPham", sanPham);
        model.addAttribute("danhMuc", danhMuc.orElse(null));
        model.addAttribute("duongDan", duongDan);
        themThongTinChung(model);
        return "khach/danh-muc";
    }

    @GetMapping("/khong-co-quyen")
    public String khongCoQuyen() {
        return "loi/403";
    }

    // Thêm thông tin chung (danh mục, số lượng giỏ hàng) vào model
    private void themThongTinChung(Model model) {
        List<DanhMuc> danhSachDanhMuc = dmRepo.findByDangHoatDongTrueOrderByThuTuHienThiAsc();
        model.addAttribute("danhSachDanhMucNav", danhSachDanhMuc);

        Optional<NguoiDung> nguoiDungHT = nguoiDungHienTai.lay();
        if (nguoiDungHT.isPresent()) {
            model.addAttribute("soLuongGio", gioHangService.demSoLuongGio(nguoiDungHT.get().getId()));
        } else {
            model.addAttribute("soLuongGio", 0);
        }
    }
}

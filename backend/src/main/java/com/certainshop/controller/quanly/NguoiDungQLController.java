package com.certainshop.controller.quanly;

import com.certainshop.constant.VaiTroConst;
import com.certainshop.entity.NguoiDung;
import com.certainshop.repository.NguoiDungRepository;
import com.certainshop.repository.VaiTroRepository;
import com.certainshop.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class NguoiDungQLController {

    private final NguoiDungService nguoiDungService;
    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository;

    // ===== QUẢN LÝ KHÁCH HÀNG (ADMIN + NHÂN VIÊN XEM) =====

    @GetMapping("/quan-ly/nguoi-dung")
    @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
    public String danhSachKhachHang(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {

        Page<NguoiDung> danhSach = nguoiDungRepository
                .timKiem(q, VaiTroConst.KHACH_HANG, PageRequest.of(trang, 15));

        model.addAttribute("danhSach", danhSach);
        model.addAttribute("q", q);
        model.addAttribute("loaiNguoiDung", "khach-hang");
        return "quan-ly/nguoi-dung/danh-sach";
    }

    @GetMapping("/quan-ly/nguoi-dung/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
    public String chiTietKhachHang(@PathVariable Long id, Model model) {
        NguoiDung nd = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        model.addAttribute("nguoiDung", nd);
        return "quan-ly/nguoi-dung/chi-tiet";
    }

    @PostMapping("/quan-ly/nguoi-dung/{id}/doi-trang-thai")
    @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
    public String doiTrangThaiKhachHang(@PathVariable Long id, RedirectAttributes ra) {
        try {
            NguoiDung nd = nguoiDungRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            nguoiDungService.doiTrangThaiTaiKhoan(id, !Boolean.TRUE.equals(nd.getDangHoatDong()));
            ra.addFlashAttribute("thanhCong", "Đã thay đổi trạng thái tài khoản");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/nguoi-dung/" + id;
    }

    // ===== QUẢN LÝ NHÂN VIÊN (CHỈ ADMIN) =====

    @GetMapping("/admin/nhan-vien")
    @PreAuthorize("hasRole('ADMIN')")
    public String danhSachNhanVien(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {

        Page<NguoiDung> danhSach = nguoiDungRepository
                .timKiem(q, VaiTroConst.NHAN_VIEN, PageRequest.of(trang, 15));

        model.addAttribute("danhSach", danhSach);
        model.addAttribute("q", q);
        model.addAttribute("loaiNguoiDung", "nhan-vien");
        return "quan-ly/nguoi-dung/danh-sach";
    }

    @GetMapping("/admin/nhan-vien/them")
    @PreAuthorize("hasRole('ADMIN')")
    public String hienThiThemNhanVien(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "quan-ly/nguoi-dung/them-nhan-vien";
    }

    @PostMapping("/admin/nhan-vien/them")
    @PreAuthorize("hasRole('ADMIN')")
    public String xuLyThemNhanVien(
            @RequestParam String hoTen,
            @RequestParam String email,
            @RequestParam String soDienThoai,
            @RequestParam String matKhau,
            RedirectAttributes ra) {
        try {
            NguoiDung nv = new NguoiDung();
            nv.setHoTen(hoTen);
            nv.setEmail(email);
            nv.setSoDienThoai(soDienThoai);
            nv.setTenDangNhap(email);
            nguoiDungService.taoNhanVien(nv, matKhau, 2);
            ra.addFlashAttribute("thanhCong", "Tạo tài khoản nhân viên thành công");
            return "redirect:/admin/nhan-vien";
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
            return "redirect:/admin/nhan-vien/them";
        }
    }

    @PostMapping("/admin/nhan-vien/{id}/doi-vai-tro")
    @PreAuthorize("hasRole('ADMIN')")
    public String doiVaiTro(
            @PathVariable Long id,
            @RequestParam Integer vaiTroId,
            RedirectAttributes ra) {
        try {
            nguoiDungService.doiVaiTro(id, vaiTroId);
            ra.addFlashAttribute("thanhCong", "Đã thay đổi vai trò thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/nguoi-dung/" + id;
    }

    @PostMapping("/admin/nhan-vien/{id}/doi-trang-thai")
    @PreAuthorize("hasRole('ADMIN')")
    public String doiTrangThaiNhanVien(@PathVariable Long id, RedirectAttributes ra) {
        try {
            NguoiDung nd = nguoiDungRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            nguoiDungService.doiTrangThaiTaiKhoan(id, !Boolean.TRUE.equals(nd.getDangHoatDong()));
            ra.addFlashAttribute("thanhCong", "Đã thay đổi trạng thái tài khoản");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/admin/nhan-vien";
    }
}

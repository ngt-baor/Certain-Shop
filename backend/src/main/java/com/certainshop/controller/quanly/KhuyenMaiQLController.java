package com.certainshop.controller.quanly;

import com.certainshop.entity.KhuyenMai;
import com.certainshop.repository.KhuyenMaiRepository;
import com.certainshop.service.KhuyenMaiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/quan-ly/khuyen-mai")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class KhuyenMaiQLController {

    private final KhuyenMaiService khuyenMaiService;
    private final KhuyenMaiRepository khuyenMaiRepository;

    @GetMapping
    public String danhSach(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String conHieuLuc,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {

        Page<KhuyenMai> danhSach = khuyenMaiService.timKiem(q, conHieuLuc, PageRequest.of(trang, 10));
        model.addAttribute("danhSach", danhSach);
        model.addAttribute("q", q);
        model.addAttribute("conHieuLuc", conHieuLuc);
        model.addAttribute("thoiGianHienTai", LocalDateTime.now());
        return "quan-ly/khuyen-mai/danh-sach";
    }

    @GetMapping("/them")
    public String hienThiThem(Model model) {
        model.addAttribute("khuyenMai", new KhuyenMai());
        return "quan-ly/khuyen-mai/them-moi";
    }

    @PostMapping("/them")
    public String xuLyThem(
            @Valid @ModelAttribute KhuyenMai khuyenMai,
            BindingResult result,
            RedirectAttributes ra) {
        if (result.hasErrors()) return "quan-ly/khuyen-mai/them-moi";
        try {
            khuyenMaiService.taoKhuyenMai(khuyenMai);
            ra.addFlashAttribute("thanhCong", "Tạo khuyến mãi thành công");
            return "redirect:/quan-ly/khuyen-mai";
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
            return "redirect:/quan-ly/khuyen-mai/them";
        }
    }

    @GetMapping("/{id}/sua")
    public String hienThiSua(@PathVariable Long id, Model model) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
        model.addAttribute("khuyenMai", km);
        return "quan-ly/khuyen-mai/sua";
    }

    @PostMapping("/{id}/sua")
    public String xuLySua(
            @PathVariable Long id,
            @Valid @ModelAttribute KhuyenMai khuyenMai,
            BindingResult result,
            RedirectAttributes ra) {
        if (result.hasErrors()) return "quan-ly/khuyen-mai/sua";
        try {
            khuyenMaiService.capNhatKhuyenMai(id, khuyenMai);
            ra.addFlashAttribute("thanhCong", "Cập nhật khuyến mãi thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/khuyen-mai";
    }

    @PostMapping("/{id}/xoa")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            khuyenMaiService.xoaKhuyenMai(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa khuyến mãi");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/khuyen-mai";
    }
}

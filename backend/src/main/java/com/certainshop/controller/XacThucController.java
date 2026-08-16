package com.certainshop.controller;

import com.certainshop.dto.DangKyDto;
import com.certainshop.service.NguoiDungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class XacThucController {

    private final NguoiDungService nguoiDungService;

    @GetMapping("/dang-nhap")
    public String hienThiDangNhap(
            @RequestParam(required = false) String loi,
            @RequestParam(required = false) String dangXuat,
            @RequestParam(required = false) String hetPhien,
            Model model) {
        if (loi != null) model.addAttribute("loiDangNhap", loi);
        if (dangXuat != null) model.addAttribute("thongBaoDangXuat", "Bạn đã đăng xuất thành công");
        if (hetPhien != null) model.addAttribute("loiDangNhap", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        return "xac-thuc/dang-nhap";
    }

    @GetMapping("/dang-ky")
    public String hienThiDangKy(Model model) {
        model.addAttribute("dangKyDto", new DangKyDto());
        return "xac-thuc/dang-ky";
    }

    @PostMapping("/dang-ky")
    public String xuLyDangKy(
            @Valid @ModelAttribute("dangKyDto") DangKyDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "xac-thuc/dang-ky";
        }

        try {
            nguoiDungService.dangKy(dto);
            redirectAttributes.addFlashAttribute("thanhCong",
                    "Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/dang-nhap";
        } catch (IllegalArgumentException e) {
            model.addAttribute("loiDangKy", e.getMessage());
            return "xac-thuc/dang-ky";
        }
    }
}

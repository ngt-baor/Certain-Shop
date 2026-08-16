package com.certainshop.controller;

import com.certainshop.entity.*;
import com.certainshop.service.DiaChiService;
import com.certainshop.service.NguoiDungService;
import com.certainshop.util.NguoiDungHienTai;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tai-khoan")
@RequiredArgsConstructor
public class TaiKhoanController {

    private final NguoiDungService nguoiDungService;
    private final DiaChiService diaChiService;
    private final NguoiDungHienTai nguoiDungHienTai;

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;

    @GetMapping("/thong-tin")
    public String thongTin(Model model) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        model.addAttribute("nguoiDung", nguoiDung);
        return "khach/tai-khoan/thong-tin";
    }

    @PostMapping("/cap-nhat-thong-tin")
    public String capNhatThongTin(
            @ModelAttribute NguoiDung thongTinMoi,
            @RequestParam(required = false) MultipartFile anhDaiDien,
            RedirectAttributes ra) throws IOException {

        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();

        // Upload ảnh nếu có
        if (anhDaiDien != null && !anhDaiDien.isEmpty()) {
            String tenFile = "avatar_" + nguoiDung.getId() + "_" + UUID.randomUUID() +
                    anhDaiDien.getOriginalFilename().substring(
                            anhDaiDien.getOriginalFilename().lastIndexOf("."));
            Path path = Paths.get(uploadDir).resolve(tenFile);
            Files.createDirectories(path.getParent());
            Files.copy(anhDaiDien.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            nguoiDungService.capNhatAnh(nguoiDung.getId(), "/uploads/images/" + tenFile);
        }

        try {
            nguoiDungService.capNhatThongTin(nguoiDung.getId(), thongTinMoi);
            ra.addFlashAttribute("thanhCong", "Cập nhật thông tin thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/tai-khoan/thong-tin";
    }

    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(
            @RequestParam String matKhauCu,
            @RequestParam String matKhauMoi,
            @RequestParam String xacNhanMatKhauMoi,
            RedirectAttributes ra) {

        if (!matKhauMoi.equals(xacNhanMatKhauMoi)) {
            ra.addFlashAttribute("loiThongBao", "Mật khẩu mới không khớp");
            return "redirect:/tai-khoan/thong-tin";
        }
        if (matKhauMoi.length() < 6) {
            ra.addFlashAttribute("loiThongBao", "Mật khẩu mới phải ít nhất 6 ký tự");
            return "redirect:/tai-khoan/thong-tin";
        }

        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        try {
            nguoiDungService.doiMatKhau(nguoiDung.getId(), matKhauCu, matKhauMoi);
            ra.addFlashAttribute("thanhCong", "Đổi mật khẩu thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/tai-khoan/thong-tin";
    }

    // === ĐỊA CHỈ ===

    @GetMapping("/dia-chi")
    public String danhSachDiaChi(Model model) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        List<DiaChiNguoiDung> danhSach = diaChiService.layDanhSachDiaChi(nguoiDung.getId());
        model.addAttribute("danhSachDiaChi", danhSach);
        model.addAttribute("diaMoi", new DiaChiNguoiDung());
        return "khach/tai-khoan/dia-chi";
    }

    @PostMapping("/dia-chi/them")
    public String themDiaChi(
            @ModelAttribute DiaChiNguoiDung diaChi,
            RedirectAttributes ra) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        try {
            diaChiService.themDiaChi(nguoiDung.getId(), diaChi);
            ra.addFlashAttribute("thanhCong", "Thêm địa chỉ thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/tai-khoan/dia-chi";
    }

    @PostMapping("/dia-chi/cap-nhat/{id}")
    public String capNhatDiaChi(
            @PathVariable Long id,
            @ModelAttribute DiaChiNguoiDung diaChi,
            RedirectAttributes ra) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        try {
            diaChiService.capNhatDiaChi(id, diaChi, nguoiDung.getId());
            ra.addFlashAttribute("thanhCong", "Cập nhật địa chỉ thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/tai-khoan/dia-chi";
    }

    @PostMapping("/dia-chi/xoa/{id}")
    public String xoaDiaChi(@PathVariable Long id, RedirectAttributes ra) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        try {
            diaChiService.xoaDiaChi(id, nguoiDung.getId());
            ra.addFlashAttribute("thanhCong", "Đã xóa địa chỉ");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/tai-khoan/dia-chi";
    }

    @PostMapping("/dia-chi/mac-dinh/{id}")
    public String datMacDinh(@PathVariable Long id, RedirectAttributes ra) {
        NguoiDung nguoiDung = nguoiDungHienTai.layBatBuoc();
        try {
            diaChiService.datLamMacDinh(id, nguoiDung.getId());
            ra.addFlashAttribute("thanhCong", "Đã đặt địa chỉ mặc định");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/tai-khoan/dia-chi";
    }
}

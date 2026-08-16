package com.certainshop.controller.quanly;

import com.certainshop.entity.*;
import com.certainshop.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quan-ly/thuoc-tinh")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class ThuocTinhQLController {

    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final KichThuocRepository kichThuocRepository;
    private final MauSacRepository mauSacRepository;
    private final ChatLieuRepository chatLieuRepository;

    // ===== DANH MỤC =====

    @GetMapping({"" ,"/danh-muc"})
    public String danhSachDanhMuc(Model model) {
        loadAllAttributes(model);
        model.addAttribute("tab", "danh-muc");
        return "quan-ly/thuoc-tinh/index";
    }

    @PostMapping("/danh-muc/them")
    public String themDanhMuc(@Valid @ModelAttribute("danhMucMoi") DanhMuc danhMuc,
                               BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("loiThongBao", "Thông tin không hợp lệ");
            return "redirect:/quan-ly/thuoc-tinh/danh-muc";
        }
        try {
            danhMucRepository.save(danhMuc);
            ra.addFlashAttribute("thanhCong", "Thêm danh mục thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/thuoc-tinh/danh-muc";
    }

    @PostMapping("/danh-muc/{id}/sua")
    public String suaDanhMuc(@PathVariable Long id,
                              @RequestParam String tenDanhMuc,
                              @RequestParam String duongDan,
                              @RequestParam(required = false) String moTa,
                              @RequestParam(defaultValue = "false") boolean dangHoatDong,
                              @RequestParam(defaultValue = "0") int thuTuHienThi,
                              RedirectAttributes ra) {
        try {
            DanhMuc dm = danhMucRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            dm.setTenDanhMuc(tenDanhMuc);
            dm.setDuongDan(duongDan);
            dm.setMoTa(moTa);
            dm.setDangHoatDong(dangHoatDong);
            dm.setThuTuHienThi(thuTuHienThi);
            danhMucRepository.save(dm);
            ra.addFlashAttribute("thanhCong", "Cập nhật danh mục thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/thuoc-tinh/danh-muc";
    }

    @PostMapping("/danh-muc/{id}/xoa")
    public String xoaDanhMuc(@PathVariable Long id, RedirectAttributes ra) {
        try {
            danhMucRepository.deleteById(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa danh mục");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", "Không thể xóa, danh mục đang có sản phẩm");
        }
        return "redirect:/quan-ly/thuoc-tinh/danh-muc";
    }

    // ===== THƯƠNG HIỆU =====

    @GetMapping("/thuong-hieu")
    public String danhSachThuongHieu(Model model) {
        loadAllAttributes(model);
        model.addAttribute("tab", "thuong-hieu");
        return "quan-ly/thuoc-tinh/index";
    }

    @PostMapping("/thuong-hieu/them")
    public String themThuongHieu(@RequestParam String tenThuongHieu,
                                  @RequestParam(required = false) String moTa,
                                  RedirectAttributes ra) {
        try {
            ThuongHieu th = new ThuongHieu();
            th.setTenThuongHieu(tenThuongHieu);
            th.setMoTa(moTa);
            th.setTrangThai(true);
            thuongHieuRepository.save(th);
            ra.addFlashAttribute("thanhCong", "Thêm thương hiệu thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/thuoc-tinh/thuong-hieu";
    }

    @PostMapping("/thuong-hieu/{id}/sua")
    public String suaThuongHieu(@PathVariable Long id,
                                 @RequestParam String tenThuongHieu,
                                 @RequestParam(required = false) String moTa,
                                 @RequestParam(defaultValue = "false") boolean trangThai,
                                 RedirectAttributes ra) {
        try {
            ThuongHieu th = thuongHieuRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
            th.setTenThuongHieu(tenThuongHieu);
            th.setMoTa(moTa);
            th.setTrangThai(trangThai);
            thuongHieuRepository.save(th);
            ra.addFlashAttribute("thanhCong", "Cập nhật thương hiệu thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/thuoc-tinh/thuong-hieu";
    }

    @PostMapping("/thuong-hieu/{id}/xoa")
    public String xoaThuongHieu(@PathVariable Long id, RedirectAttributes ra) {
        try {
            thuongHieuRepository.deleteById(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa thương hiệu");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", "Không thể xóa, thương hiệu đang có sản phẩm");
        }
        return "redirect:/quan-ly/thuoc-tinh/thuong-hieu";
    }

    // ===== KÍCH THƯỚC =====

    @GetMapping("/kich-thuoc")
    public String danhSachKichThuoc(Model model) {
        loadAllAttributes(model);
        model.addAttribute("tab", "kich-thuoc");
        return "quan-ly/thuoc-tinh/index";
    }

    @PostMapping("/kich-thuoc/them")
    public String themKichThuoc(@RequestParam String kichCo, RedirectAttributes ra) {
        try {
            KichThuoc kt = new KichThuoc();
            kt.setKichCo(kichCo);
            kichThuocRepository.save(kt);
            ra.addFlashAttribute("thanhCong", "Thêm kích thước thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/thuoc-tinh/kich-thuoc";
    }

    @PostMapping("/kich-thuoc/{id}/xoa")
    public String xoaKichThuoc(@PathVariable Long id, RedirectAttributes ra) {
        try {
            kichThuocRepository.deleteById(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa kích thước");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", "Không thể xóa, kích thước đang được sử dụng");
        }
        return "redirect:/quan-ly/thuoc-tinh/kich-thuoc";
    }

    // ===== MÀU SẮC =====

    @GetMapping("/mau-sac")
    public String danhSachMauSac(Model model) {
        loadAllAttributes(model);
        model.addAttribute("tab", "mau-sac");
        return "quan-ly/thuoc-tinh/index";
    }

    @PostMapping("/mau-sac/them")
    public String themMauSac(@RequestParam String tenMau,
                              @RequestParam(required = false) String maHex,
                              RedirectAttributes ra) {
        try {
            MauSac ms = new MauSac();
            ms.setTenMau(tenMau);
            ms.setMaHex(maHex);
            mauSacRepository.save(ms);
            ra.addFlashAttribute("thanhCong", "Thêm màu sắc thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/thuoc-tinh/mau-sac";
    }

    @PostMapping("/mau-sac/{id}/xoa")
    public String xoaMauSac(@PathVariable Long id, RedirectAttributes ra) {
        try {
            mauSacRepository.deleteById(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa màu sắc");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", "Không thể xóa, màu sắc đang được sử dụng");
        }
        return "redirect:/quan-ly/thuoc-tinh/mau-sac";
    }

    // ===== CHẤT LIỆU =====

    @GetMapping("/chat-lieu")
    public String danhSachChatLieu(Model model) {
        loadAllAttributes(model);
        model.addAttribute("tab", "chat-lieu");
        return "quan-ly/thuoc-tinh/index";
    }

    @PostMapping("/chat-lieu/them")
    public String themChatLieu(@RequestParam String tenChatLieu,
                                @RequestParam(required = false) String moTa,
                                RedirectAttributes ra) {
        try {
            ChatLieu cl = new ChatLieu();
            cl.setTenChatLieu(tenChatLieu);
            cl.setMoTa(moTa);
            chatLieuRepository.save(cl);
            ra.addFlashAttribute("thanhCong", "Thêm chất liệu thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/thuoc-tinh/chat-lieu";
    }

    @PostMapping("/chat-lieu/{id}/xoa")
    public String xoaChatLieu(@PathVariable Long id, RedirectAttributes ra) {
        try {
            chatLieuRepository.deleteById(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa chất liệu");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", "Không thể xóa, chất liệu đang được sử dụng");
        }
        return "redirect:/quan-ly/thuoc-tinh/chat-lieu";
    }

    // ===== HELPER =====

    private void loadAllAttributes(Model model) {
        model.addAttribute("danhSachDanhMuc", danhMucRepository.findAll());
        model.addAttribute("danhMucMoi", new DanhMuc());
        model.addAttribute("danhSachThuongHieu", thuongHieuRepository.findAll());
        model.addAttribute("thuongHieuMoi", new ThuongHieu());
        model.addAttribute("danhSachKichThuoc", kichThuocRepository.findAllByOrderByKichCoAsc());
        model.addAttribute("danhSachMauSac", mauSacRepository.findAllByOrderByTenMauAsc());
        model.addAttribute("danhSachChatLieu", chatLieuRepository.findAllByOrderByTenChatLieuAsc());
        model.addAttribute("chatLieuMoi", new ChatLieu());
    }
}

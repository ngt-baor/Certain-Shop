package com.certainshop.controller.quanly;

import com.certainshop.dto.BienTheDto;
import com.certainshop.dto.SanPhamDto;
import com.certainshop.entity.*;
import com.certainshop.repository.*;
import com.certainshop.service.SanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quan-ly/san-pham")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class SanPhamQLController {

    private final SanPhamService sanPhamService;
    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final KichThuocRepository kichThuocRepository;
    private final MauSacRepository mauSacRepository;
    private final ChatLieuRepository chatLieuRepository;

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;

    @GetMapping
    public String danhSach(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long danhMucId,
            @RequestParam(required = false) Long thuongHieuId,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {

        Page<SanPham> sanPham = sanPhamService.timKiemAdmin(q, danhMucId, thuongHieuId, trangThai,
                PageRequest.of(trang, 10));

        model.addAttribute("sanPham", sanPham);
        model.addAttribute("danhSachDanhMuc", danhMucRepository.findByDangHoatDongTrueOrderByThuTuHienThiAsc());
        model.addAttribute("danhSachThuongHieu", thuongHieuRepository.findByTrangThaiTrueOrderByTenThuongHieuAsc());
        model.addAttribute("q", q);
        model.addAttribute("danhMucId", danhMucId);
        model.addAttribute("thuongHieuId", thuongHieuId);
        model.addAttribute("trangThai", trangThai);
        return "quan-ly/san-pham/danh-sach";
    }

    @GetMapping("/them")
    public String hienThiThem(Model model) {
        model.addAttribute("sanPhamDto", new SanPhamDto());
        model.addAttribute("danhSachDanhMuc", danhMucRepository.findByDangHoatDongTrueOrderByThuTuHienThiAsc());
        model.addAttribute("danhSachThuongHieu", thuongHieuRepository.findByTrangThaiTrueOrderByTenThuongHieuAsc());
        model.addAttribute("danhSachKichThuoc", kichThuocRepository.findAllByOrderByKichCoAsc());
        model.addAttribute("danhSachMauSac", mauSacRepository.findAllByOrderByTenMauAsc());
        model.addAttribute("danhSachChatLieu", chatLieuRepository.findAllByOrderByTenChatLieuAsc());
        return "quan-ly/san-pham/them-moi";
    }

    @PostMapping("/them")
    public String xuLyThem(
            @Valid @ModelAttribute("sanPhamDto") SanPhamDto dto,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("danhSachDanhMuc", danhMucRepository.findByDangHoatDongTrueOrderByThuTuHienThiAsc());
            model.addAttribute("danhSachThuongHieu", thuongHieuRepository.findByTrangThaiTrueOrderByTenThuongHieuAsc());
            model.addAttribute("danhSachKichThuoc", kichThuocRepository.findAllByOrderByKichCoAsc());
            model.addAttribute("danhSachMauSac", mauSacRepository.findAllByOrderByTenMauAsc());
            model.addAttribute("danhSachChatLieu", chatLieuRepository.findAllByOrderByTenChatLieuAsc());
            return "quan-ly/san-pham/them-moi";
        }

        try {
            SanPham sp = sanPhamService.taoSanPham(dto);
            ra.addFlashAttribute("thanhCong", "Tạo sản phẩm thành công: " + sp.getTenSanPham());
            return "redirect:/quan-ly/san-pham/" + sp.getId();
        } catch (Exception e) {
            model.addAttribute("loi", e.getMessage());
            model.addAttribute("danhSachDanhMuc", danhMucRepository.findByDangHoatDongTrueOrderByThuTuHienThiAsc());
            model.addAttribute("danhSachThuongHieu", thuongHieuRepository.findByTrangThaiTrueOrderByTenThuongHieuAsc());
            return "quan-ly/san-pham/them-moi";
        }
    }

    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        SanPham sanPham = sanPhamService.timTheoId(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        model.addAttribute("sanPham", sanPham);
        model.addAttribute("danhSachBienThe", sanPhamService.danhSachBienTheCuaSanPham(id));
        model.addAttribute("danhSachKichThuoc", kichThuocRepository.findAllByOrderByKichCoAsc());
        model.addAttribute("danhSachMauSac", mauSacRepository.findAllByOrderByTenMauAsc());
        model.addAttribute("danhSachChatLieu", chatLieuRepository.findAllByOrderByTenChatLieuAsc());
        model.addAttribute("danhSachDanhMuc", danhMucRepository.findByDangHoatDongTrueOrderByThuTuHienThiAsc());
        model.addAttribute("danhSachThuongHieu", thuongHieuRepository.findByTrangThaiTrueOrderByTenThuongHieuAsc());
        return "quan-ly/san-pham/chi-tiet";
    }

    @PostMapping("/{id}/cap-nhat")
    public String capNhat(
            @PathVariable Long id,
            @Valid @ModelAttribute SanPhamDto dto,
            BindingResult result,
            RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "redirect:/quan-ly/san-pham/" + id;
        }
        try {
            sanPhamService.capNhatSanPham(id, dto);
            ra.addFlashAttribute("thanhCong", "Cập nhật sản phẩm thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/san-pham/" + id;
    }

    @PostMapping("/{id}/xoa")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            sanPhamService.xoaSanPham(id);
            ra.addFlashAttribute("thanhCong", "Đã ẩn sản phẩm thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/san-pham";
    }

    // ===== BIẾN THỂ =====

    @PostMapping("/{sanPhamId}/bien-the/them")
    @ResponseBody
    public ResponseEntity<?> themBienThe(
            @PathVariable Long sanPhamId,
            @RequestBody BienTheDto dto) {
        try {
            BienThe bt = sanPhamService.taoBienThe(sanPhamId, dto);
            return ResponseEntity.ok(Map.of("thanhCong", true, "id", bt.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    @PostMapping("/bien-the/{id}/cap-nhat")
    @ResponseBody
    public ResponseEntity<?> capNhatBienThe(
            @PathVariable Long id,
            @RequestBody BienTheDto dto) {
        try {
            sanPhamService.capNhatBienThe(id, dto);
            return ResponseEntity.ok(Map.of("thanhCong", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    @PostMapping("/bien-the/{id}/xoa")
    @ResponseBody
    public ResponseEntity<?> xoaBienThe(@PathVariable Long id) {
        try {
            sanPhamService.xoaBienThe(id);
            return ResponseEntity.ok(Map.of("thanhCong", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // ===== UPLOAD ẢNH =====

    @PostMapping("/bien-the/{id}/upload-anh")
    @ResponseBody
    public ResponseEntity<?> uploadAnh(
            @PathVariable Long id,
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "false") boolean laAnhChinh) {
        try {
            sanPhamService.uploadAnhBienThe(id, file, laAnhChinh, uploadDir);
            return ResponseEntity.ok(Map.of("thanhCong", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    @PostMapping("/anh/{anhId}/xoa")
    @ResponseBody
    public ResponseEntity<?> xoaAnh(@PathVariable Long anhId) {
        try {
            sanPhamService.xoaAnh(anhId);
            return ResponseEntity.ok(Map.of("thanhCong", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }
}

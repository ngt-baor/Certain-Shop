package com.certainshop.controller.quanly;

import com.certainshop.constant.TrangThaiDonHang;
import com.certainshop.entity.*;
import com.certainshop.repository.*;
import com.certainshop.service.DonHangService;
import com.certainshop.util.NguoiDungHienTai;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quan-ly/don-hang")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class DonHangQLController {

    private final DonHangService donHangService;
    private final DonHangRepository donHangRepository;
    private final NguoiDungHienTai nguoiDungHienTai;

    @GetMapping
    public String danhSach(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String phuongThuc,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {

        Page<DonHang> donHang = donHangService.timKiemDonHangAdmin(q, trangThai, phuongThuc,
                PageRequest.of(trang, 15));

        model.addAttribute("donHang", donHang);
        model.addAttribute("q", q);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("phuongThuc", phuongThuc);
        model.addAttribute("danhSachTrangThai", List.of(
                TrangThaiDonHang.MOI_TAO,
                TrangThaiDonHang.CHO_XAC_NHAN,
                TrangThaiDonHang.DA_XAC_NHAN,
                TrangThaiDonHang.DANG_XU_LY,
                TrangThaiDonHang.DANG_GIAO,
                TrangThaiDonHang.HOAN_TAT,
                TrangThaiDonHang.DA_HUY
        ));
        return "quan-ly/don-hang/danh-sach";
    }

    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        model.addAttribute("donHang", donHang);
        model.addAttribute("lichSu", donHang.getLichSuTrangThai());
        model.addAttribute("trangThaiHienTai", donHang.getTrangThaiDonHang());
        model.addAttribute("trangThaiCoTheChuyenSang", getTrangThaiCoTheChuyenSang(donHang.getTrangThaiDonHang()));
        return "quan-ly/don-hang/chi-tiet";
    }

    private List<String> getTrangThaiCoTheChuyenSang(String trangThaiHienTai) {
        return switch (trangThaiHienTai) {
            case TrangThaiDonHang.CHO_XAC_NHAN -> List.of(TrangThaiDonHang.DA_XAC_NHAN, TrangThaiDonHang.DA_HUY);
            case TrangThaiDonHang.DA_XAC_NHAN -> List.of(TrangThaiDonHang.DANG_XU_LY, TrangThaiDonHang.DA_HUY);
            case TrangThaiDonHang.DANG_XU_LY -> List.of(TrangThaiDonHang.DANG_GIAO);
            case TrangThaiDonHang.DANG_GIAO -> List.of(TrangThaiDonHang.HOAN_TAT);
            default -> List.of();
        };
    }

    @PostMapping("/{id}/chuyen-trang-thai")
    public String chuyenTrangThai(
            @PathVariable Long id,
            @RequestParam String trangThaiMoi,
            @RequestParam(required = false) String ghiChu,
            RedirectAttributes ra) {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            donHangService.chuyenTrangThai(id, trangThaiMoi, ghiChu, nv.getId());
            ra.addFlashAttribute("thanhCong", "Đã chuyển trạng thái đơn hàng thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/don-hang/" + id;
    }

    @PostMapping("/{id}/huy")
    public String huyDon(
            @PathVariable Long id,
            @RequestParam(required = false) String lyDo,
            RedirectAttributes ra) {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            donHangService.nhanVienHuyDon(id, lyDo, nv.getId());
            ra.addFlashAttribute("thanhCong", "Đã hủy đơn hàng thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("loiThongBao", e.getMessage());
        }
        return "redirect:/quan-ly/don-hang/" + id;
    }

    @GetMapping("/{id}/in-hoa-don")
    public String inHoaDon(@PathVariable Long id, Model model) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        model.addAttribute("donHang", donHang);
        return "quan-ly/don-hang/in-hoa-don";
    }
}

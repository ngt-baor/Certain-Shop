package com.certainshop.controller.quanly;

import com.certainshop.constant.TrangThaiDonHang;
import com.certainshop.entity.*;
import com.certainshop.repository.*;
import com.certainshop.service.DonHangService;
import com.certainshop.service.KhuyenMaiService;
import com.certainshop.service.SanPhamService;
import com.certainshop.util.NguoiDungHienTai;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/quan-ly/ban-hang")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class BanHangTaiQuayController {

    private final DonHangService donHangService;
    private final SanPhamService sanPhamService;
    private final KhuyenMaiService khuyenMaiService;
    private final DonHangRepository donHangRepository;
    private final NguoiDungHienTai nguoiDungHienTai;
    private final BienTheRepository bienTheRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;

    @GetMapping
    public String trangBanHang(Model model) {
        NguoiDung nv = nguoiDungHienTai.layBatBuoc();
        List<DonHang> danhSachChoThanhtoan = donHangRepository.findHoaDonCho(nv.getId());

        model.addAttribute("danhSachHoaDonCho", danhSachChoThanhtoan);
        model.addAttribute("nhanVien", nv);
        return "quan-ly/ban-hang/tai-quay";
    }

    @GetMapping("/danh-sach-cho")
    @ResponseBody
    public ResponseEntity<?> layDanhSachHoaDonCho() {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            List<DonHang> danhSach = donHangRepository.findHoaDonCho(nv.getId());
            return ResponseEntity.ok(danhSach);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Tạo hóa đơn chờ mới
    @PostMapping("/tao-hoa-don")
    @ResponseBody
    public ResponseEntity<?> taoHoaDon() {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            DonHang hoaDon = donHangService.taoHoaDonCho(nv.getId());
            Map<String, Object> res = new HashMap<>();
            res.put("thanhCong", true);
            res.put("idHoaDon", hoaDon.getId());
            res.put("maHoaDon", hoaDon.getMaDonHang());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Lấy chi tiết hóa đơn chờ
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> layChiTietHoaDon(@PathVariable Long id) {
        try {
            DonHang hoaDon = donHangRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if (!hoaDon.getTrangThaiDonHang().equals(TrangThaiDonHang.HOA_DON_CHO)) {
                return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", "Hóa đơn không hợp lệ"));
            }

            Map<String, Object> res = new HashMap<>();
            res.put("id", hoaDon.getId());
            res.put("maHoaDon", hoaDon.getMaDonHang());
            res.put("trangThai", hoaDon.getTrangThaiDonHang());
            res.put("tongTien", hoaDon.getTongTienThanhToan());

            List<Map<String, Object>> chiTietList = new ArrayList<>();
            for (ChiTietDonHang ct : hoaDon.getDanhSachChiTiet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", ct.getId());
                item.put("bienTheId", ct.getBienThe().getId());
                item.put("tenSanPham", ct.getBienThe().getSanPham().getTenSanPham());
                item.put("kichThuoc", ct.getBienThe().getKichThuoc() != null ? ct.getBienThe().getKichThuoc().getKichCo() : "");
                item.put("mauSac", ct.getBienThe().getMauSac() != null ? ct.getBienThe().getMauSac().getTenMau() : "");
                item.put("soLuong", ct.getSoLuong());
                item.put("donGia", ct.getGiaTaiThoiDiemMua());
                item.put("thanhTien", ct.getGiaTaiThoiDiemMua().multiply(BigDecimal.valueOf(ct.getSoLuong())));
                String anhUrl = ct.getBienThe().getAnhChinh() != null
                        ? ct.getBienThe().getAnhChinh()
                        : "/img/no-image.png";
                item.put("anhUrl", anhUrl);
                chiTietList.add(item);
            }
            res.put("chiTiet", chiTietList);
            res.put("thanhCong", true);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Tìm kiếm sản phẩm để thêm vào hóa đơn
    @GetMapping("/tim-san-pham")
    @ResponseBody
    public ResponseEntity<?> timSanPham(@RequestParam String q) {
        try {
            List<BienThe> bienTheList = bienTheRepository.timKiemChoQuay(q);
            List<Map<String, Object>> result = new ArrayList<>();
            for (BienThe bt : bienTheList) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", bt.getId());
                item.put("tenSanPham", bt.getSanPham().getTenSanPham());
                item.put("kichThuoc", bt.getKichThuoc() != null ? bt.getKichThuoc().getKichCo() : "");
                item.put("mauSac", bt.getMauSac() != null ? bt.getMauSac().getTenMau() : "");
                item.put("giaBan", bt.getGia());
                item.put("soLuongTon", bt.getSoLuongTon());
                String anhUrl = bt.getAnhChinh() != null
                        ? bt.getAnhChinh()
                        : "/img/no-image.png";
                item.put("anhUrl", anhUrl);
                result.add(item);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Thêm sản phẩm vào hóa đơn chờ
    @PostMapping("/{hoaDonId}/them-san-pham")
    @ResponseBody
    public ResponseEntity<?> themSanPham(
            @PathVariable Long hoaDonId,
            @RequestParam Long bienTheId,
            @RequestParam(defaultValue = "1") int soLuong) {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            donHangService.themSanPhamVaoHoaDonTaiQuay(hoaDonId, bienTheId, soLuong, nv.getId());
            return ResponseEntity.ok(Map.of("thanhCong", true, "thongBao", "Đã thêm sản phẩm vào hóa đơn"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Cập nhật số lượng sản phẩm trong hóa đơn
    @PostMapping("/chi-tiet/{chiTietId}/cap-nhat")
    @ResponseBody
    public ResponseEntity<?> capNhatSoLuong(
            @PathVariable Long chiTietId,
            @RequestParam int soLuong) {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            donHangService.capNhatSoLuongTaiQuay(chiTietId, soLuong, nv.getId());
            return ResponseEntity.ok(Map.of("thanhCong", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Xóa sản phẩm khỏi hóa đơn
    @PostMapping("/chi-tiet/{chiTietId}/xoa")
    @ResponseBody
    public ResponseEntity<?> xoaSanPhamKhoiHoaDon(@PathVariable Long chiTietId) {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            donHangService.xoaChiTietTaiQuay(chiTietId, nv.getId());
            return ResponseEntity.ok(Map.of("thanhCong", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Áp dụng voucher cho hóa đơn tại quầy
    @PostMapping("/{hoaDonId}/ap-voucher")
    @ResponseBody
    public ResponseEntity<?> apVoucher(
            @PathVariable Long hoaDonId,
            @RequestParam String maVoucher) {
        try {
            DonHang hoaDon = donHangRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            KhuyenMai km = khuyenMaiRepository.findByMaKhuyenMai(maVoucher)
                    .orElseThrow(() -> new RuntimeException("Mã voucher không tồn tại"));

            BigDecimal tongTruocGiam = hoaDon.getDanhSachChiTiet().stream()
                    .map(ct -> ct.getGiaTaiThoiDiemMua().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (!km.laHopLe()) {
                return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", "Voucher không hợp lệ hoặc đã hết hạn"));
            }

            BigDecimal soTienGiam = km.tinhSoTienGiam(tongTruocGiam);
            donHangService.apVoucherVaoHoaDonTaiQuay(hoaDonId, km.getId(), soTienGiam);

            return ResponseEntity.ok(Map.of(
                    "thanhCong", true,
                    "soTienGiam", soTienGiam,
                    "tongSauGiam", tongTruocGiam.subtract(soTienGiam),
                    "thongBao", "Áp dụng voucher thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Xóa voucher khỏi hóa đơn
    @PostMapping("/{hoaDonId}/xoa-voucher")
    @ResponseBody
    public ResponseEntity<?> xoaVoucher(@PathVariable Long hoaDonId) {
        try {
            donHangService.xoaVoucherKhoiHoaDonTaiQuay(hoaDonId);
            return ResponseEntity.ok(Map.of("thanhCong", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Thanh toán hóa đơn tại quầy
    @PostMapping("/{hoaDonId}/thanh-toan")
    @ResponseBody
    public ResponseEntity<?> thanhToan(
            @PathVariable Long hoaDonId,
            @RequestParam String phuongThucThanhToan,
            @RequestParam(required = false) BigDecimal tienKhachDua,
            @RequestParam(required = false) String tenKhach,
            @RequestParam(required = false) String sdtKhach) {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            donHangService.thanhToanTaiQuay(hoaDonId, phuongThucThanhToan, tienKhachDua, tenKhach, sdtKhach, nv.getId());
            return ResponseEntity.ok(Map.of("thanhCong", true, "thongBao", "Thanh toán thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    // Hủy hóa đơn chờ
    @PostMapping("/{hoaDonId}/huy")
    @ResponseBody
    public ResponseEntity<?> huyHoaDon(
            @PathVariable Long hoaDonId,
            @RequestParam(required = false) String lyDo) {
        try {
            NguoiDung nv = nguoiDungHienTai.layBatBuoc();
            donHangService.nhanVienHuyDon(hoaDonId, lyDo != null ? lyDo : "Hủy tại quầy", nv.getId());
            return ResponseEntity.ok(Map.of("thanhCong", true, "thongBao", "Đã hủy hóa đơn"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }
}

package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.entity.GioHang;
import com.certainshop.entity.GioHangChiTiet;
import com.certainshop.entity.NguoiDung;
import com.certainshop.repository.NguoiDungRepository;
import com.certainshop.service.GioHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gio-hang")
@RequiredArgsConstructor
public class GioHangApiController {

    private final GioHangService gioHangService;
    private final NguoiDungRepository nguoiDungRepository;

    @GetMapping
    public ResponseEntity<?> layGioHang(Authentication auth) {
        NguoiDung nd = layNguoiDung(auth);
        GioHang gioHang = gioHangService.layHoacTaoGioHang(nd);
        return ResponseEntity.ok(ApiResponse.ok(toGioHangResponse(gioHang)));
    }

    @PostMapping("/them")
    public ResponseEntity<?> themVaoGioHang(
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
            NguoiDung nd = layNguoiDung(auth);
            // Quản lý/Nhân viên không dùng giỏ hàng - họp lệ truy cập qua ban hang tại quầy
            String vaiTro = nd.getVaiTro() != null ? nd.getVaiTro().getTenVaiTro() : "";
            boolean isKhachHang = vaiTro.toLowerCase().contains("khach") || vaiTro.toLowerCase().contains("khách");
            if (!isKhachHang) {
                return ResponseEntity.badRequest().body(ApiResponse.loi(
                        "Quản lý/Nhân viên không sử dụng giỏ hàng."));
            }
            Long bienTheId = Long.valueOf(body.get("bienTheId").toString());
            int soLuong = Integer.parseInt(body.getOrDefault("soLuong", 1).toString());
            gioHangService.themVaoGioHang(nd, bienTheId, soLuong);
            GioHang gioHang = gioHangService.layHoacTaoGioHang(nd);
            return ResponseEntity.ok(ApiResponse.ok("Đã thêm vào giỏ hàng", toGioHangResponse(gioHang)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @PutMapping("/cap-nhat/{chiTietId}")
    public ResponseEntity<?> capNhatSoLuong(
            @PathVariable Long chiTietId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
            NguoiDung nd = layNguoiDung(auth);
            int soLuong = Integer.parseInt(body.get("soLuong").toString());
            gioHangService.capNhatSoLuong(chiTietId, soLuong, nd.getId());
            GioHang gioHang = gioHangService.layHoacTaoGioHang(nd);
            return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật", toGioHangResponse(gioHang)));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.loi("Không có quyền cập nhật mục giỏ hàng này"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @DeleteMapping("/xoa/{chiTietId}")
    public ResponseEntity<?> xoaKhoiGioHang(@PathVariable Long chiTietId, Authentication auth) {
        try {
            NguoiDung nd = layNguoiDung(auth);
            gioHangService.xoaKhoiGio(chiTietId, nd.getId());
            GioHang gioHang = gioHangService.layHoacTaoGioHang(nd);
            return ResponseEntity.ok(ApiResponse.ok("Đã xóa", toGioHangResponse(gioHang)));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.loi("Không có quyền xóa mục giỏ hàng này"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @DeleteMapping("/xoa-het")
    public ResponseEntity<?> xoaHetGioHang(Authentication auth) {
        NguoiDung nd = layNguoiDung(auth);
        GioHang gioHang = gioHangService.layHoacTaoGioHang(nd);
        gioHangService.xoaHetGioHang(gioHang.getId());
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa giỏ hàng"));
    }

    private NguoiDung layNguoiDung(Authentication auth) {
        return nguoiDungRepository.findByTenDangNhap(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    private Map<String, Object> toGioHangResponse(GioHang gioHang) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", gioHang.getId());

        List<Map<String, Object>> items = gioHang.getDanhSachChiTiet() != null ?
                gioHang.getDanhSachChiTiet().stream().map(this::toChiTietResponse).collect(Collectors.toList()) :
                List.of();

        result.put("danhSachChiTiet", items);
        result.put("soLuongSanPham", items.size());

        // Calculate total safely — thanhTien is guaranteed non-null from toChiTietResponse
        BigDecimal tongTien = items.stream()
                .map(i -> {
                    Object tt = i.get("thanhTien");
                    return tt instanceof BigDecimal ? (BigDecimal) tt : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("tongTien", tongTien);

        return result;
    }

    private Map<String, Object> toChiTietResponse(GioHangChiTiet ct) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ct.getId());
        m.put("soLuong", ct.getSoLuong());
        
        // Safely get price with complete fallback chain — never allow null
        BigDecimal donGia = ct.getDonGia();
        if (donGia == null || donGia.signum() == 0) {
            if (ct.getBienThe() != null && ct.getBienThe().getGia() != null) {
                donGia = ct.getBienThe().getGia();
            } else {
                donGia = BigDecimal.ZERO;
            }
        }
        // Final safety: ensure donGia is never null
        if (donGia == null) {
            donGia = BigDecimal.ZERO;
        }
        m.put("donGia", donGia);
        
        // Calculate thanhTien — donGia is guaranteed non-null
        BigDecimal soLuong = BigDecimal.valueOf(ct.getSoLuong() != null ? ct.getSoLuong() : 1);
        BigDecimal thanhTien = donGia.multiply(soLuong);
        m.put("thanhTien", thanhTien);

        if (ct.getBienThe() != null) {
            var bt = ct.getBienThe();
            Map<String, Object> btMap = new LinkedHashMap<>();
            btMap.put("id", bt.getId());
            btMap.put("soLuongTon", bt.getSoLuongTon());
            btMap.put("anhChinh", bt.getAnhChinh());
            if (bt.getSanPham() != null) {
                btMap.put("tenSanPham", bt.getSanPham().getTenSanPham());
                btMap.put("duongDanSanPham", bt.getSanPham().getDuongDan());
            }
            if (bt.getKichThuoc() != null) btMap.put("kichThuoc", bt.getKichThuoc().getKichCo());
            if (bt.getMauSac() != null) {
                btMap.put("tenMauSac", bt.getMauSac().getTenMau());
                btMap.put("maHexMauSac", bt.getMauSac().getMaHex());
            }
            m.put("bienThe", btMap);
        }
        return m;
    }
}

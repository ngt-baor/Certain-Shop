package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.entity.BienThe;
import com.certainshop.entity.DanhMuc;
import com.certainshop.entity.SanPham;
import com.certainshop.entity.ThuongHieu;
import com.certainshop.repository.DanhMucRepository;
import com.certainshop.repository.ThuongHieuRepository;
import com.certainshop.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SanPhamApiController {

    private final SanPhamService sanPhamService;
    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;

    // ===== SẢN PHẨM =====

    @GetMapping("/san-pham")
    public ResponseEntity<?> danhSachSanPham(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) Long danhMucId,
            @RequestParam(required = false) Long thuongHieuId,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "12") int kichThuocTrang) {

        Pageable pageable = PageRequest.of(trang, kichThuocTrang, Sort.by("thoiGianTao").descending());
        Page<SanPham> page = sanPhamService.timKiemChoKhachHang(tuKhoa, danhMucId, thuongHieuId, pageable);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("danhSach", page.getContent().stream().map(this::toSanPhamSummary).collect(Collectors.toList()));
        result.put("tongSoTrang", page.getTotalPages());
        result.put("tongSoBan", page.getTotalElements());
        result.put("trangHienTai", page.getNumber());
        result.put("kichThuocTrang", page.getSize());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/san-pham/{duongDan}")
    public ResponseEntity<?> chiTietSanPham(@PathVariable String duongDan) {
        try {
            return sanPhamService.timTheoDuongDan(duongDan)
                    .filter(sp -> sp.isTrangThai())
                    .map(sp -> {
                        List<BienThe> bienTheList = sanPhamService.danhSachBienTheCuaSanPham(sp.getId());
                        Map<String, Object> detail = toSanPhamDetail(sp, bienTheList);
                        return ResponseEntity.ok(ApiResponse.ok(detail));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.loi("Lỗi tải chi tiết sản phẩm: " + e.getMessage()));
        }
    }

    @GetMapping("/san-pham/id/{id}")
    public ResponseEntity<?> chiTietSanPhamById(@PathVariable Long id) {
        return sanPhamService.timTheoId(id)
                .filter(sp -> sp.isTrangThai())
                .map(sp -> {
                    List<BienThe> bts = sanPhamService.danhSachBienTheCuaSanPham(sp.getId());
                    return ResponseEntity.ok(ApiResponse.ok(toSanPhamDetail(sp, bts)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/san-pham/ban-chay")
    public ResponseEntity<?> sanPhamBanChay(@RequestParam(defaultValue = "8") int soLuong) {
        Pageable pageable = PageRequest.of(0, soLuong);
        List<SanPham> list = sanPhamService.timSanPhamBanChay(pageable);
        return ResponseEntity.ok(ApiResponse.ok(list.stream().map(this::toSanPhamSummary).collect(Collectors.toList())));
    }

    @GetMapping("/san-pham/moi")
    public ResponseEntity<?> sanPhamMoi(@RequestParam(defaultValue = "8") int soLuong) {
        Pageable pageable = PageRequest.of(0, soLuong, Sort.by("thoiGianTao").descending());
        Page<SanPham> page = sanPhamService.timKiemChoKhachHang(null, null, null, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent().stream().map(this::toSanPhamSummary).collect(Collectors.toList())));
    }

    // ===== DANH MỤC =====

    @GetMapping("/danh-muc")
    public ResponseEntity<?> danhSachDanhMuc() {
        List<DanhMuc> list = danhMucRepository.findAllByDangHoatDongTrueOrderByThuTuHienThiAsc();
        List<Map<String, Object>> result = list.stream().map(dm -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", dm.getId());
            m.put("tenDanhMuc", dm.getTenDanhMuc());
            m.put("duongDan", dm.getDuongDan());
            m.put("moTa", dm.getMoTa());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/danh-muc/{duongDan}/san-pham")
    public ResponseEntity<?> sanPhamTheoDanhMuc(
            @PathVariable String duongDan,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "12") int kichThuocTrang) {
        Pageable pageable = PageRequest.of(trang, kichThuocTrang, Sort.by("thoiGianTao").descending());
        Page<SanPham> page = sanPhamService.timTheoSlugDanhMuc(duongDan, pageable);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("danhSach", page.getContent().stream().map(this::toSanPhamSummary).collect(Collectors.toList()));
        result.put("tongSoTrang", page.getTotalPages());
        result.put("tongSoBan", page.getTotalElements());
        result.put("trangHienTai", page.getNumber());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ===== THƯƠNG HIỆU =====

    @GetMapping("/thuong-hieu")
    public ResponseEntity<?> danhSachThuongHieu() {
        List<ThuongHieu> list = thuongHieuRepository.findAllByTrangThaiTrue();
        List<Map<String, Object>> result = list.stream().map(th -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", th.getId());
            m.put("tenThuongHieu", th.getTenThuongHieu());
            m.put("moTa", th.getMoTa());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ===== HELPERS =====

    private Map<String, Object> toSanPhamSummary(SanPham sp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sp.getId());
        m.put("maSanPham", sp.getMaSanPham());
        m.put("tenSanPham", sp.getTenSanPham());
        m.put("duongDan", sp.getDuongDan());
        m.put("giaGoc", sp.getGiaGoc());
        m.put("anhChinh", sp.getAnhChinh());
        m.put("trangThaiSanPham", sp.getTrangThaiSanPham());

        // Giá hiển thị: lấy từ biến thể mặc định hoặc giá sản phẩm
        BigDecimal giaBan = sp.getGiaBan();
        if (sp.getDanhSachBienThe() != null) {
            giaBan = sp.getDanhSachBienThe().stream()
                    .filter(bt -> Boolean.TRUE.equals(bt.getMacDinh()) && Boolean.TRUE.equals(bt.getTrangThai()))
                    .findFirst()
                    .map(bt -> bt.getGia())
                    .orElse(giaBan);
        }
        m.put("giaBan", giaBan);

        if (sp.getDanhMuc() != null) {
            Map<String, Object> dm = new LinkedHashMap<>();
            dm.put("id", sp.getDanhMuc().getId());
            dm.put("tenDanhMuc", sp.getDanhMuc().getTenDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : "");
            dm.put("duongDan", sp.getDanhMuc().getDuongDan() != null ? sp.getDanhMuc().getDuongDan() : "");
            m.put("danhMuc", dm);
        }
        if (sp.getThuongHieu() != null) {
            Map<String, Object> th = new LinkedHashMap<>();
            th.put("id", sp.getThuongHieu().getId());
            th.put("tenThuongHieu", sp.getThuongHieu().getTenThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : "");
            m.put("thuongHieu", th);
        }
        if (sp.getChatLieu() != null) {
            Map<String, Object> cl = new LinkedHashMap<>();
            cl.put("id", sp.getChatLieu().getId());
            cl.put("tenChatLieu", sp.getChatLieu().getTenChatLieu() != null ? sp.getChatLieu().getTenChatLieu() : "");
            cl.put("moTa", sp.getChatLieu().getMoTa() != null ? sp.getChatLieu().getMoTa() : "");
            m.put("chatLieu", cl);
        }
        return m;
    }

    private Map<String, Object> toSanPhamDetail(SanPham sp, List<BienThe> bienTheList) {
        Map<String, Object> m = toSanPhamSummary(sp);
        m.put("moTa", sp.getMoTa());

        List<Map<String, Object>> bienTheMapped = bienTheList.stream()
                .filter(bt -> Boolean.TRUE.equals(bt.getTrangThai()))
                .map(bt -> {
                    Map<String, Object> btMap = new LinkedHashMap<>();
                    btMap.put("id", bt.getId());
                    btMap.put("gia", bt.getGia());
                    btMap.put("soLuongTon", bt.getSoLuongTon());
                    btMap.put("macDinh", bt.getMacDinh());
                    if (bt.getKichThuoc() != null) {
                        Map<String, Object> kt = new LinkedHashMap<>();
                        kt.put("id", bt.getKichThuoc().getId());
                        kt.put("tenKichThuoc", bt.getKichThuoc().getKichCo() != null ? bt.getKichThuoc().getKichCo() : "");
                        btMap.put("kichThuoc", kt);
                    }
                    if (bt.getMauSac() != null) {
                        Map<String, Object> ms = new LinkedHashMap<>();
                        ms.put("id", bt.getMauSac().getId());
                        ms.put("tenMauSac", bt.getMauSac().getTenMau() != null ? bt.getMauSac().getTenMau() : "");
                        ms.put("maHex", bt.getMauSac().getMaHex() != null ? bt.getMauSac().getMaHex() : "#cccccc");
                        btMap.put("mauSac", ms);
                    }
                    if (bt.getChatLieu() != null) {
                        Map<String, Object> cl = new LinkedHashMap<>();
                        cl.put("id", bt.getChatLieu().getId());
                        cl.put("tenChatLieu", bt.getChatLieu().getTenChatLieu() != null ? bt.getChatLieu().getTenChatLieu() : "");
                        btMap.put("chatLieu", cl);
                    }
                    // Ảnh
                    if (bt.getDanhSachHinhAnh() != null && !bt.getDanhSachHinhAnh().isEmpty()) {
                        btMap.put("hinhAnh", bt.getDanhSachHinhAnh().stream().map(ha -> {
                            Map<String, Object> haMap = new LinkedHashMap<>();
                            haMap.put("id", ha.getId());
                            haMap.put("duongDan", ha.getDuongDan() != null ? ha.getDuongDan() : "");
                            haMap.put("laAnhChinh", Boolean.TRUE.equals(ha.getLaAnhChinh()));
                            return haMap;
                        }).collect(Collectors.toList()));
                    }
                    return btMap;
                }).collect(Collectors.toList());

        m.put("bienThe", bienTheMapped);
        return m;
    }
}

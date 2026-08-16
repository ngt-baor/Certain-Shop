package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.entity.DiaChiNguoiDung;
import com.certainshop.entity.NguoiDung;
import com.certainshop.service.DiaChiService;
import com.certainshop.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tai-khoan")
@RequiredArgsConstructor
public class TaiKhoanApiController {

    private final NguoiDungService nguoiDungService;
    private final DiaChiService diaChiService;

    private NguoiDung getNguoiDung(UserDetails userDetails) {
        return nguoiDungService.timTheoTenDangNhap(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
    }

    // ======================== THÔNG TIN TÀI KHOẢN ========================

    @GetMapping("/thong-tin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layThongTin(
            @AuthenticationPrincipal UserDetails userDetails) {
        NguoiDung nd = getNguoiDung(userDetails);
        Map<String, Object> data = Map.of(
                "id", nd.getId(),
                "tenDangNhap", nd.getTenDangNhap(),
                "email", nd.getEmail() != null ? nd.getEmail() : "",
                "hoTen", nd.getHoTen() != null ? nd.getHoTen() : "",
                "soDienThoai", nd.getSoDienThoai() != null ? nd.getSoDienThoai() : "",
                "ngaySinh", nd.getNgaySinh() != null ? nd.getNgaySinh().toString() : "",
                "gioiTinh", nd.getGioiTinh() != null ? nd.getGioiTinh() : "",
                "anhDaiDien", nd.getAnhDaiDien() != null ? nd.getAnhDaiDien() : "",
                "vaiTro", nd.getVaiTro() != null ? nd.getVaiTro().getTenVaiTro() : ""
        );
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PutMapping("/thong-tin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capNhatThongTin(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        NguoiDung nd = getNguoiDung(userDetails);

        NguoiDung thongTinMoi = new NguoiDung();
        thongTinMoi.setHoTen((String) body.get("hoTen"));
        thongTinMoi.setSoDienThoai((String) body.get("soDienThoai"));
        thongTinMoi.setEmail((String) body.get("email"));

        Object gioiTinhObj = body.get("gioiTinh");
        if (gioiTinhObj instanceof Boolean b) {
            thongTinMoi.setGioiTinh(b);
        }

        Object ngaySinhObj = body.get("ngaySinh");
        if (ngaySinhObj instanceof String s && !s.isBlank()) {
            try {
                thongTinMoi.setNgaySinh(LocalDate.parse(s));
            } catch (Exception ignored) {}
        }

        NguoiDung saved = nguoiDungService.capNhatThongTin(nd.getId(), thongTinMoi);

        Map<String, Object> data = Map.of(
                "id", saved.getId(),
                "hoTen", saved.getHoTen() != null ? saved.getHoTen() : "",
                "email", saved.getEmail() != null ? saved.getEmail() : "",
                "soDienThoai", saved.getSoDienThoai() != null ? saved.getSoDienThoai() : ""
        );
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", data));
    }

    @PostMapping("/doi-mat-khau")
    public ResponseEntity<ApiResponse<Void>> doiMatKhau(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        NguoiDung nd = getNguoiDung(userDetails);
        String matKhauCu = body.get("matKhauCu");
        String matKhauMoi = body.get("matKhauMoi");

        if (matKhauCu == null || matKhauMoi == null || matKhauMoi.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.loi("Mật khẩu mới phải có ít nhất 6 ký tự"));
        }

        try {
            nguoiDungService.doiMatKhau(nd.getId(), matKhauCu, matKhauMoi);
            return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    // ======================== ĐỊA CHỈ ========================

    @GetMapping("/dia-chi")
    public ResponseEntity<ApiResponse<List<DiaChiNguoiDung>>> layDanhSachDiaChi(
            @AuthenticationPrincipal UserDetails userDetails) {
        NguoiDung nd = getNguoiDung(userDetails);
        List<DiaChiNguoiDung> danhSach = diaChiService.layDanhSachDiaChi(nd.getId());
        return ResponseEntity.ok(ApiResponse.ok(danhSach));
    }

    @PostMapping("/dia-chi")
    public ResponseEntity<ApiResponse<DiaChiNguoiDung>> themDiaChi(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DiaChiNguoiDung diaChi) {
        NguoiDung nd = getNguoiDung(userDetails);
        try {
            DiaChiNguoiDung saved = diaChiService.themDiaChi(nd.getId(), diaChi);
            return ResponseEntity.ok(ApiResponse.ok("Thêm địa chỉ thành công", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @PutMapping("/dia-chi/{id}")
    public ResponseEntity<ApiResponse<DiaChiNguoiDung>> capNhatDiaChi(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody DiaChiNguoiDung diaChi) {
        NguoiDung nd = getNguoiDung(userDetails);
        try {
            DiaChiNguoiDung saved = diaChiService.capNhatDiaChi(id, diaChi, nd.getId());
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật địa chỉ thành công", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @DeleteMapping("/dia-chi/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaDiaChi(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        NguoiDung nd = getNguoiDung(userDetails);
        try {
            diaChiService.xoaDiaChi(id, nd.getId());
            return ResponseEntity.ok(ApiResponse.ok("Xóa địa chỉ thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @PutMapping("/dia-chi/{id}/mac-dinh")
    public ResponseEntity<ApiResponse<Void>> datLamMacDinh(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        NguoiDung nd = getNguoiDung(userDetails);
        try {
            diaChiService.datLamMacDinh(id, nd.getId());
            return ResponseEntity.ok(ApiResponse.ok("Đã đặt làm địa chỉ mặc định", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }
}

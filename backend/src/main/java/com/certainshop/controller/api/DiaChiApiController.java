package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.dto.DiaChiChiTietDto;
import com.certainshop.entity.NguoiDung;
import com.certainshop.service.DiaChiService;
import com.certainshop.service.GHNApiService;
import com.certainshop.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dia-chi")
@RequiredArgsConstructor
@Slf4j
public class DiaChiApiController {

    private final GHNApiService ghnApiService;
    private final DiaChiService diaChiService;
    private final NguoiDungService nguoiDungService;

    private NguoiDung getNguoiDung(UserDetails userDetails) {
        return nguoiDungService.timTheoTenDangNhap(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
    }

    // ===== API GHN =====

    @GetMapping("/tinh-thanh")
    public ResponseEntity<?> layDanhSachTinhThanh() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách tỉnh/thành thành công", 
                    ghnApiService.layDanhSachTinh()));
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách tỉnh: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể lấy danh sách tỉnh: " + e.getMessage()));
        }
    }

    @GetMapping("/quan-huyen")
    public ResponseEntity<?> layDanhSachQuanHuyen(@RequestParam("maTinh") int maTinh) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách quận/huyện thành công",
                    ghnApiService.layDanhSachHuyen(maTinh)));
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách huyện: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể lấy danh sách huyện: " + e.getMessage()));
        }
    }

    @GetMapping("/phuong-xa")
    public ResponseEntity<?> layDanhSachPhuongXa(@RequestParam("maHuyen") int maHuyen) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách phường/xã thành công",
                    ghnApiService.layDanhSachXa(maHuyen)));
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách xã: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể lấy danh sách xã: " + e.getMessage()));
        }
    }

    @PostMapping("/tinh-phi-ship")
    public ResponseEntity<?> tinhPhiVanChuyen(
            @RequestParam("maHuyen") int maHuyen,
            @RequestParam("maXa") String maXa,
            @RequestParam(value = "trongLuong", defaultValue = "500") int trongLuong) {
        try {
            BigDecimal phi = ghnApiService.tinhPhiVanChuyen(maHuyen, maXa, trongLuong);
            return ResponseEntity.ok(ApiResponse.ok("Tính phí vận chuyển thành công",
                    Map.of("phiVanChuyen", phi)));
        } catch (Exception e) {
            log.error("Lỗi tính phí vận chuyển: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể tính phí vận chuyển: " + e.getMessage()));
        }
    }

    // ===== Quản lý địa chỉ =====

    /**
     * Lấy danh sách địa chỉ của người dùng hiện tại
     */
    @GetMapping("/user-addresses")
    public ResponseEntity<?> layDanhSachDiaChiCuaNguoiDung(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            NguoiDung nguoiDung = getNguoiDung(userDetails);
            List<DiaChiChiTietDto> danhSach = diaChiService.layDanhSachDiaChiDto(nguoiDung);
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách địa chỉ thành công", danhSach));
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách địa chỉ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể lấy danh sách địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết một địa chỉ
     */
    @GetMapping("/{diaChiId}")
    public ResponseEntity<?> layChiTietDiaChi(
            @PathVariable Long diaChiId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            NguoiDung nguoiDung = getNguoiDung(userDetails);
            Optional<DiaChiChiTietDto> diaChi = diaChiService.layDiaChiChiTietDto(diaChiId, nguoiDung);
            if (diaChi.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết địa chỉ thành công", diaChi.get()));
        } catch (Exception e) {
            log.error("Lỗi lấy chi tiết địa chỉ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể lấy chi tiết địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Tạo địa chỉ mới
     */
    @PostMapping
    public ResponseEntity<?> taoDiaChi(
            @RequestBody DiaChiChiTietDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            NguoiDung nguoiDung = getNguoiDung(userDetails);
            DiaChiChiTietDto created = diaChiService.taoDiaChiDto(nguoiDung, dto);
            return ResponseEntity.ok(ApiResponse.ok("Tạo địa chỉ thành công", created));
        } catch (Exception e) {
            log.error("Lỗi tạo địa chỉ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể tạo địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật địa chỉ
     */
    @PutMapping("/{diaChiId}")
    public ResponseEntity<?> capNhatDiaChi(
            @PathVariable Long diaChiId,
            @RequestBody DiaChiChiTietDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            NguoiDung nguoiDung = getNguoiDung(userDetails);
            DiaChiChiTietDto updated = diaChiService.capNhatDiaChiDto(diaChiId, nguoiDung, dto);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật địa chỉ thành công", updated));
        } catch (Exception e) {
            log.error("Lỗi cập nhật địa chỉ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể cập nhật địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Xóa địa chỉ
     */
    @DeleteMapping("/{diaChiId}")
    public ResponseEntity<?> xoaDiaChi(
            @PathVariable Long diaChiId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            NguoiDung nguoiDung = getNguoiDung(userDetails);
            diaChiService.xoaDiaChiDto(diaChiId, nguoiDung);
            return ResponseEntity.ok(ApiResponse.ok("Xóa địa chỉ thành công", null));
        } catch (Exception e) {
            log.error("Lỗi xóa địa chỉ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể xóa địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Đặt địa chỉ làm mặc định
     */
    @PostMapping("/{diaChiId}/set-default")
    public ResponseEntity<?> datLamMacDinh(
            @PathVariable Long diaChiId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            NguoiDung nguoiDung = getNguoiDung(userDetails);
            diaChiService.datDiaChiMacDinhDto(diaChiId, nguoiDung);
            return ResponseEntity.ok(ApiResponse.ok("Đặt địa chỉ mặc định thành công", null));
        } catch (Exception e) {
            log.error("Lỗi đặt địa chỉ mặc định: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.loi("Không thể đặt địa chỉ mặc định: " + e.getMessage()));
        }
    }
}

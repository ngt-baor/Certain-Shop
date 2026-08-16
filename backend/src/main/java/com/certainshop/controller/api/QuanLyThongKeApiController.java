package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quan-ly/thong-ke")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','NHAN_VIEN')")
public class QuanLyThongKeApiController {

    private final ThongKeService thongKeService;

    @GetMapping("/tong-quan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> tongQuan() {
        try {
            Map<String, Object> data = thongKeService.layThongKeTongQuan();
            return ResponseEntity.ok(ApiResponse.ok(data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @GetMapping("/doanh-thu")
    public ResponseEntity<ApiResponse<Map<String, Object>>> doanhThu(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime denNgay) {
        try {
            List<Object[]> chiTiet = thongKeService.thongKeDoanhThuTheoNgay(tuNgay, denNgay);
            BigDecimal tongDoanhThu = thongKeService.tinhTongDoanhThu(tuNgay, denNgay);
            Map<String, Object> data = Map.of(
                    "chiTiet", chiTiet,
                    "tongDoanhThu", tongDoanhThu
            );
            return ResponseEntity.ok(ApiResponse.ok(data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @GetMapping("/san-pham-ban-chay")
    public ResponseEntity<ApiResponse<List<Object[]>>> sanPhamBanChay() {
        return ResponseEntity.ok(ApiResponse.ok(thongKeService.sanPhamBanChay()));
    }

    @GetMapping("/san-pham-sap-het-hang")
    public ResponseEntity<ApiResponse<?>> sanPhamSapHetHang() {
        return ResponseEntity.ok(ApiResponse.ok(thongKeService.sanPhamSapHetHang()));
    }

    @GetMapping("/san-pham-het-hang")
    public ResponseEntity<ApiResponse<?>> sanPhamHetHang() {
        return ResponseEntity.ok(ApiResponse.ok(thongKeService.sanPhamHetHang()));
    }
}

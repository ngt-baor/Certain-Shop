package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.entity.Voucher;
import com.certainshop.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherApiController {

    private final VoucherService voucherService;

    /**
     * Danh sách voucher hoạt động (public - for customers to see active vouchers only)
     */
    @GetMapping("/hoat-dong")
    public ResponseEntity<ApiResponse<List<Voucher>>> danhSachVoucher() {
        List<Voucher> vouchers = voucherService.danhSachVoucherHoatDong();
        return ResponseEntity.ok(ApiResponse.ok("Danh sách voucher", vouchers));
    }

    /**
     * Admin: Danh sách TẤT CẢ vouchers (bao gồm hết hạn, inactive)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Voucher>>> danhSachTatCa() {
        List<Voucher> vouchers = voucherService.danhSachTatCaVoucher();
        return ResponseEntity.ok(ApiResponse.ok("Danh sách tất cả vouchers", vouchers));
    }

    /**
     * Admin: Tạo voucher mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Voucher>> taoVoucher(@RequestBody Voucher voucher) {
        try {
            Voucher created = voucherService.taoVoucher(voucher);
            return ResponseEntity.ok(ApiResponse.ok("Tạo voucher thành công", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    /**
     * Admin: Cập nhật voucher
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Voucher>> capNhatVoucher(
            @PathVariable Long id, @RequestBody Voucher updates) {
        try {
            Voucher updated = voucherService.capNhatVoucher(id, updates);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật voucher thành công", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    /**
     * Admin: Xóa voucher
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> xoaVoucher(@PathVariable Long id) {
        try {
            voucherService.xoaVoucher(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa voucher thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    /**
     * Tính giá trị giảm cho voucher (validate & calculate)
     * @param maVoucher Mã voucher
     * @param giaTriDonHang Giá trị đơn hàng
     */
    @GetMapping("/tinh-giam")
    public ResponseEntity<ApiResponse<Map<String, Object>>> tinhGiaTriGiam(
            @RequestParam String maVoucher,
            @RequestParam BigDecimal giaTriDonHang) {
        BigDecimal giaTriGiam = voucherService.tinhGiaTriGiam(maVoucher, giaTriDonHang);
        
        Map<String, Object> result = new HashMap<>();
        result.put("maVoucher", maVoucher);
        result.put("giaTriGiam", giaTriGiam);
        result.put("giaTriSauGiam", giaTriDonHang.subtract(giaTriGiam));
        result.put("hopLe", giaTriGiam.compareTo(BigDecimal.ZERO) > 0);
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}

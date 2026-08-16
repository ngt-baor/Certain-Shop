package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.service.GHNApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ghn")
@RequiredArgsConstructor
public class GHNApiController {

    private final GHNApiService ghnService;

    /**
     * Tính phí vận chuyển GHN - Public endpoint
     * Params: maHuyenNhan, maXaNhan, weight
     */
    @PostMapping("/fee")
    public ResponseEntity<?> tinhPhi(
            @RequestParam Integer maHuyenNhan,
            @RequestParam String maXaNhan,
            @RequestParam(defaultValue = "0") Integer weight) {
        try {

            BigDecimal fee = ghnService.tinhPhiVanChuyen(maHuyenNhan, maXaNhan, weight);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fee", fee);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.loi("Lỗi khi tính phí: " + e.getMessage()));
        }
    }
}

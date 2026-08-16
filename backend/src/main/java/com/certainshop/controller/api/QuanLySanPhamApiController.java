package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.dto.BienTheDto;
import com.certainshop.dto.SanPhamDto;
import com.certainshop.entity.BienThe;
import com.certainshop.entity.SanPham;
import com.certainshop.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quan-ly/san-pham")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','NHAN_VIEN')")
public class QuanLySanPhamApiController {

    private final SanPhamService sanPhamService;

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> danhSach(
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(required = false) Long danhMucId,
            @RequestParam(required = false) Long thuongHieuId,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "20") int kichThuocTrang) {
        Pageable pageable = PageRequest.of(trang, kichThuocTrang, Sort.by("thoiGianTao").descending());
        Page<SanPham> page = sanPhamService.timKiemAdmin(
                tuKhoa.isEmpty() ? null : tuKhoa, danhMucId, thuongHieuId, trangThai, pageable);
        Map<String, Object> result = Map.of(
                "sanPham", page.getContent(),
                "tongSo", page.getTotalElements(),
                "tongTrang", page.getTotalPages(),
                "trang", trang
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chiTiet(@PathVariable Long id) {
        return sanPhamService.timTheoId(id)
                .map(sp -> {
                    List<BienThe> bienThe = sanPhamService.danhSachBienTheCuaSanPham(id);
                    Map<String, Object> data = Map.of("sanPham", sp, "bienThe", bienThe);
                    return ResponseEntity.ok(ApiResponse.ok(data));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SanPham>> taoMoi(@RequestBody SanPhamDto dto) {
        try {
            SanPham sp = sanPhamService.taoSanPham(dto);
            return ResponseEntity.ok(ApiResponse.ok("Tạo sản phẩm thành công", sp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SanPham>> capNhat(
            @PathVariable Long id, @RequestBody SanPhamDto dto) {
        try {
            SanPham sp = sanPhamService.capNhatSanPham(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", sp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoa(@PathVariable Long id) {
        try {
            sanPhamService.xoaSanPham(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    // ======================== BIẾN THỂ ========================

    @PostMapping("/{sanPhamId}/bien-the")
    public ResponseEntity<ApiResponse<BienThe>> taoBienThe(
            @PathVariable Long sanPhamId, @RequestBody BienTheDto dto) {
        try {
            BienThe bt = sanPhamService.taoBienThe(sanPhamId, dto);
            return ResponseEntity.ok(ApiResponse.ok("Tạo biến thể thành công", bt));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @PostMapping("/{sanPhamId}/bien-the/bulk")
    public ResponseEntity<ApiResponse<List<BienThe>>> taoBulkBienThe(
            @PathVariable Long sanPhamId, @RequestBody List<BienTheDto> danhSachBienThe) {
        try {
            if (danhSachBienThe == null || danhSachBienThe.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.loi("Danh sách biến thể không được trống"));
            }
            List<BienThe> result = sanPhamService.taoBulkBienThe(sanPhamId, danhSachBienThe);
            return ResponseEntity.ok(ApiResponse.ok(
                    String.format("Tạo %d biến thể thành công", result.size()), result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @PutMapping("/bien-the/{bienTheId}")
    public ResponseEntity<ApiResponse<BienThe>> capNhatBienThe(
            @PathVariable Long bienTheId, @RequestBody BienTheDto dto) {
        try {
            BienThe bt = sanPhamService.capNhatBienThe(bienTheId, dto);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật biến thể thành công", bt));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @DeleteMapping("/bien-the/{bienTheId}")
    public ResponseEntity<ApiResponse<Void>> xoaBienThe(@PathVariable Long bienTheId) {
        try {
            sanPhamService.xoaBienThe(bienTheId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa biến thể thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    // ======================== ẢNH ========================

    @PostMapping("/bien-the/{bienTheId}/upload-anh")
    public ResponseEntity<?> uploadAnh(
            @PathVariable Long bienTheId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean laAnhChinh) {
        try {
            var hinhAnh = sanPhamService.uploadAnhBienThe(bienTheId, file, laAnhChinh, uploadDir);
            return ResponseEntity.ok(ApiResponse.ok("Upload ảnh thành công",
                    Map.of("id", hinhAnh.getId(), "duongDan", hinhAnh.getDuongDan(), "laAnhChinh", hinhAnh.getLaAnhChinh())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @DeleteMapping("/anh/{anhId}")
    public ResponseEntity<ApiResponse<Void>> xoaAnh(@PathVariable Long anhId) {
        try {
            sanPhamService.xoaAnh(anhId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa ảnh thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }
}

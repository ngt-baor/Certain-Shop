package com.certainshop.controller.api;

import com.certainshop.dto.ApiResponse;
import com.certainshop.entity.*;
import com.certainshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quan-ly/thuoc-tinh")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','NHAN_VIEN')")
public class QuanLyThuocTinhApiController {

    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final ChatLieuRepository chatLieuRepository;
    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;

    // ======================== MÀU SẮC ========================

    @GetMapping("/mau-sac")
    public ResponseEntity<ApiResponse<List<MauSac>>> danhSachMauSac() {
        return ResponseEntity.ok(ApiResponse.ok(mauSacRepository.findAll()));
    }

    @PostMapping("/mau-sac")
    public ResponseEntity<ApiResponse<MauSac>> themMauSac(@RequestBody Map<String, String> body) {
        MauSac mauSac = new MauSac();
        mauSac.setTenMau(body.get("tenMau"));
        mauSac.setMaHex(body.get("maHex"));
        mauSac.setMoTa(body.get("moTa"));
        return ResponseEntity.ok(ApiResponse.ok("Thêm màu sắc thành công", mauSacRepository.save(mauSac)));
    }

    @PutMapping("/mau-sac/{id}")
    public ResponseEntity<ApiResponse<MauSac>> capNhatMauSac(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return mauSacRepository.findById(id).map(ms -> {
            if (body.get("tenMau") != null) ms.setTenMau(body.get("tenMau"));
            if (body.get("maHex") != null) ms.setMaHex(body.get("maHex"));
            if (body.get("moTa") != null) ms.setMoTa(body.get("moTa"));
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", mauSacRepository.save(ms)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/mau-sac/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaMauSac(@PathVariable Long id) {
        try {
            mauSacRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi("Không thể xóa: " + e.getMessage()));
        }
    }

    // ======================== KÍCH THƯỚC ========================

    @GetMapping("/kich-thuoc")
    public ResponseEntity<ApiResponse<List<KichThuoc>>> danhSachKichThuoc() {
        return ResponseEntity.ok(ApiResponse.ok(kichThuocRepository.findAll()));
    }

    @PostMapping("/kich-thuoc")
    public ResponseEntity<ApiResponse<KichThuoc>> themKichThuoc(@RequestBody Map<String, Object> body) {
        KichThuoc kt = new KichThuoc();
        kt.setKichCo((String) body.get("kichCo"));
        if (body.get("thuTu") instanceof Integer i) kt.setThuTu(i);
        return ResponseEntity.ok(ApiResponse.ok("Thêm kích thước thành công", kichThuocRepository.save(kt)));
    }

    @PutMapping("/kich-thuoc/{id}")
    public ResponseEntity<ApiResponse<KichThuoc>> capNhatKichThuoc(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return kichThuocRepository.findById(id).map(kt -> {
            if (body.get("kichCo") != null) kt.setKichCo((String) body.get("kichCo"));
            if (body.get("thuTu") instanceof Integer i) kt.setThuTu(i);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", kichThuocRepository.save(kt)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/kich-thuoc/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaKichThuoc(@PathVariable Long id) {
        try {
            kichThuocRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi("Không thể xóa: " + e.getMessage()));
        }
    }

    // ======================== CHẤT LIỆU ========================

    @GetMapping("/chat-lieu")
    public ResponseEntity<ApiResponse<List<ChatLieu>>> danhSachChatLieu() {
        return ResponseEntity.ok(ApiResponse.ok(chatLieuRepository.findAll()));
    }

    @PostMapping("/chat-lieu")
    public ResponseEntity<ApiResponse<ChatLieu>> themChatLieu(@RequestBody Map<String, String> body) {
        ChatLieu cl = new ChatLieu();
        cl.setTenChatLieu(body.get("tenChatLieu"));
        cl.setMoTa(body.get("moTa"));
        return ResponseEntity.ok(ApiResponse.ok("Thêm chất liệu thành công", chatLieuRepository.save(cl)));
    }

    @PutMapping("/chat-lieu/{id}")
    public ResponseEntity<ApiResponse<ChatLieu>> capNhatChatLieu(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return chatLieuRepository.findById(id).map(cl -> {
            if (body.get("tenChatLieu") != null) cl.setTenChatLieu(body.get("tenChatLieu"));
            if (body.get("moTa") != null) cl.setMoTa(body.get("moTa"));
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", chatLieuRepository.save(cl)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chat-lieu/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaChatLieu(@PathVariable Long id) {
        try {
            chatLieuRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi("Không thể xóa: " + e.getMessage()));
        }
    }

    // ======================== DANH MỤC ========================

    @GetMapping("/danh-muc")
    public ResponseEntity<ApiResponse<List<DanhMuc>>> danhSachDanhMuc() {
        return ResponseEntity.ok(ApiResponse.ok(danhMucRepository.findAll()));
    }

    @PostMapping("/danh-muc")
    public ResponseEntity<ApiResponse<DanhMuc>> themDanhMuc(@RequestBody DanhMuc danhMuc) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Thêm danh mục thành công", danhMucRepository.save(danhMuc)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @PutMapping("/danh-muc/{id}")
    public ResponseEntity<ApiResponse<DanhMuc>> capNhatDanhMuc(
            @PathVariable Long id, @RequestBody DanhMuc body) {
        return danhMucRepository.findById(id).map(dm -> {
            if (body.getTenDanhMuc() != null) dm.setTenDanhMuc(body.getTenDanhMuc());
            if (body.getDuongDan() != null) dm.setDuongDan(body.getDuongDan());
            if (body.getMoTa() != null) dm.setMoTa(body.getMoTa());
            if (body.getThuTuHienThi() != null) dm.setThuTuHienThi(body.getThuTuHienThi());
            if (body.getDangHoatDong() != null) dm.setDangHoatDong(body.getDangHoatDong());
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", danhMucRepository.save(dm)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/danh-muc/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaDanhMuc(@PathVariable Long id) {
        try {
            danhMucRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi("Không thể xóa: " + e.getMessage()));
        }
    }

    // ======================== THƯƠNG HIỆU ========================

    @GetMapping("/thuong-hieu")
    public ResponseEntity<ApiResponse<List<ThuongHieu>>> danhSachThuongHieu() {
        return ResponseEntity.ok(ApiResponse.ok(thuongHieuRepository.findAll()));
    }

    @PostMapping("/thuong-hieu")
    public ResponseEntity<ApiResponse<ThuongHieu>> themThuongHieu(@RequestBody ThuongHieu body) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Thêm thương hiệu thành công", thuongHieuRepository.save(body)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi(e.getMessage()));
        }
    }

    @PutMapping("/thuong-hieu/{id}")
    public ResponseEntity<ApiResponse<ThuongHieu>> capNhatThuongHieu(
            @PathVariable Long id, @RequestBody ThuongHieu body) {
        return thuongHieuRepository.findById(id).map(th -> {
            if (body.getTenThuongHieu() != null) th.setTenThuongHieu(body.getTenThuongHieu());
            if (body.getMoTa() != null) th.setMoTa(body.getMoTa());
            if (body.getTrangThai() != null) th.setTrangThai(body.getTrangThai());
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", thuongHieuRepository.save(th)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/thuong-hieu/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaThuongHieu(@PathVariable Long id) {
        try {
            thuongHieuRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.loi("Không thể xóa: " + e.getMessage()));
        }
    }
}

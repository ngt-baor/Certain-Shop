package com.certainshop.repository;

import com.certainshop.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {

    // Tìm kiếm và lọc sản phẩm (chỉ đang bán)
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThaiSanPham = 'DANG_BAN' " +
           "AND (:tuKhoa IS NULL OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "AND (:danhMucId IS NULL OR sp.danhMuc.id = :danhMucId) " +
           "AND (:thuongHieuId IS NULL OR sp.thuongHieu.id = :thuongHieuId)")
    Page<SanPham> timKiemVaLoc(
            @Param("tuKhoa") String tuKhoa,
            @Param("danhMucId") Long danhMucId,
            @Param("thuongHieuId") Long thuongHieuId,
            Pageable pageable);

    // Tất cả (bao gồm đã ẩn) - cho admin, lọc những sản phẩm chưa bị xóa (trangThai = true)
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai = true " +
           "AND (:tuKhoa IS NULL OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "AND (:danhMucId IS NULL OR sp.danhMuc.id = :danhMucId) " +
           "AND (:thuongHieuId IS NULL OR sp.thuongHieu.id = :thuongHieuId) " +
           "AND (:trangThai IS NULL OR sp.trangThaiSanPham = :trangThai)")
    Page<SanPham> timKiemAdmin(
            @Param("tuKhoa") String tuKhoa,
            @Param("danhMucId") Long danhMucId,
            @Param("thuongHieuId") Long thuongHieuId,
            @Param("trangThai") String trangThai,
            Pageable pageable);

    // Sản phẩm theo danh mục
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThaiSanPham = 'DANG_BAN' AND sp.danhMuc.duongDan = :duongDan")
    Page<SanPham> findByDanhMucDuongDan(@Param("duongDan") String duongDan, Pageable pageable);

    // Sản phẩm bán chạy
    @Query("SELECT sp FROM SanPham sp " +
           "JOIN sp.danhSachBienThe bt " +
           "JOIN ChiTietDonHang ctdh ON ctdh.bienThe = bt " +
           "JOIN ctdh.donHang dh " +
           "WHERE dh.trangThaiDonHang = 'HOAN_TAT' " +
           "AND dh.thoiGianTao >= :tuNgay " +
           "GROUP BY sp.id, sp.maSanPham, sp.tenSanPham, sp.moTa, sp.giaGoc, sp.giaBan, sp.danhMuc, sp.thuongHieu, sp.anhChinh, sp.duongDan, sp.trangThaiSanPham, sp.thoiGianTao, sp.thoiGianCapNhat " +
           "ORDER BY SUM(ctdh.soLuong) DESC")
    List<SanPham> findSanPhamBanChay(@Param("tuNgay") java.time.LocalDateTime tuNgay, Pageable pageable);

    Optional<SanPham> findByDuongDan(String duongDan);

    boolean existsByTenSanPhamAndIdNot(String tenSanPham, Long id);

    boolean existsByTenSanPham(String tenSanPham);
}

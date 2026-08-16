package com.certainshop.repository;

import com.certainshop.entity.KhuyenMai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Long> {

    Optional<KhuyenMai> findByMaKhuyenMai(String maKhuyenMai);

    boolean existsByMaKhuyenMai(String maKhuyenMai);

    boolean existsByMaKhuyenMaiAndIdNot(String maKhuyenMai, Long id);

    // Lấy voucher còn hiệu lực cho đơn hàng
    @Query("SELECT km FROM KhuyenMai km WHERE km.trangThaiKhuyenMai = 'HOAT_DONG' " +
           "AND (km.ngayBatDau IS NULL OR km.ngayBatDau <= :thoiGianHienTai) " +
           "AND (km.ngayKetThuc IS NULL OR km.ngayKetThuc >= :thoiGianHienTai) " +
           "AND (km.soLanSuDungToiDa IS NULL OR km.soLanDaSuDung < km.soLanSuDungToiDa) " +
           "AND (km.giaTriDonHangToiThieu IS NULL OR km.giaTriDonHangToiThieu <= :tongTien) " +
           "ORDER BY km.giaTriGiam DESC")
    List<KhuyenMai> findVoucherHopLe(
            @Param("thoiGianHienTai") LocalDateTime thoiGianHienTai,
            @Param("tongTien") BigDecimal tongTien);

    // Tìm kiếm voucher cho admin
    @Query("SELECT km FROM KhuyenMai km WHERE " +
           "(:tuKhoa IS NULL OR LOWER(km.maKhuyenMai) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR LOWER(km.tenKhuyenMai) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "AND (:trangThai IS NULL OR km.trangThaiKhuyenMai = :trangThai)")
    Page<KhuyenMai> timKiem(
            @Param("tuKhoa") String tuKhoa,
            @Param("trangThai") String trangThai,
            Pageable pageable);
}

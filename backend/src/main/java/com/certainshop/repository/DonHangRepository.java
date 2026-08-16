package com.certainshop.repository;

import com.certainshop.entity.DonHang;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Long> {

    Optional<DonHang> findByMaDonHang(String maDonHang);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dh FROM DonHang dh WHERE dh.maDonHang = :maDonHang")
    Optional<DonHang> findByMaDonHangForUpdate(@Param("maDonHang") String maDonHang);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dh FROM DonHang dh WHERE dh.id = :id")
    Optional<DonHang> findByIdForUpdate(@Param("id") Long id);

    // Đơn hàng của khách hàng
    Page<DonHang> findByNguoiDungIdAndLoaiDonHangOrderByThoiGianTaoDesc(
            Long nguoiDungId, String loaiDonHang, Pageable pageable);

    List<DonHang> findByNguoiDungIdOrderByThoiGianTaoDesc(Long nguoiDungId);

    Page<DonHang> findByNguoiDungIdOrderByThoiGianTaoDesc(Long nguoiDungId, Pageable pageable);

    @Query("SELECT dh FROM DonHang dh WHERE " +
           "(:trangThai IS NULL OR dh.trangThaiDonHang = :trangThai) " +
           "AND (:tuKhoa IS NULL OR LOWER(dh.maDonHang) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR LOWER(dh.tenNguoiNhan) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR dh.sdtNguoiNhan LIKE CONCAT('%', :tuKhoa, '%')) ")
    Page<DonHang> findDonHangAdmin(@Param("trangThai") String trangThai, @Param("tuKhoa") String tuKhoa, Pageable pageable);

    // Hóa đơn chờ tại quầy (tối đa 5)
    @Query("SELECT dh FROM DonHang dh WHERE dh.trangThaiDonHang = 'HOA_DON_CHO' AND dh.loaiDonHang = 'TAI_QUAY' ORDER BY dh.thoiGianTao ASC")
    List<DonHang> findHoaDonCho();

    @Query("SELECT dh FROM DonHang dh WHERE dh.trangThaiDonHang = 'HOA_DON_CHO' AND dh.loaiDonHang = 'TAI_QUAY' AND dh.nhanVien.id = :nhanVienId ORDER BY dh.thoiGianTao ASC")
    List<DonHang> findHoaDonCho(@Param("nhanVienId") Long nhanVienId);

    // Đếm hóa đơn chờ
    @Query("SELECT COUNT(dh) FROM DonHang dh WHERE dh.trangThaiDonHang = 'HOA_DON_CHO' AND dh.loaiDonHang = 'TAI_QUAY'")
    long demHoaDonCho();

    // Hóa đơn chờ quá hạn (để tự hủy)
    @Query("SELECT dh FROM DonHang dh WHERE dh.trangThaiDonHang = 'HOA_DON_CHO' AND dh.thoiGianTuHuy <= :thoiGianHienTai")
    List<DonHang> findHoaDonChoQuaHan(@Param("thoiGianHienTai") LocalDateTime thoiGianHienTai);

    // Tìm kiếm đơn hàng (admin/nhân viên)
    @Query("SELECT dh FROM DonHang dh WHERE " +
           "(:tuKhoa IS NULL OR LOWER(dh.maDonHang) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR LOWER(dh.tenNguoiNhan) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR dh.sdtNguoiNhan LIKE CONCAT('%', :tuKhoa, '%')) " +
           "AND (:trangThai IS NULL OR dh.trangThaiDonHang = :trangThai) " +
           "AND (:loaiDonHang IS NULL OR dh.loaiDonHang = :loaiDonHang) " +
           "AND (:tuNgay IS NULL OR dh.thoiGianTao >= :tuNgay) " +
           "AND (:denNgay IS NULL OR dh.thoiGianTao <= :denNgay) ")
    Page<DonHang> timKiemDonHang(
            @Param("tuKhoa") String tuKhoa,
            @Param("trangThai") String trangThai,
            @Param("loaiDonHang") String loaiDonHang,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable);

    // Thống kê doanh thu theo ngày
    @Query(value = "SELECT CAST(ThoiGianTao AS DATE) AS ngay, SUM(TongTienThanhToan) AS tong " +
           "FROM DonHang WHERE TrangThaiDonHang = 'HOAN_TAT' " +
           "AND ThoiGianTao BETWEEN :tuNgay AND :denNgay " +
           "GROUP BY CAST(ThoiGianTao AS DATE) " +
           "ORDER BY CAST(ThoiGianTao AS DATE) ASC", nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoNgay(
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay);

    // Tổng doanh thu
    @Query("SELECT COALESCE(SUM(dh.tongTienThanhToan), 0) FROM DonHang dh " +
           "WHERE dh.trangThaiDonHang = 'HOAN_TAT' AND dh.thoiGianTao BETWEEN :tuNgay AND :denNgay")
    BigDecimal tinhTongDoanhThu(
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay);

    // Đếm đơn hàng theo trạng thái
    @Query("SELECT dh.trangThaiDonHang, COUNT(dh) FROM DonHang dh " +
           "WHERE dh.thoiGianTao BETWEEN :tuNgay AND :denNgay " +
           "GROUP BY dh.trangThaiDonHang")
    List<Object[]> demDonHangTheoTrangThai(
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay);

    // Đơn VNPay đang chờ thanh toán quá 15 phút
    @Query("SELECT dh FROM DonHang dh WHERE dh.trangThaiDonHang = 'CHO_THANH_TOAN' " +
           "AND dh.thoiGianTao <= :thoiGianHetHan")
    List<DonHang> findVNPayHetHan(@Param("thoiGianHetHan") LocalDateTime thoiGianHetHan);
}

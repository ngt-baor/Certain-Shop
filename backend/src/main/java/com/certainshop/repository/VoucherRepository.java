package com.certainshop.repository;

import com.certainshop.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByMaVoucher(String maVoucher);

    boolean existsByMaVoucher(String maVoucher);

    // Find all active vouchers (trangThai = true, regardless of date)
    @Query("SELECT v FROM Voucher v WHERE v.trangThai = true ORDER BY v.thoiGianTao DESC")
    List<Voucher> findAllActive();

    // Find all valid vouchers (active, within date range, not exceeded max usage)
    @Query("SELECT v FROM Voucher v WHERE v.trangThai = true " +
           "AND :now >= v.ngayBatDau AND :now <= v.ngayKetThuc " +
           "AND (v.soLuongToiDa IS NULL OR v.soLuongSuDung < v.soLuongToiDa) " +
           "ORDER BY v.thoiGianTao DESC")
    List<Voucher> findAllValidVouchers(@Param("now") LocalDateTime now);

    // Find vouchers for a specific month
    @Query("SELECT v FROM Voucher v WHERE v.trangThai = true " +
           "AND YEAR(v.ngayBatDau) = :year AND MONTH(v.ngayBatDau) = :month")
    List<Voucher> findByMonth(@Param("year") int year, @Param("month") int month);
}

package com.certainshop.repository;

import com.certainshop.entity.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);

    Optional<NguoiDung> findByEmail(String email);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmail(String email);

    boolean existsByTenDangNhapAndIdNot(String tenDangNhap, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT nd FROM NguoiDung nd WHERE nd.vaiTro.tenVaiTro = :tenVaiTro AND nd.dangHoatDong = true")
    List<NguoiDung> findByVaiTro(@Param("tenVaiTro") String tenVaiTro);

    @Query("SELECT nd FROM NguoiDung nd WHERE " +
           "(:tuKhoa IS NULL OR LOWER(nd.hoTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR LOWER(nd.tenDangNhap) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR LOWER(nd.email) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR nd.soDienThoai LIKE CONCAT('%', :tuKhoa, '%')) " +
           "AND (:tenVaiTro IS NULL OR nd.vaiTro.tenVaiTro = :tenVaiTro)")
    Page<NguoiDung> timKiem(@Param("tuKhoa") String tuKhoa, @Param("tenVaiTro") String tenVaiTro, Pageable pageable);

    @Query("SELECT nd FROM NguoiDung nd WHERE " +
           "(:tuKhoa IS NULL OR LOWER(nd.hoTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR LOWER(nd.tenDangNhap) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR LOWER(nd.email) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "OR nd.soDienThoai LIKE CONCAT('%', :tuKhoa, '%'))")
    Page<NguoiDung> timKiem(@Param("tuKhoa") String tuKhoa, Pageable pageable);

    @Query("SELECT nd FROM NguoiDung nd WHERE nd.vaiTro.tenVaiTro = :tenVaiTro")
    Page<NguoiDung> findByTenVaiTro(@Param("tenVaiTro") String tenVaiTro, Pageable pageable);
}

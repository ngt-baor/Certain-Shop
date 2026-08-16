package com.certainshop.repository;

import com.certainshop.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
    List<DanhMuc> findByDangHoatDongTrueOrderByThuTuHienThiAsc();
    List<DanhMuc> findAllByDangHoatDongTrueOrderByThuTuHienThiAsc();
    Optional<DanhMuc> findByDuongDan(String duongDan);
    boolean existsByTenDanhMuc(String tenDanhMuc);
    boolean existsByTenDanhMucAndIdNot(String tenDanhMuc, Long id);
}

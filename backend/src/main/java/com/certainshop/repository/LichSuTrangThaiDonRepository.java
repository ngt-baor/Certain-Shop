package com.certainshop.repository;

import com.certainshop.entity.LichSuTrangThaiDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LichSuTrangThaiDonRepository extends JpaRepository<LichSuTrangThaiDon, Long> {
    List<LichSuTrangThaiDon> findByDonHangIdOrderByThoiGianAsc(Long donHangId);
}

package com.certainshop.repository;

import com.certainshop.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {
    List<DanhGia> findBySanPhamIdOrderByThoiGianTaoDesc(Long sanPhamId);

    @Query("SELECT AVG(dg.diemDanhGia) FROM DanhGia dg WHERE dg.sanPham.id = :sanPhamId")
    Double tinhDiemTrungBinh(@Param("sanPhamId") Long sanPhamId);

    boolean existsByNguoiDungIdAndChiTietDonHangId(Long nguoiDungId, Long chiTietDonHangId);
}

package com.certainshop.repository;

import com.certainshop.entity.ChiTietDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Long> {

    List<ChiTietDonHang> findByDonHangId(Long donHangId);

    // Sản phẩm bán chạy theo tháng
    @Query("SELECT ctdh.bienThe.sanPham.id, ctdh.bienThe.sanPham.tenSanPham, SUM(ctdh.soLuong) as tongSoLuong " +
           "FROM ChiTietDonHang ctdh JOIN ctdh.donHang dh " +
           "WHERE dh.trangThaiDonHang = 'HOAN_TAT' " +
           "AND dh.thoiGianTao >= :tuNgay " +
           "GROUP BY ctdh.bienThe.sanPham.id, ctdh.bienThe.sanPham.tenSanPham " +
           "ORDER BY tongSoLuong DESC")
    List<Object[]> thongKeSanPhamBanChay(@Param("tuNgay") LocalDateTime tuNgay);
}

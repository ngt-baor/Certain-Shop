package com.certainshop.repository;

import com.certainshop.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Long> {
    List<ThuongHieu> findByTrangThaiTrueOrderByTenThuongHieuAsc();
    List<ThuongHieu> findAllByTrangThaiTrue();
    boolean existsByTenThuongHieu(String tenThuongHieu);
    boolean existsByTenThuongHieuAndIdNot(String tenThuongHieu, Long id);
}

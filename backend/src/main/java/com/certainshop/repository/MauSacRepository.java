package com.certainshop.repository;

import com.certainshop.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Long> {
    List<MauSac> findAllByOrderByTenMauAsc();
    boolean existsByTenMau(String tenMau);
    boolean existsByTenMauAndIdNot(String tenMau, Long id);
}

package com.certainshop.repository;

import com.certainshop.entity.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Long> {
    List<KichThuoc> findAllByOrderByKichCoAsc();
    boolean existsByKichCo(String kichCo);
    boolean existsByKichCoAndIdNot(String kichCo, Long id);
}

package com.certainshop.repository;

import com.certainshop.entity.HinhAnhBienThe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HinhAnhBienTheRepository extends JpaRepository<HinhAnhBienThe, Long> {
    List<HinhAnhBienThe> findByBienTheIdOrderByThuTuAsc(Long bienTheId);
    Optional<HinhAnhBienThe> findByBienTheIdAndLaAnhChinhTrue(Long bienTheId);
    void deleteByBienTheId(Long bienTheId);
}

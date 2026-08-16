package com.certainshop.repository;

import com.certainshop.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Long> {
    Optional<GioHangChiTiet> findByGioHangIdAndBienTheId(Long gioHangId, Long bienTheId);

    /**
     * Xóa thẳng bằng JPQL bulk-delete để tránh conflict với managed collection (orphanRemoval).
     * Dùng clearAutomatically=true để reset L1 cache sau khi delete.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM GioHangChiTiet g WHERE g.gioHang.id = :gioHangId")
    void deleteByGioHangId(@Param("gioHangId") Long gioHangId);
}

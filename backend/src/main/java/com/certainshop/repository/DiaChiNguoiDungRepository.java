package com.certainshop.repository;

import com.certainshop.entity.DiaChiNguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiNguoiDungRepository extends JpaRepository<DiaChiNguoiDung, Long> {

    Optional<DiaChiNguoiDung> findByIdAndNguoiDungId(Long id, Long nguoiDungId);

    List<DiaChiNguoiDung> findByNguoiDungIdOrderByLaMacDinhDesc(Long nguoiDungId);

    Optional<DiaChiNguoiDung> findByNguoiDungIdAndLaMacDinhTrue(Long nguoiDungId);

    @Modifying
    @Query("UPDATE DiaChiNguoiDung dc SET dc.laMacDinh = false WHERE dc.nguoiDung.id = :nguoiDungId")
    void boMacDinhTatCaDiaChi(@Param("nguoiDungId") Long nguoiDungId);

    long countByNguoiDungId(Long nguoiDungId);
}

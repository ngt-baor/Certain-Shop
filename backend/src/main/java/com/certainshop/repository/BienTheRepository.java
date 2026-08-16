package com.certainshop.repository;

import com.certainshop.entity.BienThe;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BienTheRepository extends JpaRepository<BienThe, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bt FROM BienThe bt WHERE bt.id = :id")
    Optional<BienThe> findByIdForUpdate(@Param("id") Long id);

    // Fetch eagerly with images to avoid LazyInitializationException
    @Query("SELECT DISTINCT bt FROM BienThe bt " +
           "LEFT JOIN FETCH bt.danhSachHinhAnh " +
           "LEFT JOIN FETCH bt.kichThuoc " +
           "LEFT JOIN FETCH bt.mauSac " +
           "LEFT JOIN FETCH bt.chatLieu " +
           "WHERE bt.sanPham.id = :sanPhamId " +
           "ORDER BY bt.macDinh DESC, bt.id ASC")
    List<BienThe> findBySanPhamId(@Param("sanPhamId") Long sanPhamId);

    List<BienThe> findBySanPhamIdAndTrangThaiTrue(Long sanPhamId);

    Optional<BienThe> findBySanPhamIdAndMacDinhTrue(Long sanPhamId);

    // Kiểm tra trùng biến thể
    @Query("SELECT COUNT(bt) > 0 FROM BienThe bt WHERE bt.sanPham.id = :sanPhamId " +
           "AND (:kichThuocId IS NULL AND bt.kichThuoc IS NULL OR bt.kichThuoc.id = :kichThuocId) " +
           "AND (:mauSacId IS NULL AND bt.mauSac IS NULL OR bt.mauSac.id = :mauSacId) " +
           "AND (:chatLieuId IS NULL AND bt.chatLieu IS NULL OR bt.chatLieu.id = :chatLieuId) " +
           "AND bt.id != :excludeId")
    boolean kiemTraTrungBienThe(@Param("sanPhamId") Long sanPhamId,
                                 @Param("kichThuocId") Long kichThuocId,
                                 @Param("mauSacId") Long mauSacId,
                                 @Param("chatLieuId") Long chatLieuId,
                                 @Param("excludeId") Long excludeId);

    // Biến thể sắp hết hàng
    @Query("SELECT bt FROM BienThe bt WHERE bt.trangThai = true AND bt.soLuongTon <= :nguongCanhBao AND bt.soLuongTon > 0 ORDER BY bt.soLuongTon ASC")
    List<BienThe> findBienTheSapHetHang(@Param("nguongCanhBao") int nguongCanhBao);

    // Biến thể hết hàng
    @Query("SELECT bt FROM BienThe bt WHERE bt.trangThai = true AND bt.soLuongTon = 0")
    List<BienThe> findBienTheHetHang();

    // Tìm kiếm biến thể cho bán hàng tại quầy
    @Query("SELECT bt FROM BienThe bt JOIN bt.sanPham sp WHERE bt.trangThai = true AND bt.soLuongTon > 0 " +
           "AND (LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(bt.kichThuoc.kichCo) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(bt.mauSac.tenMau) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY sp.tenSanPham ASC")
    List<BienThe> timKiemChoQuay(@Param("q") String q);
}

package com.certainshop.service;

import com.certainshop.constant.TrangThaiDonHang;
import com.certainshop.dto.DatHangDto;
import com.certainshop.entity.*;
import com.certainshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DonHangService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final BienTheRepository bienTheRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final LichSuTrangThaiDonRepository lichSuRepository;
    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final KhuyenMaiService khuyenMaiService;
    private final VoucherService voucherService;
    private final DiaChiNguoiDungRepository diaChiRepository;
    private final MailService mailService;
    private final GHNApiService ghnApiService;

    @Value("${app.hoadon.soLuongChoToiDa:5}")
    private int soLuongHoaDonChoToiDa;

    @Value("${app.hoadon.thoiGianTuHuy:120}")
    private int thoiGianTuHuyPhut;

    // ===================== ĐẶT HÀNG ONLINE =====================

    /**
     * Tạo đơn hàng online (từ giỏ hàng)
     */
    public DonHang datHangOnline(Long nguoiDungId, DatHangDto dto) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(nguoiDungId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        GioHang gioHang = gioHangRepository.findByNguoiDungId(nguoiDungId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        List<GioHangChiTiet> danhSachGio = gioHang.getDanhSachChiTiet();
        if (danhSachGio == null || danhSachGio.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống");
        }

        // Tính tổng tiền sản phẩm
        BigDecimal tongTien = danhSachGio.stream()
                .map(GioHangChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Áp dụng giảm giá (KhuyenMai hoặc Voucher)
        BigDecimal soTienGiam = BigDecimal.ZERO;
        KhuyenMai khuyenMai = null;
        Voucher voucher = null;

        boolean hasKhuyenMai = dto.getKhuyenMaiId() != null;
        boolean hasVoucher = dto.getMaVoucher() != null && !dto.getMaVoucher().isBlank();
        if (hasKhuyenMai && hasVoucher) {
            throw new IllegalArgumentException("Chỉ được áp dụng 1 loại giảm giá (khuyến mãi hoặc voucher)");
        }

        if (hasKhuyenMai) {
            khuyenMai = khuyenMaiService.timTheoId(dto.getKhuyenMaiId()).orElse(null);
            if (khuyenMai != null && khuyenMai.laHopLe()) {
                soTienGiam = khuyenMai.tinhSoTienGiam(tongTien);
            } else {
                throw new IllegalArgumentException("Khuyến mãi không hợp lệ hoặc đã hết hạn");
            }
        }

        if (hasVoucher) {
            String maVoucher = dto.getMaVoucher().trim().toUpperCase();
            voucher = voucherService.timTheoMa(maVoucher).orElse(null);
            if (voucher == null || !voucher.isValid()) {
                throw new IllegalArgumentException("Voucher không hợp lệ hoặc đã hết hạn");
            }
            soTienGiam = voucher.tinhGiaTriGiam(tongTien);
            if (soTienGiam.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Voucher không áp dụng được cho đơn hàng này");
            }
        }

        // Phí vận chuyển
        // Xác định trạng thái ban đầu
        String trangThaiDau;
        if ("VNPAY".equalsIgnoreCase(dto.getPhuongThucThanhToan())) {
            trangThaiDau = TrangThaiDonHang.CHO_THANH_TOAN;
        } else {
            trangThaiDau = TrangThaiDonHang.CHO_XAC_NHAN;
        }

        // Lấy địa chỉ giao hàng
        String tenNguoiNhan = dto.getTenNguoiNhan();
        String sdt = dto.getSoDienThoai();
        String diaChiCuThe = dto.getDiaChiCuThe();
        String tenXa = dto.getTenXa();
        String tenHuyen = dto.getTenHuyen();
        String tenTinh = dto.getTenTinh();
        Integer maTinh = dto.getMaTinhGHN();
        Integer maHuyen = dto.getMaHuyenGHN();
        String maXa = dto.getMaXaGHN();

        if (dto.getDiaChiId() != null) {
            DiaChiNguoiDung diaChi = diaChiRepository
                    .findByIdAndNguoiDungId(dto.getDiaChiId(), nguoiDungId)
                    .orElseThrow(() -> new SecurityException("Địa chỉ giao hàng không thuộc tài khoản hiện tại"));
            tenNguoiNhan = diaChi.getHoTen();
            sdt = diaChi.getSoDienThoai();
            diaChiCuThe = diaChi.getDiaChiDong1();
            tenXa = diaChi.getPhuongXa();
            tenHuyen = diaChi.getQuanHuyen();
            tenTinh = diaChi.getTinhThanh();
            maTinh = diaChi.getMaTinhGHN();
            maHuyen = diaChi.getMaHuyenGHN();
            maXa = diaChi.getMaXaGHN();
        }

        BigDecimal phiVanChuyen = maHuyen != null && maXa != null && !maXa.isBlank()
                ? ghnApiService.tinhPhiVanChuyen(maHuyen, maXa, 500)
                : BigDecimal.valueOf(35_000);
        BigDecimal tongThanhToan = tongTien.subtract(soTienGiam).add(phiVanChuyen);
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) tongThanhToan = BigDecimal.ZERO;

        DonHang donHang = DonHang.builder()
                .nguoiDung(nguoiDung)
                .maDonHang(sinhMaDonHang())
                .tongTien(tongTien)
                .soTienGiamGia(soTienGiam)
                .phiVanChuyen(phiVanChuyen)
                .tongTienThanhToan(tongThanhToan)
                .trangThaiDonHang(trangThaiDau)
                .loaiDonHang("ONLINE")
                .phuongThucThanhToan(dto.getPhuongThucThanhToan())
                .khuyenMai(khuyenMai)
                .voucher(voucher)
                .tenNguoiNhan(tenNguoiNhan)
                .sdtNguoiNhan(sdt)
                .diaChiGiaoHang(buildDiaChiDayDu(diaChiCuThe, tenXa, tenHuyen, tenTinh))
                .maTinhGHN(maTinh)
                .maHuyenGHN(maHuyen)
                .maXaGHN(maXa)
                .ghiChu(dto.getGhiChu())
                .daThanhToan(false)
                .build();

        donHang = donHangRepository.save(donHang);

        // Tạo chi tiết đơn hàng
        final DonHang savedDonHang = donHang;
        List<ChiTietDonHang> danhSachChiTiet = new ArrayList<>();
        for (GioHangChiTiet gioItem : danhSachGio) {
            BienThe bienThe = gioItem.getBienThe();
            // Chỉ kiểm tra tồn kho (không trừ ngay với COD)
            if (bienThe.getSoLuongTon() < gioItem.getSoLuong()) {
                throw new IllegalArgumentException(
                        "Sản phẩm '" + bienThe.getSanPham().getTenSanPham() + " - " + 
                        (bienThe.getMauSac() != null ? bienThe.getMauSac().getTenMau() : "") + 
                        " " + (bienThe.getKichThuoc() != null ? bienThe.getKichThuoc().getKichCo() : "") + 
                        "' chỉ còn " + bienThe.getSoLuongTon() + " trong kho");
            }

            ChiTietDonHang chiTiet = ChiTietDonHang.builder()
                    .donHang(savedDonHang)
                    .bienThe(bienThe)
                    .giaTaiThoiDiemMua(gioItem.getDonGia())
                    .soLuong(gioItem.getSoLuong())
                    .build();
            danhSachChiTiet.add(chiTiet);
        }
        chiTietDonHangRepository.saveAll(danhSachChiTiet);

        // Ghi lịch sử trạng thái
        ghiLichSuTrangThai(donHang, null, trangThaiDau, "Đặt hàng thành công", nguoiDung);

        // Tăng số lần sử dụng giảm giá
        if (khuyenMai != null) {
            khuyenMaiService.tangSoLanSuDung(khuyenMai.getId());
        }
        if (voucher != null) {
            voucherService.tangSoLanSuDung(voucher.getMaVoucher());
        }

        // Lưu địa chỉ mới nếu yêu cầu
        if (Boolean.TRUE.equals(dto.getLuuDiaChi()) && dto.getDiaChiId() == null) {
            DiaChiNguoiDung diaChi = DiaChiNguoiDung.builder()
                    .nguoiDung(nguoiDung)
                    .hoTen(tenNguoiNhan)
                    .soDienThoai(sdt)
                    .diaChiDong1(diaChiCuThe)
                    .phuongXa(tenXa)
                    .quanHuyen(tenHuyen)
                    .tinhThanh(tenTinh)
                    .maTinhGHN(maTinh)
                    .maHuyenGHN(maHuyen)
                    .maXaGHN(maXa)
                    .laMacDinh(false)
                    .build();
            diaChiRepository.save(diaChi);
        }

        // Xóa giỏ hàng sau khi đặt thành công
        // Dùng orphanRemoval để tránh conflict với JPA managed collection (CascadeType.ALL + orphanRemoval=true)
        gioHang.getDanhSachChiTiet().clear();
        gioHangRepository.save(gioHang);

        // Gửi mail xác nhận — chỉ với COD (VNPay gửi sau khi callback thanh toán thành công)
        if (!"VNPAY".equalsIgnoreCase(dto.getPhuongThucThanhToan())) {
            mailService.guiMailXacNhanDonHang(
                    nguoiDung.getEmail(),
                    nguoiDung.getHoTen(),
                    donHang.getMaDonHang(),
                    donHang.getTongTienThanhToan(),
                    dto.getPhuongThucThanhToan()
            );
        }

        return donHang;
    }

    /**
     * Trừ kho khi xác nhận đơn COD
     */
    private void truKho(DonHang donHang) {
        for (ChiTietDonHang ct : donHang.getDanhSachChiTiet()) {
            BienThe bienThe = bienTheRepository.findByIdForUpdate(ct.getBienThe().getId())
                    .orElseThrow(() -> new IllegalStateException("Biến thể sản phẩm không còn tồn tại"));
            int soLuongMoi = bienThe.getSoLuongTon() - ct.getSoLuong();
            if (soLuongMoi < 0) {
                throw new IllegalStateException(
                        "Sản phẩm '" + bienThe.getSanPham().getTenSanPham() + "' không đủ số lượng trong kho");
            }
            bienThe.setSoLuongTon(soLuongMoi);
            bienTheRepository.save(bienThe);
        }
    }

    /**
     * Rollback kho khi hủy đơn
     */
    private void rollbackKho(DonHang donHang) {
        for (ChiTietDonHang ct : donHang.getDanhSachChiTiet()) {
            BienThe bienThe = bienTheRepository.findByIdForUpdate(ct.getBienThe().getId())
                    .orElseThrow(() -> new IllegalStateException("Biến thể sản phẩm không còn tồn tại"));
            bienThe.setSoLuongTon(bienThe.getSoLuongTon() + ct.getSoLuong());
            bienTheRepository.save(bienThe);
        }
    }

    // ===================== CHUYỂN TRẠNG THÁI =====================

    /**
     * Nhân viên/Admin chuyển trạng thái đơn hàng (chỉ tiến)
     */
    public DonHang chuyenTrangThai(Long donHangId, String trangThaiMoi, String ghiChu, Long nguoiDungId) {
        if (TrangThaiDonHang.DA_HUY.equals(trangThaiMoi)) {
            return nhanVienHuyDon(donHangId, ghiChu, nguoiDungId);
        }

        DonHang donHang = donHangRepository.findByIdForUpdate(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String trangThaiHienTai = donHang.getTrangThaiDonHang();
        NguoiDung nguoiThayDoi = nguoiDungRepository.findById(nguoiDungId).orElse(null);

        // Validate luồng trạng thái
        kiemTraLuongTrangThai(trangThaiHienTai, trangThaiMoi, donHang.getPhuongThucThanhToan());

        // Trừ kho khi chuyển sang DA_XAC_NHAN (for both COD and VNPAY)
        // - COD: admin confirms order → deduct inventory
        // - VNPAY: admin confirms paid order → deduct inventory
        if (TrangThaiDonHang.DA_XAC_NHAN.equals(trangThaiMoi)) {
            truKho(donHang);
        }

        donHang.setTrangThaiDonHang(trangThaiMoi);
        donHang = donHangRepository.save(donHang);

        ghiLichSuTrangThai(donHang, trangThaiHienTai, trangThaiMoi, ghiChu, nguoiThayDoi);

        // Gửi mail cập nhật trạng thái cho khách
        if (donHang.getNguoiDung() != null) {
            mailService.guiMailCapNhatTrangThai(
                    donHang.getNguoiDung().getEmail(),
                    donHang.getNguoiDung().getHoTen(),
                    donHang.getMaDonHang(),
                    trangThaiMoi
            );
        }

        return donHang;
    }

    /**
     * Hủy đơn hàng (khách hàng)
     */
    public DonHang khachHuyDon(Long donHangId, String lyDo, Long nguoiDungId) {
        DonHang donHang = donHangRepository.findByIdForUpdate(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (donHang.getNguoiDung() == null
                || !donHang.getNguoiDung().getId().equals(nguoiDungId)) {
            throw new SecurityException("Không có quyền hủy đơn hàng này");
        }
        if (Boolean.TRUE.equals(donHang.getDaThanhToan())) {
            throw new IllegalArgumentException("Đơn đã thanh toán cần được hoàn tiền trước khi hủy");
        }

        // Kiểm tra quyền hủy của khách
        String trangThai = donHang.getTrangThaiDonHang();
        boolean coQuyen = TrangThaiDonHang.CHO_XAC_NHAN.equals(trangThai)
                || TrangThaiDonHang.CHO_THANH_TOAN.equals(trangThai);

        if (!coQuyen) {
            throw new IllegalArgumentException("Bạn không thể hủy đơn hàng ở trạng thái: " +
                    TrangThaiDonHang.layNhan(trangThai));
        }

        // Hoàn voucher
        if (donHang.getKhuyenMai() != null) {
            khuyenMaiService.giamSoLanSuDung(donHang.getKhuyenMai().getId());
        }
        if (donHang.getVoucher() != null) {
            voucherService.giamSoLanSuDung(donHang.getVoucher().getMaVoucher());
        }

        donHang.setTrangThaiDonHang(TrangThaiDonHang.DA_HUY);
        donHang = donHangRepository.save(donHang);

        NguoiDung nguoiDung = nguoiDungRepository.findById(nguoiDungId).orElse(null);
        ghiLichSuTrangThai(donHang, trangThai, TrangThaiDonHang.DA_HUY, "Khách hủy: " + lyDo, nguoiDung);

        // Gửi mail thông báo hủy cho khách
        if (donHang.getNguoiDung() != null) {
            mailService.guiMailHuyDon(
                    donHang.getNguoiDung().getEmail(),
                    donHang.getNguoiDung().getHoTen(),
                    donHang.getMaDonHang(),
                    lyDo,
                    true
            );
        }

        return donHang;
    }

    /**
     * Nhân viên hủy đơn
     */
    public DonHang nhanVienHuyDon(Long donHangId, String lyDo, Long nhanVienId) {
        DonHang donHang = donHangRepository.findByIdForUpdate(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (Boolean.TRUE.equals(donHang.getDaThanhToan())) {
            throw new IllegalArgumentException("Đơn đã thanh toán cần được hoàn tiền trước khi hủy");
        }

        String trangThai = donHang.getTrangThaiDonHang();
        boolean coQuyen = TrangThaiDonHang.CHO_XAC_NHAN.equals(trangThai)
                || TrangThaiDonHang.DA_XAC_NHAN.equals(trangThai)
                || TrangThaiDonHang.DANG_XU_LY.equals(trangThai)
                || TrangThaiDonHang.CHO_THANH_TOAN.equals(trangThai);

        if (!coQuyen) {
            throw new IllegalArgumentException("Không thể hủy đơn ở trạng thái: " + TrangThaiDonHang.layNhan(trangThai));
        }

        if (TrangThaiDonHang.DA_XAC_NHAN.equals(trangThai)
                || TrangThaiDonHang.DANG_XU_LY.equals(trangThai)) {
            rollbackKho(donHang);
        }

        if (donHang.getKhuyenMai() != null) {
            khuyenMaiService.giamSoLanSuDung(donHang.getKhuyenMai().getId());
        }
        if (donHang.getVoucher() != null) {
            voucherService.giamSoLanSuDung(donHang.getVoucher().getMaVoucher());
        }

        donHang.setTrangThaiDonHang(TrangThaiDonHang.DA_HUY);
        donHang = donHangRepository.save(donHang);

        NguoiDung nhanVien = nguoiDungRepository.findById(nhanVienId).orElse(null);
        ghiLichSuTrangThai(donHang, trangThai, TrangThaiDonHang.DA_HUY, "Nhân viên hủy: " + lyDo, nhanVien);

        // Gửi mail thông báo hủy cho khách
        if (donHang.getNguoiDung() != null) {
            mailService.guiMailHuyDon(
                    donHang.getNguoiDung().getEmail(),
                    donHang.getNguoiDung().getHoTen(),
                    donHang.getMaDonHang(),
                    lyDo,
                    false
            );
        }

        return donHang;
    }

    // ===================== BÁN TẠI QUẦY =====================

    /**
     * Tạo hóa đơn chờ tại quầy (tối đa 5)
     */
    public DonHang taoHoaDonCho(NguoiDung nhanVien) {
        long soHoaDonHienTai = donHangRepository.demHoaDonCho();
        if (soHoaDonHienTai >= soLuongHoaDonChoToiDa) {
            throw new IllegalArgumentException(
                    "Đã đạt tối đa " + soLuongHoaDonChoToiDa + " hóa đơn chờ. Vui lòng xử lý hoặc hủy bớt.");
        }

        DonHang donHang = DonHang.builder()
                .maDonHang(sinhMaDonHang())
                .trangThaiDonHang(TrangThaiDonHang.HOA_DON_CHO)
                .loaiDonHang("TAI_QUAY")
                .tongTien(BigDecimal.ZERO)
                .soTienGiamGia(BigDecimal.ZERO)
                .phiVanChuyen(BigDecimal.ZERO)
                .tongTienThanhToan(BigDecimal.ZERO)
                .nhanVien(nhanVien)
                .daThanhToan(false)
                .thoiGianTuHuy(LocalDateTime.now().plusMinutes(thoiGianTuHuyPhut))
                .build();

        donHang = donHangRepository.save(donHang);
        ghiLichSuTrangThai(donHang, null, TrangThaiDonHang.HOA_DON_CHO, "Tạo hóa đơn chờ tại quầy", nhanVien);
        return donHang;
    }

    /**
     * Thêm sản phẩm vào hóa đơn tại quầy
     */
    public DonHang themSanPhamVaoHoaDonTaiQuay(Long donHangId, Long bienTheId, int soLuong) {
        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!TrangThaiDonHang.HOA_DON_CHO.equals(donHang.getTrangThaiDonHang())) {
            throw new IllegalArgumentException("Hóa đơn không còn ở trạng thái chờ");
        }

        BienThe bienThe = bienTheRepository.findById(bienTheId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Kiểm tra và trừ kho ngay (vì khách thật đang đứng ở quầy)
        if (soLuong > bienThe.getSoLuongTon()) {
            throw new IllegalArgumentException("Chỉ còn " + bienThe.getSoLuongTon() + " sản phẩm trong kho");
        }

        // Tìm xem sản phẩm đã có trong hóa đơn chưa
        Optional<ChiTietDonHang> chiTietHienCo = donHang.getDanhSachChiTiet().stream()
                .filter(ct -> ct.getBienThe().getId().equals(bienTheId))
                .findFirst();

        if (chiTietHienCo.isPresent()) {
            ChiTietDonHang ct = chiTietHienCo.get();
            int soLuongMoi = ct.getSoLuong() + soLuong;
            if (soLuongMoi > bienThe.getSoLuongTon() + ct.getSoLuong()) {
                throw new IllegalArgumentException("Không đủ hàng trong kho");
            }
            // Hoàn lại số lượng cũ, trừ số lượng mới
            bienThe.setSoLuongTon(bienThe.getSoLuongTon() - soLuong);
            ct.setSoLuong(soLuongMoi);
            chiTietDonHangRepository.save(ct);
        } else {
            bienThe.setSoLuongTon(bienThe.getSoLuongTon() - soLuong);
            ChiTietDonHang chiTiet = ChiTietDonHang.builder()
                    .donHang(donHang)
                    .bienThe(bienThe)
                    .giaTaiThoiDiemMua(bienThe.getGia())
                    .soLuong(soLuong)
                    .build();
            chiTietDonHangRepository.save(chiTiet);
        }

        bienTheRepository.save(bienThe);
        capNhatTongTienDonHang(donHang);
        return donHangRepository.findById(donHangId).orElse(donHang);
    }

    /**
     * Xóa sản phẩm khỏi hóa đơn tại quầy
     */
    public DonHang xoaSanPhamKhoiHoaDonTaiQuay(Long donHangId, Long chiTietId) {
        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        ChiTietDonHang ct = chiTietDonHangRepository.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết"));

        // Hoàn kho
        BienThe bienThe = ct.getBienThe();
        bienThe.setSoLuongTon(bienThe.getSoLuongTon() + ct.getSoLuong());
        bienTheRepository.save(bienThe);

        chiTietDonHangRepository.delete(ct);
        capNhatTongTienDonHang(donHang);
        return donHangRepository.findById(donHangId).orElse(donHang);
    }

    /**
     * Thanh toán hóa đơn tại quầy
     */
    public DonHang thanhToanTaiQuay(Long donHangId, String phuongThucThanhToan,
                                     Long khuyenMaiId, NguoiDung nhanVien,
                                     NguoiDung khachHang, String tenNguoiNhan,
                                     String sdt, String diaChi) {
        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!TrangThaiDonHang.HOA_DON_CHO.equals(donHang.getTrangThaiDonHang())) {
            throw new IllegalArgumentException("Hóa đơn không ở trạng thái chờ");
        }

        if (donHang.getDanhSachChiTiet() == null || donHang.getDanhSachChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn chưa có sản phẩm");
        }

        // Áp dụng voucher
        BigDecimal soTienGiam = BigDecimal.ZERO;
        KhuyenMai khuyenMai = null;
        if (khuyenMaiId != null) {
            khuyenMai = khuyenMaiService.timTheoId(khuyenMaiId).orElse(null);
            if (khuyenMai != null && khuyenMai.laHopLe()) {
                soTienGiam = khuyenMai.tinhSoTienGiam(donHang.getTongTien());
                khuyenMaiService.tangSoLanSuDung(khuyenMai.getId());
            }
        }

        donHang.setKhuyenMai(khuyenMai);
        donHang.setSoTienGiamGia(soTienGiam);
        donHang.setNguoiDung(khachHang);
        donHang.setNhanVien(nhanVien);
        donHang.setPhuongThucThanhToan(phuongThucThanhToan);
        donHang.setTenNguoiNhan(tenNguoiNhan != null ? tenNguoiNhan : (khachHang != null ? khachHang.getHoTen() : "Khách lẻ"));
        donHang.setSdtNguoiNhan(sdt != null ? sdt : (khachHang != null ? khachHang.getSoDienThoai() : ""));
        donHang.setDiaChiGiaoHang(diaChi);

        BigDecimal tongThanhToan = donHang.getTongTien().subtract(soTienGiam);
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) tongThanhToan = BigDecimal.ZERO;
        donHang.setTongTienThanhToan(tongThanhToan);
        donHang.setTrangThaiDonHang(TrangThaiDonHang.HOAN_TAT);
        donHang.setThoiGianTuHuy(null);

        donHang = donHangRepository.save(donHang);
        ghiLichSuTrangThai(donHang, TrangThaiDonHang.HOA_DON_CHO, TrangThaiDonHang.HOAN_TAT, "Thanh toán tại quầy - " + phuongThucThanhToan, nhanVien);

        return donHang;
    }

    /**
     * Hủy hóa đơn chờ tại quầy
     */
    public void huyHoaDonCho(Long donHangId, NguoiDung nhanVien) {
        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!TrangThaiDonHang.HOA_DON_CHO.equals(donHang.getTrangThaiDonHang())) {
            throw new IllegalArgumentException("Chỉ có thể hủy hóa đơn đang ở trạng thái chờ");
        }

        // Hoàn kho tất cả sản phẩm
        if (donHang.getDanhSachChiTiet() != null) {
            donHang.getDanhSachChiTiet().forEach(ct -> {
                BienThe bt = ct.getBienThe();
                bt.setSoLuongTon(bt.getSoLuongTon() + ct.getSoLuong());
                bienTheRepository.save(bt);
            });
        }

        donHang.setTrangThaiDonHang(TrangThaiDonHang.DA_HUY);
        donHangRepository.save(donHang);
        ghiLichSuTrangThai(donHang, TrangThaiDonHang.HOA_DON_CHO, TrangThaiDonHang.DA_HUY, "Hủy hóa đơn chờ", nhanVien);
    }

    // ===================== TÌM KIẾM =====================

    @Transactional(readOnly = true)
    public Optional<DonHang> timTheoId(Long id) {
        return donHangRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<DonHang> timTheoMa(String ma) {
        return donHangRepository.findByMaDonHang(ma);
    }

    @Transactional(readOnly = true)
    public List<DonHang> layDonHangCuaKhach(Long nguoiDungId) {
        return donHangRepository.findByNguoiDungIdOrderByThoiGianTaoDesc(nguoiDungId);
    }

    @Transactional(readOnly = true)
    public Page<DonHang> timKiemDonHang(String tuKhoa, String trangThai, String loaiDonHang,
                                          LocalDateTime tuNgay, LocalDateTime denNgay, Pageable pageable) {
        return donHangRepository.timKiemDonHang(tuKhoa, trangThai, loaiDonHang, tuNgay, denNgay, pageable);
    }

    @Transactional(readOnly = true)
    public List<DonHang> layDanhSachHoaDonCho() {
        return donHangRepository.findHoaDonCho();
    }

    // ===================== SCHEDULE TỰ HỦY =====================

    /**
     * Job tự động hủy hóa đơn chờ quá hạn (chạy mỗi phút)
     */
    @Scheduled(fixedDelay = 60000)
    public void tuDongHuyHoaDonChoQuaHan() {
        List<DonHang> danhSach = donHangRepository.findHoaDonChoQuaHan(LocalDateTime.now());
        for (DonHang donHang : danhSach) {
            try {
                huyHoaDonCho(donHang.getId(), null);
                log.info("Tự động hủy hóa đơn chờ: {}", donHang.getMaDonHang());
            } catch (Exception e) {
                log.error("Lỗi khi tự hủy hóa đơn {}: {}", donHang.getMaDonHang(), e.getMessage());
            }
        }
    }

    /**
     * Job tự động hủy đơn VNPay quá 15 phút chưa thanh toán
     */
    @Scheduled(fixedDelay = 120000)
    public void tuDongHuyDonVNPayHetHan() {
        List<DonHang> danhSach = donHangRepository.findVNPayHetHan(LocalDateTime.now().minusMinutes(15));
        for (DonHang donHang : danhSach) {
            try {
                donHang.setTrangThaiDonHang(TrangThaiDonHang.DA_HUY);
                donHangRepository.save(donHang);
                ghiLichSuTrangThai(donHang, TrangThaiDonHang.CHO_THANH_TOAN, TrangThaiDonHang.DA_HUY, "Tự động hủy - quá hạn thanh toán VNPay", null);
                if (donHang.getKhuyenMai() != null) {
                    khuyenMaiService.giamSoLanSuDung(donHang.getKhuyenMai().getId());
                }
                if (donHang.getVoucher() != null) {
                    voucherService.giamSoLanSuDung(donHang.getVoucher().getMaVoucher());
                }
                log.info("Tự động hủy đơn VNPay hết hạn: {}", donHang.getMaDonHang());
            } catch (Exception e) {
                log.error("Lỗi khi tự hủy đơn VNPay {}: {}", donHang.getMaDonHang(), e.getMessage());
            }
        }
    }

    // ===================== HELPER =====================

    private void capNhatTongTienDonHang(DonHang donHang) {
        BigDecimal tong = donHangRepository.findById(donHang.getId())
                .map(dh -> dh.getDanhSachChiTiet() == null ? BigDecimal.ZERO :
                        dh.getDanhSachChiTiet().stream()
                                .map(ChiTietDonHang::getThanhTien)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);

        donHang.setTongTien(tong);
        donHang.setTongTienThanhToan(tong);
        donHangRepository.save(donHang);
    }

    private void ghiLichSuTrangThai(DonHang donHang, String trangThaiCu, String trangThaiMoi, String ghiChu, NguoiDung nguoiThayDoi) {
        LichSuTrangThaiDon ls = LichSuTrangThaiDon.builder()
                .donHang(donHang)
                .trangThaiCu(trangThaiCu)
                .trangThaiMoi(trangThaiMoi)
                .ghiChu(ghiChu)
                .nguoiThayDoi(nguoiThayDoi)
                .build();
        lichSuRepository.save(ls);
    }

    private void kiemTraLuongTrangThai(String hienTai, String tiepTheo, String phuongThucThanhToan) {
        boolean hop = false;

        if ("COD".equalsIgnoreCase(phuongThucThanhToan) || phuongThucThanhToan == null) {
            // COD workflow: Chờ → Xác nhận (TRỪ KHO) → Xử lý → Giao → Hoàn tất
            // Cancellation (DA_HUY) allowed from: CHO_XAC_NHAN, DA_XAC_NHAN, DANG_XU_LY, DANG_GIAO
            hop = switch (hienTai) {
                case TrangThaiDonHang.CHO_THANH_TOAN -> 
                    TrangThaiDonHang.CHO_XAC_NHAN.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.CHO_XAC_NHAN -> 
                    TrangThaiDonHang.DA_XAC_NHAN.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.DA_XAC_NHAN -> 
                    TrangThaiDonHang.DANG_XU_LY.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.DANG_XU_LY -> 
                    TrangThaiDonHang.DANG_GIAO.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.DANG_GIAO -> 
                    TrangThaiDonHang.HOAN_TAT.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                default -> false;
            };
        } else if ("VNPAY".equalsIgnoreCase(phuongThucThanhToan)) {
            // VNPAY workflow: Chờ TT → Đã TT → Chờ xác nhận → Xác nhận (TRỪ KHO) → Xử lý → Giao → Hoàn tất
            // Cancellation (DA_HUY) allowed from: DA_THANH_TOAN, CHO_XAC_NHAN, DA_XAC_NHAN, DANG_XU_LY, DANG_GIAO
            hop = switch (hienTai) {
                case TrangThaiDonHang.CHO_THANH_TOAN -> 
                    TrangThaiDonHang.DA_THANH_TOAN.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.DA_THANH_TOAN -> 
                    TrangThaiDonHang.CHO_XAC_NHAN.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.CHO_XAC_NHAN -> 
                    TrangThaiDonHang.DA_XAC_NHAN.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.DA_XAC_NHAN -> 
                    TrangThaiDonHang.DANG_XU_LY.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.DANG_XU_LY -> 
                    TrangThaiDonHang.DANG_GIAO.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                case TrangThaiDonHang.DANG_GIAO -> 
                    TrangThaiDonHang.HOAN_TAT.equals(tiepTheo) || TrangThaiDonHang.DA_HUY.equals(tiepTheo);
                default -> false;
            };
        }

        if (!hop) {
            throw new IllegalArgumentException("Không thể chuyển từ '" +
                    TrangThaiDonHang.layNhan(hienTai) + "' sang '" +
                    TrangThaiDonHang.layNhan(tiepTheo) + "'");
        }
    }

    public String sinhMaDonHang() {
        String thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String ngauNhien = String.format("%04d", new Random().nextInt(9999));
        return "DH" + thoiGian + ngauNhien;
    }

    private String buildDiaChiDayDu(String diaChiCuThe, String phuongXa, String quanHuyen, String tinhThanh) {
        StringBuilder sb = new StringBuilder();
        if (diaChiCuThe != null && !diaChiCuThe.isBlank()) sb.append(diaChiCuThe);
        if (phuongXa != null && !phuongXa.isBlank()) sb.append(", ").append(phuongXa);
        if (quanHuyen != null && !quanHuyen.isBlank()) sb.append(", ").append(quanHuyen);
        if (tinhThanh != null && !tinhThanh.isBlank()) sb.append(", ").append(tinhThanh);
        return sb.toString();
    }

    // ===================== VNPAY PAYMENT CONFIRMATION =====================

    /**
     * Xác nhận thanh toán VNPay từ callback gateway
     * - Idempotent: only process if status is CHO_THANH_TOAN
     * - Thread-safe: locks the order row while processing
     */
    @Transactional
    public DonHang xacNhanThanhToanVNPay(
            String maDonHang, String maGiaoDich, BigDecimal soTienThanhToan) {
        log.info("[VNPay] Xac nhan - Ma DH: {}, Ma giao dich: {}", maDonHang, maGiaoDich);
        
        DonHang donHang = donHangRepository.findByMaDonHangForUpdate(maDonHang)
                .orElseThrow(() -> {
                    log.error("[VNPay] Khong tim thay don hang: {}", maDonHang);
                    return new RuntimeException("Khong tim thay don hang: " + maDonHang);
                });
        
        if (soTienThanhToan == null
                || soTienThanhToan.compareTo(donHang.getTongTienThanhToan()) != 0) {
            throw new IllegalArgumentException("Số tiền VNPay không khớp với tổng tiền đơn hàng");
        }

        // Idempotency check - only process if waiting for payment
        String trangThaiHienTai = donHang.getTrangThaiDonHang();
        if (!TrangThaiDonHang.CHO_THANH_TOAN.equals(trangThaiHienTai)) {
            if (TrangThaiDonHang.DA_THANH_TOAN.equals(trangThaiHienTai)
                    && Objects.equals(donHang.getVnPayTransactionRef(), maGiaoDich)) {
                return donHang;
            }
            log.warn("[VNPay] Don hang khong o trang thai CHO_THANH_TOAN: {} (trang thai: {})", 
                    maDonHang, trangThaiHienTai);
            throw new IllegalStateException(
                    "Don hang da duoc xu ly hoac o trang thai khong hop le: " + trangThaiHienTai);
        }
        
        // Mark as paid
        donHang.setDaThanhToan(true);
        if (donHang.getVnPayTransactionRef() == null) {
            donHang.setVnPayTransactionRef(maGiaoDich);
        }
        donHang.setTrangThaiDonHang(TrangThaiDonHang.DA_THANH_TOAN);
        donHang = donHangRepository.save(donHang);
        log.info("[VNPay] Cap nhat thanh toan thanh cong - Trang thai: DA_THANH_TOAN: {}", maDonHang);
        
        // IMPORTANT: Do NOT deduct inventory here!
        // Inventory will be deducted LATER when admin confirms (DAXACNhan -> DA_XAC_NHAN)
        // This is proper business logic: VNPay payment only marks as paid, admin must confirm to process
        
        // Record status change history
        try {
            ghiLichSuTrangThai(donHang, TrangThaiDonHang.CHO_THANH_TOAN, TrangThaiDonHang.DA_THANH_TOAN,
                    "Thanh toan VNPay thanh cong. Ma giao dich: " + maGiaoDich, null);
        } catch (Exception e) {
            log.warn("[VNPay] Loi ghi lich su: {}", maDonHang, e);
        }
        
        // Send confirmation email (asynchronous, non-blocking)
        try {
            if (donHang.getNguoiDung() != null && donHang.getNguoiDung().getEmail() != null) {
                mailService.guiMailXacNhanDonHang(
                        donHang.getNguoiDung().getEmail(),
                        donHang.getNguoiDung().getHoTen(),
                        donHang.getMaDonHang(),
                        donHang.getTongTienThanhToan(),
                        donHang.getPhuongThucThanhToan()
                );
                log.info("[VNPay] Gui email xac nhan: {} -> {}", maDonHang, donHang.getNguoiDung().getEmail());
            }
        } catch (Exception e) {
            log.warn("[VNPay] Loi gui email: {}", maDonHang, e);
        }
        
        return donHang;
    }

    /**
     * Lấy trạng thái thanh toán đơn hàng - để frontend kiểm tra
     */
    @Transactional(readOnly = true)
    public Map<String, Object> layTrangThaiThanhToan(String maDonHang, Long nguoiDungId) {
        DonHang donHang = donHangRepository.findByMaDonHang(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (donHang.getNguoiDung() == null
                || !donHang.getNguoiDung().getId().equals(nguoiDungId)) {
            throw new SecurityException("Không có quyền xem trạng thái thanh toán của đơn hàng này");
        }
        
        return Map.of(
                "maDonHang", donHang.getMaDonHang(),
                "trangThai", donHang.getTrangThaiDonHang(),
                "daThanhToan", donHang.getDaThanhToan(),
                "phuongThuc", donHang.getPhuongThucThanhToan(),
                "tongTienThanhToan", donHang.getTongTienThanhToan(),
                "maGiaoDichVNPay", donHang.getVnPayTransactionRef() != null ? donHang.getVnPayTransactionRef() : "N/A",
                "thoiGianTao", donHang.getThoiGianTao()
        );
    }

    // ===================== PHƯƠNG THỨC ĐÃ THU GỌN CHO CONTROLLER =====================


    /** Tạo hóa đơn chờ - nhận nhanVienId */
    public DonHang taoHoaDonCho(Long nhanVienId) {
        NguoiDung nv = nguoiDungRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        return taoHoaDonCho(nv);
    }

    /** Thêm sản phẩm vào hóa đơn tại quầy - nhận nhanVienId */
    public DonHang themSanPhamVaoHoaDonTaiQuay(Long donHangId, Long bienTheId, int soLuong, Long nhanVienId) {
        return themSanPhamVaoHoaDonTaiQuay(donHangId, bienTheId, soLuong);
    }

    /** Cập nhật số lượng chi tiết tại quầy */
    public void capNhatSoLuongTaiQuay(Long chiTietId, int soLuongMoi, Long nhanVienId) {
        ChiTietDonHang ct = chiTietDonHangRepository.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));
        BienThe bt = ct.getBienThe();
        int chenhLech = soLuongMoi - ct.getSoLuong();
        if (chenhLech > 0 && bt.getSoLuongTon() < chenhLech) {
            throw new IllegalArgumentException("Không đủ tồn kho (còn " + bt.getSoLuongTon() + ")");
        }
        bt.setSoLuongTon(bt.getSoLuongTon() - chenhLech);
        bienTheRepository.save(bt);
        ct.setSoLuong(soLuongMoi);
        chiTietDonHangRepository.save(ct);
        capNhatTongTienDonHang(ct.getDonHang());
    }

    /** Xóa chi tiết khỏi hóa đơn tại quầy */
    public void xoaChiTietTaiQuay(Long chiTietId, Long nhanVienId) {
        ChiTietDonHang ct = chiTietDonHangRepository.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));
        DonHang donHang = ct.getDonHang();
        BienThe bt = ct.getBienThe();
        bt.setSoLuongTon(bt.getSoLuongTon() + ct.getSoLuong());
        bienTheRepository.save(bt);
        chiTietDonHangRepository.delete(ct);
        capNhatTongTienDonHang(donHang);
    }

    /** Áp voucher vào hóa đơn tại quầy */
    public void apVoucherVaoHoaDonTaiQuay(Long donHangId, Long khuyenMaiId, BigDecimal soTienGiam) {
        DonHang dh = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        KhuyenMai km = khuyenMaiService.timTheoId(khuyenMaiId).orElse(null);
        dh.setKhuyenMai(km);
        dh.setSoTienGiamGia(soTienGiam);
        BigDecimal tong = dh.getTongTien().subtract(soTienGiam);
        dh.setTongTienThanhToan(tong.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : tong);
        donHangRepository.save(dh);
    }

    /** Xóa voucher khỏi hóa đơn tại quầy */
    public void xoaVoucherKhoiHoaDonTaiQuay(Long donHangId) {
        DonHang dh = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        dh.setKhuyenMai(null);
        dh.setSoTienGiamGia(BigDecimal.ZERO);
        dh.setTongTienThanhToan(dh.getTongTien());
        donHangRepository.save(dh);
    }

    /** Thanh toán tại quầy - đơn giản hóa signature */
    public void thanhToanTaiQuay(Long donHangId, String phuongThuc, BigDecimal tienKhachDua,
                                  String tenKhach, String sdtKhach, Long nhanVienId) {
        NguoiDung nv = nguoiDungRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        thanhToanTaiQuay(donHangId, phuongThuc, null, nv, null,
                tenKhach, sdtKhach, null);
    }

    /** Hủy đơn hàng bởi nhân viên - wrapper để DonHangQLController gọi */
    public void nhanVienHuyDon(Long donHangId, String lyDo, Long nhanVienId, boolean _ignored) {
        nhanVienHuyDon(donHangId, lyDo, nhanVienId);
    }

    /** Tìm kiếm đơn hàng cho admin */
    @Transactional(readOnly = true)
    public Page<DonHang> timKiemDonHangAdmin(String q, String trangThai, String phuongThuc, Pageable pageable) {
        return donHangRepository.timKiemDonHang(q, trangThai, phuongThuc, null, null, pageable);
    }

    /** Chuyển trạng thái đơn hàng - wrapper tương thích */
    public DonHang chuyenTrangThai(Long donHangId, String trangThaiMoi, String ghiChu, NguoiDung nguoiDung) {
        return chuyenTrangThai(donHangId, trangThaiMoi, ghiChu, nguoiDung.getId());
    }
}

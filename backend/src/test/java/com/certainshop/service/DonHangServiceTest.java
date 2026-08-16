package com.certainshop.service;

import com.certainshop.constant.TrangThaiDonHang;
import com.certainshop.dto.DatHangDto;
import com.certainshop.entity.BienThe;
import com.certainshop.entity.ChiTietDonHang;
import com.certainshop.entity.DonHang;
import com.certainshop.entity.GioHang;
import com.certainshop.entity.GioHangChiTiet;
import com.certainshop.entity.NguoiDung;
import com.certainshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DonHangServiceTest {

    private final DonHangRepository donHangRepository = mock(DonHangRepository.class);
    private final ChiTietDonHangRepository chiTietRepository = mock(ChiTietDonHangRepository.class);
    private final BienTheRepository bienTheRepository = mock(BienTheRepository.class);
    private final NguoiDungRepository nguoiDungRepository = mock(NguoiDungRepository.class);
    private final LichSuTrangThaiDonRepository lichSuRepository = mock(LichSuTrangThaiDonRepository.class);
    private final GioHangRepository gioHangRepository = mock(GioHangRepository.class);
    private final GioHangChiTietRepository gioHangChiTietRepository = mock(GioHangChiTietRepository.class);
    private final KhuyenMaiService khuyenMaiService = mock(KhuyenMaiService.class);
    private final VoucherService voucherService = mock(VoucherService.class);
    private final DiaChiNguoiDungRepository diaChiRepository = mock(DiaChiNguoiDungRepository.class);
    private final MailService mailService = mock(MailService.class);
    private final GHNApiService ghnApiService = mock(GHNApiService.class);
    private DonHangService service;

    @BeforeEach
    void setUp() {
        service = new DonHangService(donHangRepository, chiTietRepository, bienTheRepository,
                nguoiDungRepository, lichSuRepository, gioHangRepository,
                gioHangChiTietRepository, khuyenMaiService, voucherService,
                diaChiRepository, mailService, ghnApiService);
    }

    @Test
    void paidOrderCannotBeCancelledWithoutRefund() {
        NguoiDung owner = NguoiDung.builder().id(1L).build();
        BienThe variant = BienThe.builder().id(5L).soLuongTon(10).build();
        ChiTietDonHang item = ChiTietDonHang.builder().bienThe(variant).soLuong(2).build();
        DonHang order = DonHang.builder()
                .id(9L)
                .nguoiDung(owner)
                .daThanhToan(true)
                .trangThaiDonHang(TrangThaiDonHang.DA_THANH_TOAN)
                .danhSachChiTiet(List.of(item))
                .build();
        when(donHangRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(order));
        when(donHangRepository.save(order)).thenReturn(order);

        assertThrows(IllegalArgumentException.class,
                () -> service.khachHuyDon(9L, "changed mind", 1L));

        assertEquals(10, variant.getSoLuongTon());
        verify(bienTheRepository, never()).save(any());
        verify(donHangRepository, never()).save(any());
        assertEquals(TrangThaiDonHang.DA_THANH_TOAN, order.getTrangThaiDonHang());
    }

    @Test
    void customerCannotCancelAnotherUsersOrder() {
        DonHang order = DonHang.builder()
                .id(9L)
                .nguoiDung(NguoiDung.builder().id(1L).build())
                .trangThaiDonHang(TrangThaiDonHang.CHO_XAC_NHAN)
                .build();
        when(donHangRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(order));

        assertThrows(SecurityException.class,
                () -> service.khachHuyDon(9L, "not mine", 2L));
        verify(donHangRepository, never()).save(any());
    }

    @Test
    void statusTransitionCannotBypassRefundRequirement() {
        DonHang order = DonHang.builder()
                .id(9L)
                .daThanhToan(true)
                .trangThaiDonHang(TrangThaiDonHang.CHO_XAC_NHAN)
                .build();
        when(donHangRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class,
                () -> service.chuyenTrangThai(9L, TrangThaiDonHang.DA_HUY, "cancel", 3L));
        verify(donHangRepository, never()).save(any());
    }

    @Test
    void rejectsVnPayAmountThatDoesNotMatchOrderTotal() {
        DonHang order = DonHang.builder()
                .maDonHang("DH1")
                .tongTienThanhToan(BigDecimal.valueOf(100_000))
                .trangThaiDonHang(TrangThaiDonHang.CHO_THANH_TOAN)
                .build();
        when(donHangRepository.findByMaDonHangForUpdate("DH1")).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class,
                () -> service.xacNhanThanhToanVNPay("DH1", "TX1", BigDecimal.valueOf(1_000)));
        verify(donHangRepository, never()).save(any());
    }

    @Test
    void rejectsSavedAddressThatDoesNotBelongToCurrentUser() {
        NguoiDung user = NguoiDung.builder().id(1L).build();
        GioHangChiTiet item = GioHangChiTiet.builder()
                .soLuong(1)
                .donGia(BigDecimal.valueOf(100_000))
                .build();
        GioHang cart = GioHang.builder()
                .danhSachChiTiet(new ArrayList<>(List.of(item)))
                .build();
        DatHangDto dto = DatHangDto.builder()
                .diaChiId(99L)
                .tenNguoiNhan("Bao Nguyen")
                .soDienThoai("0900000000")
                .diaChiCuThe("Ha Noi")
                .phuongThucThanhToan("COD")
                .build();
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gioHangRepository.findByNguoiDungId(1L)).thenReturn(Optional.of(cart));
        when(diaChiRepository.findByIdAndNguoiDungId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(SecurityException.class, () -> service.datHangOnline(1L, dto));
        verify(donHangRepository, never()).save(any());
        verify(ghnApiService, never()).tinhPhiVanChuyen(anyInt(), anyString(), anyInt());
    }
}

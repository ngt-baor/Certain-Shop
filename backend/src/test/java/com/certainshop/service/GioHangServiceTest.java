package com.certainshop.service;

import com.certainshop.entity.GioHang;
import com.certainshop.entity.GioHangChiTiet;
import com.certainshop.entity.NguoiDung;
import com.certainshop.repository.BienTheRepository;
import com.certainshop.repository.GioHangChiTietRepository;
import com.certainshop.repository.GioHangRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GioHangServiceTest {

    private final GioHangRepository gioHangRepository = mock(GioHangRepository.class);
    private final GioHangChiTietRepository chiTietRepository = mock(GioHangChiTietRepository.class);
    private final BienTheRepository bienTheRepository = mock(BienTheRepository.class);
    private final GioHangService service = new GioHangService(
            gioHangRepository, chiTietRepository, bienTheRepository);

    @Test
    void rejectsUpdatingAndDeletingAnotherUsersCartItem() {
        NguoiDung owner = NguoiDung.builder().id(1L).build();
        GioHang cart = GioHang.builder().nguoiDung(owner).build();
        GioHangChiTiet item = GioHangChiTiet.builder().id(10L).gioHang(cart).build();
        when(chiTietRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(SecurityException.class,
                () -> service.capNhatSoLuong(10L, 2, 2L));
        assertThrows(SecurityException.class,
                () -> service.xoaKhoiGio(10L, 2L));

        verify(chiTietRepository, never()).save(any());
        verify(chiTietRepository, never()).delete(any());
    }
}

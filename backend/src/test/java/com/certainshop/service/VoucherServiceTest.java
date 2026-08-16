package com.certainshop.service;

import com.certainshop.entity.Voucher;
import com.certainshop.repository.VoucherRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VoucherServiceTest {

    private final VoucherRepository repository = mock(VoucherRepository.class);
    private final VoucherService service = new VoucherService(repository);

    @Test
    void acceptsFixedDiscountGreaterThanOneHundred() {
        Voucher voucher = validVoucher("fixed200k", "FIXED", "200000");
        when(repository.existsByMaVoucher("FIXED200K")).thenReturn(false);
        when(repository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Voucher saved = service.taoVoucher(voucher);

        assertEquals("FIXED200K", saved.getMaVoucher());
        assertEquals(new BigDecimal("200000"), saved.getGiaTriGiam());
    }

    @Test
    void rejectsPercentAboveOneHundred() {
        Voucher voucher = validVoucher("OVER100", "PERCENT", "101");
        assertThrows(IllegalArgumentException.class, () -> service.taoVoucher(voucher));
    }

    @Test
    void rejectsInvalidDateRange() {
        Voucher voucher = validVoucher("DATES", "PERCENT", "10");
        voucher.setNgayKetThuc(voucher.getNgayBatDau());
        assertThrows(IllegalArgumentException.class, () -> service.taoVoucher(voucher));
    }

    private Voucher validVoucher(String code, String type, String discount) {
        LocalDateTime start = LocalDateTime.now().plusMinutes(1);
        return Voucher.builder()
                .maVoucher(code)
                .ngayBatDau(start)
                .ngayKetThuc(start.plusDays(7))
                .giaTriToiThieu(BigDecimal.ZERO)
                .giaTriGiamToiDa(new BigDecimal("200000"))
                .loaiGiam(type)
                .giaTriGiam(new BigDecimal(discount))
                .soLuongToiDa(100)
                .trangThai(true)
                .build();
    }
}

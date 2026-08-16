package com.certainshop.service;

import com.certainshop.entity.NguoiDung;
import com.certainshop.entity.VaiTro;
import com.certainshop.repository.GioHangRepository;
import com.certainshop.repository.NguoiDungRepository;
import com.certainshop.repository.VaiTroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NguoiDungServiceTest {

    @Mock NguoiDungRepository nguoiDungRepository;
    @Mock VaiTroRepository vaiTroRepository;
    @Mock GioHangRepository gioHangRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock MailService mailService;
    @InjectMocks NguoiDungService service;

    @Test
    void rejectsReusingCurrentPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.doiMatKhau(1L, "secret123", "secret123"));
    }

    @Test
    void changesPasswordAfterCheckingCurrentPassword() {
        NguoiDung user = NguoiDung.builder().id(7L).matKhauMaHoa("old-hash").build();
        when(nguoiDungRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-secret", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");

        service.doiMatKhau(7L, "old-secret", "new-secret");

        assertEquals("new-hash", user.getMatKhauMaHoa());
        verify(nguoiDungRepository).save(user);
    }

    @Test
    void defaultStaffCreationResolvesRoleByName() {
        NguoiDung staff = NguoiDung.builder().tenDangNhap("staff01").email("staff@example.com").build();
        VaiTro role = VaiTro.builder().tenVaiTro("NHAN_VIEN").build();
        when(vaiTroRepository.findByTenVaiTro("NHAN_VIEN")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secret123")).thenReturn("hash");
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NguoiDung saved = service.taoNhanVien(staff, "secret123", null);

        assertEquals(role, saved.getVaiTro());
        assertEquals("hash", saved.getMatKhauMaHoa());
    }
}

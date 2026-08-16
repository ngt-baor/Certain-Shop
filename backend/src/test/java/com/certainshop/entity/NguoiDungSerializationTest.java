package com.certainshop.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class NguoiDungSerializationTest {

    @Test
    void omitsPasswordAndResetTokenFromJson() throws Exception {
        NguoiDung user = NguoiDung.builder()
                .id(1L)
                .tenDangNhap("bao")
                .matKhauMaHoa("password-hash")
                .maDatLaiMatKhau("reset-token")
                .build();

        String json = new ObjectMapper().writeValueAsString(user);

        assertFalse(json.contains("matKhauMaHoa"));
        assertFalse(json.contains("maDatLaiMatKhau"));
        assertFalse(json.contains("password-hash"));
        assertFalse(json.contains("reset-token"));
    }
}

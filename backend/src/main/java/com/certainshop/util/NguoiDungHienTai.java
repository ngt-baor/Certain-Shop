package com.certainshop.util;

import com.certainshop.entity.NguoiDung;
import com.certainshop.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NguoiDungHienTai {

    private final NguoiDungRepository nguoiDungRepository;

    public Optional<NguoiDung> lay() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return nguoiDungRepository.findByTenDangNhap(auth.getName());
    }

    public NguoiDung layBatBuoc() {
        return lay().orElseThrow(() -> new RuntimeException("Chưa đăng nhập"));
    }

    public boolean daDangNhap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
}

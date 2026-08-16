package com.certainshop.config;

import com.certainshop.entity.NguoiDung;
import com.certainshop.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class BaoMatConfig {

    private final NguoiDungRepository nguoiDungRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public BaoMatConfig(NguoiDungRepository nguoiDungRepository,
                        @org.springframework.context.annotation.Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Value("${app.cors.allowedOrigins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder boMaHoaMatKhau() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService danhSachNguoiDung() {
        return tenDangNhap -> {
            NguoiDung nd = nguoiDungRepository.findByTenDangNhap(tenDangNhap)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + tenDangNhap));

            if (Boolean.FALSE.equals(nd.getDangHoatDong())) {
                throw new UsernameNotFoundException("Tài khoản đã bị khoá");
            }

            // Normalize Vietnamese role name to ASCII (e.g., "Nhân viên" → "NHAN_VIEN")
            String tenVaiTro = nd.getVaiTro().getTenVaiTro();
            tenVaiTro = java.text.Normalizer.normalize(tenVaiTro, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                    .replace("đ", "d").replace("Đ", "D")
                    .toUpperCase().replace(" ", "_");

            return User.builder()
                    .username(nd.getTenDangNhap())
                    .password(nd.getMatKhauMaHoa())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + tenVaiTro)))
                    .build();
        };
    }

    @Bean
    public DaoAuthenticationProvider nhaXacThuc() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(danhSachNguoiDung());
        provider.setPasswordEncoder(boMaHoaMatKhau());
        return provider;
    }

    @Bean
    public AuthenticationManager quanLyXacThuc(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(Arrays.asList("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain chuoiBoLocBaoMat(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(nhaXacThuc())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - không cần đăng nhập
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/api/auth/**",
                    "/api/san-pham/**",
                    "/api/danh-muc/**",
                    "/api/thuong-hieu/**",
                    "/api/tim-kiem/**",
                    "/api/ghn/**",
                    "/api/dia-chi/tinh-thanh",
                    "/api/dia-chi/quan-huyen/**",
                    "/api/dia-chi/phuong-xa/**",
                    "/api/vnpay-return",
                    // Thymeleaf legacy (giữ lại để không conflict)
                    "/", "/trang-chu", "/san-pham/**", "/danh-muc/**", "/tim-kiem",
                    "/dang-nhap", "/dang-ky", "/css/**", "/js/**", "/img/**",
                    "/uploads/**", "/thanh-toan/vnpay-return", "/error"
                ).permitAll()

                // Khách hàng
                .requestMatchers("/api/gio-hang/**", "/api/dat-hang/**",
                        "/api/don-hang/cua-toi/**", "/api/tai-khoan/**",
                        "/api/dia-chi/**").hasAnyRole("KHACH_HANG", "ADMIN", "NHAN_VIEN")

                // Admin + Nhân viên
                .requestMatchers("/api/quan-ly/**").hasAnyRole("ADMIN", "NHAN_VIEN")

                // Chỉ Admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Legacy Thymeleaf routes
                .requestMatchers("/gio-hang/**", "/dat-hang/**", "/tai-khoan/**",
                        "/don-hang/cua-toi/**").hasAnyRole("KHACH_HANG", "ADMIN", "NHAN_VIEN")
                .requestMatchers("/quan-ly/**").hasAnyRole("ADMIN", "NHAN_VIEN")

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setContentType("application/json;charset=UTF-8");
                    res.setStatus(401);
                    res.getWriter().write("{\"loi\":\"Chưa đăng nhập hoặc token hết hạn\",\"maLoi\":401}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setContentType("application/json;charset=UTF-8");
                    res.setStatus(403);
                    res.getWriter().write("{\"loi\":\"Không có quyền truy cập\",\"maLoi\":403}");
                })
            );

        return http.build();
    }
}

package com.certainshop.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO chi tiết địa chỉ của khách hàng
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiaChiChiTietDto {
    
    private Long id;
    private String hoTen;
    private String soDienThoai;
    private String diaChiDong1; // Số nhà, đường
    private String phuongXa;
    private String quanHuyen;
    private String tinhThanh;
    
    // Mã GHN
    private Integer maTinhGHN;
    private Integer maHuyenGHN;
    private String maXaGHN;
    
    private Boolean laMacDinh;
    private LocalDateTime thoiGianTao;
    
    // Phương thức tiện lợi
    public String getDiaChiDayDu() {
        StringBuilder sb = new StringBuilder();
        if (diaChiDong1 != null && !diaChiDong1.isBlank()) sb.append(diaChiDong1);
        if (phuongXa != null && !phuongXa.isBlank()) sb.append(", ").append(phuongXa);
        if (quanHuyen != null && !quanHuyen.isBlank()) sb.append(", ").append(quanHuyen);
        if (tinhThanh != null && !tinhThanh.isBlank()) sb.append(", ").append(tinhThanh);
        return sb.toString();
    }
}

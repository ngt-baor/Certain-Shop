package com.certainshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "HinhAnhBienThe")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HinhAnhBienThe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BienTheId", nullable = false)
    @JsonIgnore
    private BienThe bienThe;

    @Column(name = "DuongDan", nullable = false, length = 255)
    private String duongDan;

    @Column(name = "LaAnhChinh")
    private Boolean laAnhChinh = false;

    @Column(name = "ThuTu")
    private Integer thuTu = 0;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @PrePersist
    protected void truocKhiTao() {
        thoiGianTao = LocalDateTime.now();
    }
}

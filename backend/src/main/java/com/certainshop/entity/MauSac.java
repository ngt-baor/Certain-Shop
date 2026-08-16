package com.certainshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MauSac")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MauSac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenMauSac", length = 100)
    private String tenMau;

    @Column(name = "MaHex", length = 10)
    private String maHex;

    @Column(name = "MoTa", length = 255)
    private String moTa;
}

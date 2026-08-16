package com.certainshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "KichThuoc")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KichThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenKichThuoc", length = 50)
    private String kichCo; // kept as kichCo for backward compat

    @Column(name = "ThuTu")
    private Integer thuTu = 0;
}

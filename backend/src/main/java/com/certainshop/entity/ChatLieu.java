package com.certainshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ChatLieu")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatLieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenChatLieu", length = 100)
    private String tenChatLieu;

    @Column(name = "MoTa", length = 500)
    private String moTa;
}

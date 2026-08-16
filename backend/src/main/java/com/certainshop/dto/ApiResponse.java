package com.certainshop.dto;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ApiResponse<T> {
    private boolean thanhCong;
    private String thongBao;
    private T duLieu;
    private int maLoi = 0;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().thanhCong(true).duLieu(data).build();
    }

    public static <T> ApiResponse<T> ok(String msg, T data) {
        return ApiResponse.<T>builder().thanhCong(true).thongBao(msg).duLieu(data).build();
    }

    public static <T> ApiResponse<T> loi(String msg) {
        return ApiResponse.<T>builder().thanhCong(false).thongBao(msg).build();
    }

    public static <T> ApiResponse<T> loi(int code, String msg) {
        return ApiResponse.<T>builder().thanhCong(false).maLoi(code).thongBao(msg).build();
    }
}

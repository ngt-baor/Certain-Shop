package com.certainshop.constant;

/**
 * Hằng số trạng thái đơn hàng
 */
public final class TrangThaiDonHang {

    private TrangThaiDonHang() {}

    // ===== COD =====
    /** Mới tạo */
    public static final String MOI_TAO = "MOI_TAO";
    /** Chờ xác nhận */
    public static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    /** Đã xác nhận (trừ kho) */
    public static final String DA_XAC_NHAN = "DA_XAC_NHAN";
    /** Đang xử lý */
    public static final String DANG_XU_LY = "DANG_XU_LY";
    /** Đang giao */
    public static final String DANG_GIAO = "DANG_GIAO";
    /** Hoàn tất */
    public static final String HOAN_TAT = "HOAN_TAT";
    /** Đã hủy */
    public static final String DA_HUY = "DA_HUY";

    // ===== VNPAY =====
    /** Chờ thanh toán */
    public static final String CHO_THANH_TOAN = "CHO_THANH_TOAN";
    /** Đã thanh toán (trừ kho) */
    public static final String DA_THANH_TOAN = "DA_THANH_TOAN";

    // ===== TẠI QUẦY =====
    /** Hóa đơn chờ - tại quầy */
    public static final String HOA_DON_CHO = "HOA_DON_CHO";

    /**
     * Lấy nhãn hiển thị cho trạng thái
     */
    public static String layNhan(String trangThai) {
        return switch (trangThai) {
            case MOI_TAO -> "Mới tạo";
            case CHO_XAC_NHAN -> "Chờ xác nhận";
            case DA_XAC_NHAN -> "Đã xác nhận";
            case DANG_XU_LY -> "Đang xử lý";
            case DANG_GIAO -> "Đang giao";
            case HOAN_TAT -> "Hoàn tất";
            case DA_HUY -> "Đã hủy";
            case CHO_THANH_TOAN -> "Chờ thanh toán";
            case DA_THANH_TOAN -> "Đã thanh toán";
            case HOA_DON_CHO -> "Hóa đơn chờ";
            default -> trangThai;
        };
    }

    /**
     * Lấy màu CSS cho trạng thái
     */
    public static String layMauCss(String trangThai) {
        return switch (trangThai) {
            case MOI_TAO -> "secondary";
            case CHO_XAC_NHAN -> "warning";
            case DA_XAC_NHAN -> "info";
            case DANG_XU_LY -> "primary";
            case DANG_GIAO -> "primary";
            case HOAN_TAT -> "success";
            case DA_HUY -> "danger";
            case CHO_THANH_TOAN -> "warning";
            case DA_THANH_TOAN -> "success";
            case HOA_DON_CHO -> "secondary";
            default -> "secondary";
        };
    }
}

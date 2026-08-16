package com.certainshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;

/**
 * Service gửi email bất đồng bộ (@Async).
 * Tất cả phương thức public đều chạy trên thread pool riêng,
 * không block luồng xử lý chính — người dùng không phải chờ.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.shop.ten:Certain Shop}")
    private String shopName;

    // ================================================================
    //  PRIVATE HELPERS
    // ================================================================

    private boolean hopLe(String email) {
        return email != null && !email.isBlank() && email.contains("@");
    }

    private String formatTien(BigDecimal so) {
        if (so == null) return "0 ₫";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(so) + " ₫";
    }

    private String tenTrangThai(String code) {
        if (code == null) return "";
        return switch (code) {
            case "CHO_XAC_NHAN"  -> "⏳ Chờ xác nhận";
            case "DA_XAC_NHAN"   -> "✅ Đã xác nhận";
            case "DANG_XU_LY"    -> "⚙️ Đang xử lý";
            case "DANG_GIAO"     -> "🚚 Đang giao hàng";
            case "HOAN_TAT"      -> "🎉 Hoàn tất";
            case "DA_HUY"        -> "❌ Đã hủy";
            case "DA_THANH_TOAN" -> "💳 Đã thanh toán (VNPay)";
            default              -> code;
        };
    }

    /** Khung HTML chuẩn thương hiệu (không dùng String.formatted để tránh xung đột % trong CSS) */
    private String khuonHtml(String tieuDe, String noiDung) {
        return "<div style=\"font-family:'Segoe UI',Arial,sans-serif;max-width:620px;margin:0 auto;"
             + "background:#ffffff;border:1px solid #e5e7eb;border-radius:10px;overflow:hidden;\">"
             + "<div style=\"background:linear-gradient(135deg,#4f46e5 0%,#7c3aed 100%);"
             + "padding:28px 30px;text-align:center;\">"
             + "<h1 style=\"color:#ffffff;margin:0;font-size:24px;letter-spacing:1px;\">" + shopName + "</h1>"
             + "<p style=\"color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:13px;\">Chăm sóc khách hàng 24/7</p>"
             + "</div>"
             + "<div style=\"padding:32px 30px;\">"
             + "<h2 style=\"color:#1f2937;margin-top:0;border-bottom:2px solid #f3f4f6;padding-bottom:12px;\">"
             + tieuDe + "</h2>"
             + noiDung
             + "</div>"
             + "<div style=\"background:#f9fafb;border-top:1px solid #e5e7eb;padding:16px 30px;text-align:center;\">"
             + "<p style=\"color:#9ca3af;font-size:12px;margin:0;\">"
             + "&copy; 2025 " + shopName + " &nbsp;&middot;&nbsp; Cảm ơn bạn đã tin tưởng mua sắm!</p>"
             + "</div>"
             + "</div>";
    }

    private String dongBang(String nhan, String giaTri, boolean vien) {
        String b = vien ? "border-top:1px solid #e2e8f0;" : "";
        return "<tr>"
             + "<td style=\"color:#64748b;padding:9px 0;" + b + "\">" + nhan + "</td>"
             + "<td style=\"text-align:right;padding:9px 0;" + b + "\">" + giaTri + "</td>"
             + "</tr>";
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(fromEmail);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            mailSender.send(msg);
            log.info("[Mail] OK -> {} | {}", to, subject);
        } catch (Exception e) {
            log.error("[Mail] FAIL -> {} | {} | {}", to, subject, e.getMessage());
        }
    }

    // ================================================================
    //  1. CHÀO MỪNG ĐĂNG KÝ TÀI KHOẢN
    // ================================================================

    @Async
    public void guiMailChaoMung(String toEmail, String hoTen, String tenDangNhap) {
        if (!hopLe(toEmail)) {
            log.warn("[Mail] Bo qua chao mung - email khong hop le (user={})", tenDangNhap);
            return;
        }
        String ten = (hoTen != null && !hoTen.isBlank()) ? hoTen : tenDangNhap;
        String noiDung = "<p style=\"font-size:15px;\">Xin chào <strong>" + ten + "</strong>,</p>"
            + "<p>Tài khoản tại <strong>" + shopName + "</strong> đã được tạo thành công! "
            + "Chào mừng bạn gia nhập cộng đồng mua sắm của chúng tôi.</p>"
            + "<div style=\"background:#f0fdf4;border-left:4px solid #22c55e;"
            + "padding:16px 20px;border-radius:0 6px 6px 0;margin:24px 0;\">"
            + "<p style=\"margin:0;font-size:14px;\"><strong>Tên đăng nhập:</strong> <code>"
            + tenDangNhap + "</code></p>"
            + "</div>"
            + "<p>Bạn đã có thể mua sắm ngay với hàng nghìn sản phẩm chất lượng đang chờ bạn khám phá.</p>"
            + "<p style=\"color:#9ca3af;font-size:12px;margin-top:24px;\">"
            + "Nếu bạn không thực hiện đăng ký này, hãy bỏ qua email này.</p>";
        send(toEmail,
             "Chào mừng bạn đến với " + shopName + "!",
             khuonHtml("Đăng ký thành công!", noiDung));
    }

    // ================================================================
    //  2. XÁC NHẬN ĐẶT HÀNG — CHỈ DÙNG CHO ĐƠN COD
    // ================================================================

    /**
     * Chỉ gọi cho đơn COD. Đơn VNPay dùng guiMailXacNhanThanhToanVNPay sau callback.
     */
    @Async
    public void guiMailXacNhanDonHang(String toEmail, String hoTen, String maDonHang,
                                       BigDecimal tongTien, String phuongThucThanhToan) {
        if (!hopLe(toEmail)) {
            log.warn("[Mail] Bo qua xac nhan don {} - email khong hop le", maDonHang);
            return;
        }
        String ten = (hoTen != null && !hoTen.isBlank()) ? hoTen : "Quý khách";
        String noiDung = "<p style=\"font-size:15px;\">Xin chào <strong>" + ten + "</strong>,</p>"
            + "<p>Cảm ơn bạn đã đặt hàng tại <strong>" + shopName + "</strong>. "
            + "Đơn hàng đã được tiếp nhận thành công.</p>"
            + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin:24px 0;\">"
            + "<table style=\"width:100%;border-collapse:collapse;\">"
            + dongBang("Mã đơn hàng",
                "<span style=\"font-weight:700;color:#4f46e5;\">#" + maDonHang + "</span>", false)
            + dongBang("Tổng thanh toán",
                "<span style=\"font-weight:700;color:#16a34a;\">" + formatTien(tongTien) + "</span>", true)
            + dongBang("Phương thức", "Thanh toán khi nhận hàng (COD)", true)
            + dongBang("Trạng thái",
                "<span style=\"color:#d97706;font-weight:600;\">⏳ Chờ xác nhận</span>", true)
            + "</table></div>"
            + "<p style=\"color:#6b7280;font-size:14px;\">Chúng tôi sẽ xác nhận và xử lý đơn hàng "
            + "trong thời gian sớm nhất. Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.</p>";
        send(toEmail,
             "[" + shopName + "] Đặt hàng thành công #" + maDonHang,
             khuonHtml("Đơn hàng đã được nhận!", noiDung));
    }

    // ================================================================
    //  3. XÁC NHẬN THANH TOÁN VNPAY
    // ================================================================

    /**
     * Gửi email sau khi VNPay callback trả về thành công.
     *
     * @param maGiaoDich vnp_TransactionNo từ VNPay
     */
    @Async
    public void guiMailXacNhanThanhToanVNPay(String toEmail, String hoTen, String maDonHang,
                                              BigDecimal tongTien, String maGiaoDich) {
        if (!hopLe(toEmail)) {
            log.warn("[Mail] Bo qua xac nhan VNPay don {} - email khong hop le", maDonHang);
            return;
        }
        String ten = (hoTen != null && !hoTen.isBlank()) ? hoTen : "Quý khách";
        String maGD = (maGiaoDich != null && !maGiaoDich.isBlank()) ? maGiaoDich : "N/A";
        String noiDung = "<p style=\"font-size:15px;\">Xin chào <strong>" + ten + "</strong>,</p>"
            + "<p>Thanh toán VNPay cho đơn hàng của bạn đã được xác nhận thành công!</p>"
            + "<div style=\"background:#f0fdf4;border:1px solid #86efac;border-radius:8px;padding:20px;margin:24px 0;\">"
            + "<table style=\"width:100%;border-collapse:collapse;\">"
            + dongBang("Mã đơn hàng",
                "<span style=\"font-weight:700;color:#4f46e5;\">#" + maDonHang + "</span>", false)
            + dongBang("Số tiền",
                "<span style=\"font-weight:700;color:#16a34a;\">" + formatTien(tongTien) + "</span>", true)
            + dongBang("Mã giao dịch VNPay",
                "<code style=\"font-size:12px;\">" + maGD + "</code>", true)
            + dongBang("Trạng thái",
                "<span style=\"color:#16a34a;font-weight:600;\">✅ Đã thanh toán</span>", true)
            + "</table></div>"
            + "<p>Đơn hàng đang được chuẩn bị và sẽ sớm được giao đến tay bạn.</p>";
        send(toEmail,
             "[" + shopName + "] Thanh toán thành công #" + maDonHang,
             khuonHtml("Thanh toán VNPay thành công!", noiDung));
    }

    // ================================================================
    //  4. CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
    // ================================================================

    /**
     * Chỉ gửi cho: DA_XAC_NHAN, DANG_XU_LY, DANG_GIAO, HOAN_TAT.
     */
    @Async
    public void guiMailCapNhatTrangThai(String toEmail, String hoTen, String maDonHang,
                                         String trangThaiMoi) {
        if (!hopLe(toEmail)) return;
        if (!Set.of("DA_XAC_NHAN", "DANG_XU_LY", "DANG_GIAO", "HOAN_TAT").contains(trangThaiMoi)) return;

        String ten = (hoTen != null && !hoTen.isBlank()) ? hoTen : "Quý khách";
        String label = tenTrangThai(trangThaiMoi);
        String moTa = switch (trangThaiMoi) {
            case "DA_XAC_NHAN" -> "Đơn hàng đã được xác nhận và chuẩn bị đưa vào xử lý.";
            case "DANG_XU_LY"  -> "Chúng tôi đang đóng gói và chuẩn bị hàng cho bạn.";
            case "DANG_GIAO"   -> "Đơn hàng đang trên đường vận chuyển. Vui lòng chú ý điện thoại để nhận hàng.";
            case "HOAN_TAT"    -> "Đơn hàng đã giao thành công! Cảm ơn bạn đã tin tưởng mua sắm tại " + shopName + ".";
            default            -> "Trạng thái đơn hàng đã được cập nhật.";
        };
        boolean laHoanTat = "HOAN_TAT".equals(trangThaiMoi);
        String mauNen  = laHoanTat ? "#f0fdf4" : "#eff6ff";
        String mauVien = laHoanTat ? "#86efac" : "#bfdbfe";

        String noiDung = "<p style=\"font-size:15px;\">Xin chào <strong>" + ten + "</strong>,</p>"
            + "<p>Đơn hàng <strong>#" + maDonHang + "</strong> vừa được cập nhật:</p>"
            + "<div style=\"background:" + mauNen + ";border:1px solid " + mauVien + ";"
            + "border-radius:8px;padding:24px;margin:24px 0;text-align:center;\">"
            + "<p style=\"font-size:22px;font-weight:700;color:#1f2937;margin:0;\">" + label + "</p>"
            + "<p style=\"color:#6b7280;margin:10px 0 0;font-size:14px;\">" + moTa + "</p>"
            + "</div>"
            + "<p style=\"color:#6b7280;font-size:14px;\">Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.</p>";

        String subject = laHoanTat
                ? "[" + shopName + "] Giao hàng thành công #" + maDonHang
                : "[" + shopName + "] Cập nhật đơn hàng #" + maDonHang;
        send(toEmail, subject, khuonHtml("Cập nhật trạng thái đơn hàng", noiDung));
    }

    // ================================================================
    //  5. HỦY ĐƠN HÀNG
    // ================================================================

    /**
     * @param doKhachHuy true = khách tự hủy | false = cửa hàng hủy
     */
    @Async
    public void guiMailHuyDon(String toEmail, String hoTen, String maDonHang,
                               String lyDo, boolean doKhachHuy) {
        if (!hopLe(toEmail)) return;
        String ten = (hoTen != null && !hoTen.isBlank()) ? hoTen : "Quý khách";
        String nguoiHuy = doKhachHuy ? "theo yêu cầu của bạn" : "bởi cửa hàng";
        String lyDoHienThi = (lyDo != null && !lyDo.isBlank()) ? lyDo : "Không có ghi chú";
        String noiDung = "<p style=\"font-size:15px;\">Xin chào <strong>" + ten + "</strong>,</p>"
            + "<p>Đơn hàng <strong>#" + maDonHang + "</strong> đã bị hủy " + nguoiHuy + ".</p>"
            + "<div style=\"background:#fff7ed;border-left:4px solid #fb923c;"
            + "padding:16px 20px;border-radius:0 6px 6px 0;margin:24px 0;\">"
            + "<p style=\"margin:0;\"><strong>Lý do:</strong> " + lyDoHienThi + "</p>"
            + "</div>"
            + "<p>Nếu đây không phải yêu cầu của bạn hoặc bạn cần hỗ trợ, "
            + "vui lòng liên hệ ngay với chúng tôi.</p>"
            + "<p style=\"color:#9ca3af;font-size:13px;\">Chúng tôi rất tiếc vì sự bất tiện này. "
            + "Hy vọng gặp lại bạn trong những lần mua sắm tiếp theo!</p>";
        send(toEmail,
             "[" + shopName + "] Đơn hàng #" + maDonHang + " đã bị hủy",
             khuonHtml("Thông báo hủy đơn hàng", noiDung));
    }
}
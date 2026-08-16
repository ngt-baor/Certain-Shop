package com.certainshop.controller.quanly;

import com.certainshop.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quan-ly")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class TongQuanController {

    private final ThongKeService thongKeService;

    @GetMapping({"/", "/tong-quan"})
    public String tongQuan(Model model) {
        Map<String, Object> thongKe = thongKeService.layThongKeTongQuan();
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("sanPhamSapHet", thongKeService.sanPhamSapHetHang());
        model.addAttribute("sanPhamBanChay", thongKeService.sanPhamBanChay());
        return "quan-ly/tong-quan";
    }

    @GetMapping("/doanh-thu")
    public String doanhThu(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            Model model) {

        LocalDateTime tu = tuNgay != null && !tuNgay.isBlank()
                ? LocalDateTime.parse(tuNgay + "T00:00:00")
                : LocalDateTime.now().withDayOfMonth(1).withHour(0);
        LocalDateTime den = denNgay != null && !denNgay.isBlank()
                ? LocalDateTime.parse(denNgay + "T23:59:59")
                : LocalDateTime.now();

        List<Object[]> doanhThu = thongKeService.thongKeDoanhThuTheoNgay(tu, den);
        BigDecimal tongDoanhThu = thongKeService.tinhTongDoanhThu(tu, den);

        model.addAttribute("doanhThu", doanhThu);
        model.addAttribute("tongDoanhThu", tongDoanhThu);
        model.addAttribute("tuNgay", tu.toLocalDate().toString());
        model.addAttribute("denNgay", den.toLocalDate().toString());
        model.addAttribute("sanPhamBanChay", thongKeService.sanPhamBanChay());
        model.addAttribute("sanPhamSapHet", thongKeService.sanPhamSapHetHang());
        model.addAttribute("sanPhamHetHang", thongKeService.sanPhamHetHang());
        return "quan-ly/doanh-thu";
    }
}

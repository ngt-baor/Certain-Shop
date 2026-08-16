package com.certainshop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service gọi API GHN - Giao Hàng Nhanh
 * Production URL: https://online-gateway.ghn.vn
 */
@Service
@Slf4j
public class GHNApiService {

    @Value("${ghn.apiUrl:https://online-gateway.ghn.vn}")
    private String apiUrl;

    @Value("${ghn.token:}")
    private String token;

    @Value("${ghn.shopId:}")
    private String shopId;

    @Value("${ghn.warehouseDistrictId:1542}")
    private Integer warehouseDistrictId; // Kho hàng (mặc định Hà Đông, Hà Nội)

    @Value("${ghn.warehouseWardCode:20314}")
    private String warehouseWardCode; // Phường kho hàng

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lấy danh sách tỉnh/thành phố
     */
    public List<Map<String, Object>> layDanhSachTinh() {
        try {
            String url = apiUrl + "/shiip/public-api/master-data/province";
            HttpHeaders headers = taoHeaders();
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            List<Map<String, Object>> danhSach = new ArrayList<>();

            if (root.has("data") && root.get("data").isArray()) {
                for (JsonNode node : root.get("data")) {
                    Map<String, Object> tinh = new HashMap<>();
                    tinh.put("ProvinceID", node.get("ProvinceID").asInt());
                    tinh.put("ProvinceName", node.get("ProvinceName").asText());
                    danhSach.add(tinh);
                }
            }
            return danhSach;
        } catch (Exception e) {
            log.warn("Không thể lấy danh sách tỉnh từ GHN: {}", e.getMessage());
            return DuLieuDiaChi.layDanhSachTinh();
        }
    }

    /**
     * Lấy danh sách quận/huyện theo tỉnh
     */
    public List<Map<String, Object>> layDanhSachHuyen(int maTinh) {
        try {
            String url = apiUrl + "/shiip/public-api/master-data/district";
            HttpHeaders headers = taoHeaders();
            Map<String, Object> body = Map.of("province_id", maTinh);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            List<Map<String, Object>> danhSach = new ArrayList<>();

            if (root.has("data") && root.get("data").isArray()) {
                for (JsonNode node : root.get("data")) {
                    Map<String, Object> huyen = new HashMap<>();
                    huyen.put("DistrictID", node.get("DistrictID").asInt());
                    huyen.put("DistrictName", node.get("DistrictName").asText());
                    danhSach.add(huyen);
                }
            }
            return danhSach;
        } catch (Exception e) {
            log.warn("Không thể lấy danh sách huyện từ GHN: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách xã/phường theo huyện
     */
    public List<Map<String, Object>> layDanhSachXa(int maHuyen) {
        try {
            String url = apiUrl + "/shiip/public-api/master-data/ward";
            HttpHeaders headers = taoHeaders();
            Map<String, Object> body = Map.of("district_id", maHuyen);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            List<Map<String, Object>> danhSach = new ArrayList<>();

            if (root.has("data") && root.get("data").isArray()) {
                for (JsonNode node : root.get("data")) {
                    Map<String, Object> xa = new HashMap<>();
                    xa.put("WardCode", node.get("WardCode").asText());
                    xa.put("WardName", node.get("WardName").asText());
                    danhSach.add(xa);
                }
            }
            return danhSach;
        } catch (Exception e) {
            log.warn("Không thể lấy danh sách xã từ GHN: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Tính phí vận chuyển GHN
     * @param maHuyenNhan ID quận/huyện người nhận
     * @param maXaNhan Code phường/xã người nhận (format theo GHN)
     * @param tongKhoiLuongGram Trọng lượng (gram)
     * @return Phí vận chuyển tính từ kho
     */
    public BigDecimal tinhPhiVanChuyen(int maHuyenNhan, String maXaNhan, int tongKhoiLuongGram) {
        try {
            String url = apiUrl + "/shiip/public-api/v2/shipping-order/fee";
            HttpHeaders headers = taoHeadersVoiShopId();

            // Nếu trọng lượng = 0, tính mặc định 500g
            int khoiLuong = tongKhoiLuongGram > 0 ? tongKhoiLuongGram : 500;

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("service_type_id", 2); // 2 = Standard (dùng service_type_id thay vì service_id)
            body.put("from_district_id", warehouseDistrictId); // Kho hàng
            body.put("from_ward_code", warehouseWardCode);
            body.put("to_district_id", maHuyenNhan);
            body.put("to_ward_code", maXaNhan);
            body.put("weight", khoiLuong);
            body.put("length", 30); // Kích thước box dự tính
            body.put("width", 20);
            body.put("height", 5);
            body.put("insurance_value", 0); // Không bảo hiểm

            log.info("GHN Request URL: {}", url);
            log.info("GHN Request Body: {}", body);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);

            log.info("GHN Response Status: {}", response.getStatusCode());
            log.info("GHN Response Body: {}", response.getBody());

            JsonNode root = objectMapper.readTree(response.getBody());
            
            // Kiểm tra code = 200 (success)
            int code = root.has("code") ? root.get("code").asInt() : -1;
            if (code != 200) {
                String message = root.has("message") ? root.get("message").asText() : "Unknown error";
                log.warn("GHN API trả về lỗi: code={}, message={}", code, message);
                return BigDecimal.valueOf(35000L); // Phí mặc định
            }

            if (root.has("data") && root.get("data").has("total")) {
                long total = root.get("data").get("total").asLong();
                log.info("GHN tính phí thành công: {}", total);
                return BigDecimal.valueOf(total);
            }

            log.warn("GHN response không có trường 'total'");
            return BigDecimal.valueOf(35000L); // Phí mặc định
            
        } catch (RestClientException e) {
            log.error("Lỗi kết nối GHN: {}", e.getMessage());
            return BigDecimal.valueOf(35000L); // Phí mặc định khi lỗi
        } catch (Exception e) {
            log.error("Lỗi tính phí GHN: {}", e.getMessage(), e);
            return BigDecimal.valueOf(35000L); // Phí mặc định khi lỗi
        }
    }

    /**
     * Lấy danh sách dịch vụ khả dụng từ quận/huyện A đến B
     * @param maHuyenGui ID quận/huyện gửi
     * @param maHuyenNhan ID quận/huyện nhận
     * @return Danh sách services
     */
    public List<Map<String, Object>> layDanhSachDichVu(int maHuyenGui, int maHuyenNhan) {
        try {
            String url = apiUrl + "/shiip/public-api/v2/shipping-order/available-services";
            HttpHeaders headers = taoHeadersVoiShopId();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("from_district_id", maHuyenGui);
            body.put("to_district_id", maHuyenNhan);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            List<Map<String, Object>> danhSach = new ArrayList<>();

            if (root.has("data") && root.get("data").isArray()) {
                for (JsonNode node : root.get("data")) {
                    Map<String, Object> service = new HashMap<>();
                    service.put("service_id", node.get("service_id").asInt());
                    service.put("service_name", node.get("service_name").asText());
                    service.put("service_type_id", node.get("service_type_id").asInt());
                    danhSach.add(service);
                }
            }
            return danhSach;
        } catch (Exception e) {
            log.warn("Không thể lấy danh sách dịch vụ từ GHN: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private HttpHeaders taoHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!token.isBlank()) headers.set("Token", token);
        return headers;
    }

    private HttpHeaders taoHeadersVoiShopId() {
        HttpHeaders headers = taoHeaders();
        if (!shopId.isBlank()) headers.set("ShopId", shopId);
        return headers;
    }

    /**
     * Dữ liệu địa chỉ dự phòng khi API GHN không khả dụng
     */
    public static class DuLieuDiaChi {
        public static List<Map<String, Object>> layDanhSachTinh() {
            List<Map<String, Object>> tinhList = new ArrayList<>();
            Object[][] data = {
                {201, "Hà Nội"}, {202, "TP. Hồ Chí Minh"}, {203, "Đà Nẵng"},
                {204, "Hải Phòng"}, {205, "Cần Thơ"}, {206, "An Giang"},
                {207, "Bà Rịa - Vũng Tàu"}, {208, "Bắc Giang"}, {209, "Bắc Kạn"},
                {210, "Bạc Liêu"}, {211, "Bắc Ninh"}, {212, "Bến Tre"},
                {213, "Bình Định"}, {214, "Bình Dương"}, {215, "Bình Phước"},
                {216, "Bình Thuận"}, {217, "Cà Mau"}, {218, "Cao Bằng"},
                {219, "Đắk Lắk"}, {220, "Đắk Nông"}, {221, "Điện Biên"},
                {222, "Đồng Nai"}, {223, "Đồng Tháp"}, {224, "Gia Lai"},
                {225, "Hà Giang"}, {226, "Hà Nam"}, {227, "Hà Tĩnh"},
                {228, "Hải Dương"}, {229, "Hậu Giang"}, {230, "Hòa Bình"},
                {231, "Hưng Yên"}, {232, "Khánh Hòa"}, {233, "Kiên Giang"},
                {234, "Kon Tum"}, {235, "Lai Châu"}, {236, "Lâm Đồng"},
                {237, "Lạng Sơn"}, {238, "Lào Cai"}, {239, "Long An"},
                {240, "Nam Định"}, {241, "Nghệ An"}, {242, "Ninh Bình"},
                {243, "Ninh Thuận"}, {244, "Phú Thọ"}, {245, "Phú Yên"},
                {246, "Quảng Bình"}, {247, "Quảng Nam"}, {248, "Quảng Ngãi"},
                {249, "Quảng Ninh"}, {250, "Quảng Trị"}, {251, "Sóc Trăng"},
                {252, "Sơn La"}, {253, "Tây Ninh"}, {254, "Thái Bình"},
                {255, "Thái Nguyên"}, {256, "Thanh Hóa"}, {257, "Thừa Thiên Huế"},
                {258, "Tiền Giang"}, {259, "Trà Vinh"}, {260, "Tuyên Quang"},
                {261, "Vĩnh Long"}, {262, "Vĩnh Phúc"}, {263, "Yên Bái"}
            };
            for (Object[] row : data) {
                tinhList.add(Map.of("ProvinceID", row[0], "ProvinceName", row[1]));
            }
            return tinhList;
        }
    }
}

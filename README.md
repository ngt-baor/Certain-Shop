# Certain Shop

Certain Shop là hệ thống thương mại điện tử bán áo, gồm website khách hàng và trang quản trị vận hành. Dự án tách frontend React/Vite và backend Spring Boot, hỗ trợ mua hàng, đơn hàng, voucher, thanh toán VNPay và quản lý sản phẩm.

## Built With

![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-7-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![SQL Server](https://img.shields.io/badge/SQL%20Server-Database-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white)

## Project Overview

Certain Shop phục vụ ba nhóm người dùng:

- Khách hàng: đăng ký, đăng nhập, tìm kiếm sản phẩm, giỏ hàng, đặt hàng, theo dõi đơn, cập nhật hồ sơ và đổi mật khẩu.
- Nhân viên: xử lý đơn hàng, bán hàng tại quầy và quản lý nghiệp vụ được cấp quyền.
- Quản trị viên: quản lý sản phẩm, biến thể, người dùng, voucher, hóa đơn, đơn hàng và thống kê.

## Main Features

- Đăng nhập, đăng ký và phân quyền theo vai trò.
- Giỏ hàng và checkout với địa chỉ giao hàng, phí vận chuyển và voucher.
- Tạo, theo dõi và cập nhật trạng thái đơn hàng.
- Thanh toán VNPay theo cấu hình môi trường.
- Quản lý sản phẩm, danh mục, biến thể, khuyến mãi và voucher.
- Dashboard doanh thu, sản phẩm bán chạy và cảnh báo tồn kho.
- Bán hàng tại quầy và in hóa đơn PDF.
- Quản lý tài khoản, tạo tài khoản nhân viên và đổi mật khẩu.
- Rate limiting cho các endpoint xác thực và public API.

## Project Structure

```text
Certain-Shop/
├── frontend/                 # React + TypeScript + Vite
│   ├── src/pages/            # Trang khách hàng và quản trị
│   ├── src/components/       # Thành phần giao diện
│   └── src/services/         # API client
├── backend/                  # Spring Boot API
│   ├── src/main/java/        # Controller, service, entity, repository
│   ├── src/main/resources/   # application.properties và migration
│   ├── src/test/             # Unit/regression tests
│   └── .env.example          # Mẫu biến môi trường
└── README.md
```

## Getting Started

### Prerequisites

- Node.js 20+ và npm
- JDK 17+
- Maven 3.9+
- Microsoft SQL Server

### Clone Project

```bash
git clone https://github.com/ngt-baor/Certain-Shop.git
cd Certain-Shop
```

### Backend

Tạo biến môi trường từ [backend/.env.example](backend/.env.example). Không commit file `.env` hoặc bất kỳ thông tin kết nối thật nào.

Các biến tối thiểu:

```env
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=Certain_Shop;encrypt=true;trustServerCertificate=true
DB_USERNAME=your_database_user
DB_PASSWORD=your_database_password
JWT_SECRET=replace_with_a_long_random_secret
```

Chạy backend:

```bash
cd backend
mvn spring-boot:run
```

Backend chạy mặc định tại `http://localhost:8080`.

### Frontend

Mở terminal khác:

```bash
cd frontend
npm install
npm run dev
```

Frontend chạy mặc định tại `http://localhost:5173` và proxy API tới backend tại cổng `8080`.

### Production Build

```bash
cd frontend
npm run build
```

```bash
cd backend
mvn test
mvn package
```

## Security Notes

- Database, JWT, mail, VNPay và GHN đều được cấu hình bằng environment variables.
- Không commit mật khẩu, access token, API key, database backup hoặc file `.env`.
- API kiểm tra quyền sở hữu dữ liệu theo người dùng hiện tại để giảm rủi ro IDOR.
- Các luồng đăng nhập và API public có rate limit theo IP trong phạm vi process.

## Notes

- Cần import schema/database SQL Server phù hợp với môi trường của bạn trước khi chạy backend.
- Cấu hình VNPay và GHN là tùy chọn cho môi trường local; chỉ bật khi đã có credential hợp lệ.

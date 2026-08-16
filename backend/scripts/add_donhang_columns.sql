-- ============================================================
-- Thêm TẤT CẢ cột thiếu vào bảng DonHang (khớp entity DonHang.java)
-- Chạy trên database certain_shop (đổi tên USE nếu cần)
-- ============================================================

USE [certain_shop]
GO

-- Helper: thêm cột nếu chưa có (tên cột phải đúng PascalCase như @Column)
-- Cột nào đã có sẽ bỏ qua.

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'NguoiDungId')
    ALTER TABLE dbo.DonHang ADD NguoiDungId bigint NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'MaDonHang')
    ALTER TABLE dbo.DonHang ADD MaDonHang varchar(50) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'TongTienHang')
    ALTER TABLE dbo.DonHang ADD TongTienHang decimal(18,2) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'SoTienGiamGia')
    ALTER TABLE dbo.DonHang ADD SoTienGiamGia decimal(18,2) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'PhiVanChuyen')
    ALTER TABLE dbo.DonHang ADD PhiVanChuyen decimal(18,2) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'TongTienThanhToan')
    ALTER TABLE dbo.DonHang ADD TongTienThanhToan decimal(18,2) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'TrangThaiDonHang')
    ALTER TABLE dbo.DonHang ADD TrangThaiDonHang varchar(50) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'LoaiDonHang')
    ALTER TABLE dbo.DonHang ADD LoaiDonHang varchar(50) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'PhuongThucThanhToan')
    ALTER TABLE dbo.DonHang ADD PhuongThucThanhToan varchar(50) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'KhuyenMaiId')
    ALTER TABLE dbo.DonHang ADD KhuyenMaiId bigint NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'VoucherId')
    ALTER TABLE dbo.DonHang ADD VoucherId bigint NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'TenNguoiNhan')
    ALTER TABLE dbo.DonHang ADD TenNguoiNhan nvarchar(150) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'SdtNguoiNhan')
    ALTER TABLE dbo.DonHang ADD SdtNguoiNhan varchar(20) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'DiaChiGiaoHang')
    ALTER TABLE dbo.DonHang ADD DiaChiGiaoHang nvarchar(500) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'DaThanhToan')
    ALTER TABLE dbo.DonHang ADD DaThanhToan bit NOT NULL DEFAULT 0;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'MaTinhGHN')
    ALTER TABLE dbo.DonHang ADD MaTinhGHN int NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'MaHuyenGHN')
    ALTER TABLE dbo.DonHang ADD MaHuyenGHN int NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'MaXaGHN')
    ALTER TABLE dbo.DonHang ADD MaXaGHN varchar(20) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'GhiChu')
    ALTER TABLE dbo.DonHang ADD GhiChu nvarchar(255) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'GhiChuThuNgan')
    ALTER TABLE dbo.DonHang ADD GhiChuThuNgan nvarchar(255) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'NhanVienId')
    ALTER TABLE dbo.DonHang ADD NhanVienId bigint NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'VnpayTxnRef')
    ALTER TABLE dbo.DonHang ADD VnpayTxnRef varchar(100) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'ThoiGianTao')
    ALTER TABLE dbo.DonHang ADD ThoiGianTao datetime NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'ThoiGianCapNhat')
    ALTER TABLE dbo.DonHang ADD ThoiGianCapNhat datetime NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DonHang') AND name = 'ThoiGianTuHuy')
    ALTER TABLE dbo.DonHang ADD ThoiGianTuHuy datetime NULL;

PRINT 'Hoan thanh kiem tra/them cot DonHang.';
GO

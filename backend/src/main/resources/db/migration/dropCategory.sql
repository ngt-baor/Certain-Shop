use certain_shop;

DELETE FROM dbo.ChiTietDonHang
WHERE BienTheId IN (
    SELECT Id FROM dbo.BienThe
    WHERE SanPhamId IN (
        SELECT Id FROM dbo.SanPham WHERE DanhMucId = 4
    )
);


DELETE FROM dbo.BienThe
WHERE SanPhamId IN (
    SELECT Id FROM dbo.SanPham WHERE DanhMucId = 4
);

DELETE FROM dbo.SanPham
WHERE DanhMucId = 4;

DELETE FROM dbo.DanhMuc
WHERE Id = 4;
--
DELETE FROM dbo.ChiTietDonHang
WHERE BienTheId IN (
    SELECT Id FROM dbo.BienThe
    WHERE SanPhamId IN (
        SELECT Id FROM dbo.SanPham WHERE DanhMucId = 3
    )
);


DELETE FROM dbo.BienThe
WHERE SanPhamId IN (
    SELECT Id FROM dbo.SanPham WHERE DanhMucId = 3
);

DELETE FROM dbo.SanPham
WHERE DanhMucId = 3;

DELETE FROM dbo.DanhMuc
WHERE Id = 3;

--
DELETE FROM dbo.ChiTietDonHang
WHERE BienTheId IN (
    SELECT Id FROM dbo.BienThe
    WHERE SanPhamId IN (
        SELECT Id FROM dbo.SanPham WHERE DanhMucId = 5
    )
);

--  Xóa biến thể
DELETE FROM dbo.BienThe
WHERE SanPhamId IN (
    SELECT Id FROM dbo.SanPham WHERE DanhMucId = 5
);

-- Xóa sản phẩm
DELETE FROM dbo.SanPham
WHERE DanhMucId = 5;

-- 4️ Xóa danh mục
DELETE FROM dbo.DanhMuc
WHERE Id = 5;


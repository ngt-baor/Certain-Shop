-- Update all existing SanPham records to have TrangThai = 1 (active)
UPDATE SanPham SET TrangThai = 1 WHERE TrangThai IS NULL;

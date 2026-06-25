-- ============================================================
-- ZAPPY - DỮ LIỆU MẪU ĐẦY ĐỦ
-- Chạy file này trong Navicat: Query -> New Query -> Run
-- Lưu ý: Chạy sau khi đã khởi động Backend ít nhất 1 lần
--         (để Hibernate tự tạo bảng trước)
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. RESTAURANTS - 3 chi nhánh
-- ============================================================
INSERT INTO restaurants (res_name, res_domain, address) VALUES
('Mỳ Cay Seoul', 'mycayseoulthuduc', '126 Đường Võ Văn Ngân, Bình Thọ, Thủ Đức, TP.HCM'),
('Mỳ Cay Seoul', 'mycayseoulbinhthanh', '45 Đinh Tiên Hoàng, Phường 3, Bình Thạnh, TP.HCM'),
('Phở Hà Nội 1975', 'phohanoi1975thuduc', '88 Lê Văn Việt, Tăng Nhơn Phú A, Thủ Đức, TP.HCM');

-- ============================================================
-- 2. USERS - Mỗi nhà hàng 1 Quản lý (role=1) + 2 Nhân viên (role=0)
-- ============================================================

-- Chi nhánh 1: Mỳ Cay Seoul Thủ Đức (res_id = 1)
INSERT INTO users (res_id, username, password, role, email, fullname) VALUES
(1, 'admin',   '123456', 1, 'admin1@gmail.com', 'Admin'),   -- Quản lý
(1, 'nhanvien01', '123456', 0, 'nv1@gmail.com', 'Nhân viên 1'), -- Nhân viên 1
(1, 'nhanvien02', '123456', 0, 'nv2@gmail.com', 'Nhân viên 2'); -- Nhân viên 2

-- Chi nhánh 2: Mỳ Cay Seoul Bình Thạnh (res_id = 2)
INSERT INTO users (res_id, username, password, role, email, fullname) VALUES
(2, 'admin',   '123456', 1, 'admin2@gmail.com', 'Admin BT'),
(2, 'nv_binhthanh01', '123456', 0, 'nv3@gmail.com', 'Nhân viên 3'),
(2, 'nv_binhthanh02', '123456', 0, 'nv4@gmail.com', 'Nhân viên 4');

-- Chi nhánh 3: Phở Hà Nội 1975 (res_id = 3)
INSERT INTO users (res_id, username, password, role, email, fullname) VALUES
(3, 'admin',   '123456', 1, 'admin3@gmail.com', 'Admin HN'),
(3, 'nv_pho01', '123456', 0, 'nv5@gmail.com', 'Nhân viên 5');

-- ============================================================
-- 3. AREAS - Khu vực phục vụ cho từng nhà hàng
-- ============================================================

-- Mỳ Cay Seoul Thủ Đức (res_id = 1) - 4 khu
INSERT INTO areas (res_id, area_name) VALUES
(1, 'Tầng Trệt'),
(1, 'Lầu 1'),
(1, 'Khu VIP'),
(1, 'Sân Vườn');

-- Mỳ Cay Seoul Bình Thạnh (res_id = 2) - 3 khu
INSERT INTO areas (res_id, area_name) VALUES
(2, 'Khu A'),
(2, 'Khu B'),
(2, 'Khu Ngoài Trời');

-- Phở Hà Nội 1975 (res_id = 3) - 2 khu
INSERT INTO areas (res_id, area_name) VALUES
(3, 'Khu Trong'),
(3, 'Khu Ngoài');

-- ============================================================
-- 4. TABLES - Bàn ăn
-- areas id: 1=Tầng Trệt, 2=Lầu1, 3=VIP, 4=SânVườn
--           5=KhuA, 6=KhuB, 7=NgoàiTrời, 8=KhuTrong, 9=KhuNgoài
-- ============================================================

-- Tầng Trệt (area_id = 1) - 8 bàn
INSERT INTO tables (area_id, table_name, is_occupied) VALUES
(1, 'Bàn 01', 0),
(1, 'Bàn 02', 1),  -- Đang có khách
(1, 'Bàn 03', 0),
(1, 'Bàn 04', 1),  -- Đang có khách
(1, 'Bàn 05', 0),
(1, 'Bàn 06', 1),  -- Đang có khách
(1, 'Bàn 07', 0),
(1, 'Bàn 08', 0);

-- Lầu 1 (area_id = 2) - 6 bàn
INSERT INTO tables (area_id, table_name, is_occupied) VALUES
(2, 'Bàn A01', 1),
(2, 'Bàn A02', 0),
(2, 'Bàn A03', 1),
(2, 'Bàn A04', 0),
(2, 'Bàn A05', 0),
(2, 'Bàn A06', 1);

-- Khu VIP (area_id = 3) - 4 bàn
INSERT INTO tables (area_id, table_name, is_occupied) VALUES
(3, 'VIP 01', 0),
(3, 'VIP 02', 1),
(3, 'VIP 03', 0),
(3, 'VIP 04', 0);

-- Sân Vườn (area_id = 4) - 5 bàn
INSERT INTO tables (area_id, table_name, is_occupied) VALUES
(4, 'SV 01', 0),
(4, 'SV 02', 0),
(4, 'SV 03', 1),
(4, 'SV 04', 0),
(4, 'SV 05', 0);

-- Khu A - Mỳ Cay Seoul BT (area_id = 5) - 6 bàn
INSERT INTO tables (area_id, table_name, is_occupied) VALUES
(5, 'A01', 1),
(5, 'A02', 0),
(5, 'A03', 1),
(5, 'A04', 0),
(5, 'A05', 0),
(5, 'A06', 1);

-- Khu B (area_id = 6) - 4 bàn
INSERT INTO tables (area_id, table_name, is_occupied) VALUES
(6, 'B01', 0),
(6, 'B02', 1),
(6, 'B03', 0),
(6, 'B04', 0);

-- Khu Ngoài Trời (area_id = 7) - 4 bàn
INSERT INTO tables (area_id, table_name, is_occupied) VALUES
(7, 'NT 01', 0),
(7, 'NT 02', 0),
(7, 'NT 03', 1),
(7, 'NT 04', 0);

-- Phở HN (area_id = 8,9) - 8 bàn
INSERT INTO tables (area_id, table_name, is_occupied) VALUES
(8, 'Bàn 01', 1),
(8, 'Bàn 02', 0),
(8, 'Bàn 03', 1),
(8, 'Bàn 04', 0),
(9, 'Bàn 05', 0),
(9, 'Bàn 06', 1),
(9, 'Bàn 07', 0),
(9, 'Bàn 08', 0);

-- ============================================================
-- 5. UNITS - Đơn vị tính (riêng cho mỗi nhà hàng)
-- ============================================================

-- Mỳ Cay Seoul Thủ Đức (res_id = 1)
INSERT INTO units (res_id, unit_name) VALUES
(1, 'Tô'),
(1, 'Phần'),
(1, 'Ly'),
(1, 'Chai'),
(1, 'Lon'),
(1, 'Cái'),
(1, 'Đĩa'),
(1, 'Set');

-- Mỳ Cay Seoul Bình Thạnh (res_id = 2)
INSERT INTO units (res_id, unit_name) VALUES
(2, 'Tô'),
(2, 'Phần'),
(2, 'Ly'),
(2, 'Chai'),
(2, 'Lon'),
(2, 'Cái');

-- Phở Hà Nội 1975 (res_id = 3)
INSERT INTO units (res_id, unit_name) VALUES
(3, 'Tô'),
(3, 'Phần'),
(3, 'Ly'),
(3, 'Đĩa'),
(3, 'Cái');

-- ============================================================
-- 6. CATEGORIES - Danh mục menu
-- ============================================================

-- Mỳ Cay Seoul Thủ Đức (res_id = 1)
INSERT INTO categories (res_id, cat_name) VALUES
(1, 'Mỳ Cay'),
(1, 'Lẩu'),
(1, 'Đồ Nướng'),
(1, 'Đồ Uống'),
(1, 'Ăn Vặt'),
(1, 'Tráng Miệng');

-- Mỳ Cay Seoul Bình Thạnh (res_id = 2)
INSERT INTO categories (res_id, cat_name) VALUES
(2, 'Mỳ Cay'),
(2, 'Lẩu'),
(2, 'Đồ Uống'),
(2, 'Ăn Kèm');

-- Phở Hà Nội 1975 (res_id = 3)
INSERT INTO categories (res_id, cat_name) VALUES
(3, 'Phở'),
(3, 'Bún'),
(3, 'Cơm'),
(3, 'Đồ Uống'),
(3, 'Ăn Vặt');

-- ============================================================
-- 7. MENU_ITEMS - Món ăn
-- cat_id:  1=MỳCay, 2=Lẩu, 3=Nướng, 4=Uống, 5=ĂnVặt, 6=TrángMiệng (nhà hàng 1)
-- unit_id: 1=Tô, 2=Phần, 3=Ly, 4=Chai, 5=Lon, 6=Cái, 7=Đĩa, 8=Set
-- ============================================================

-- Mỳ Cay Seoul Thủ Đức - Danh mục MỲ CAY (cat_id=1)
INSERT INTO menu_items (cat_id, unit_id, item_name, price, image_url) VALUES
(1, 1, 'Mỳ Cay Bò Mỹ Cấp Độ 1',  55000, ''),
(1, 1, 'Mỳ Cay Bò Mỹ Cấp Độ 3',  58000, ''),
(1, 1, 'Mỳ Cay Bò Mỹ Cấp Độ 5',  60000, ''),
(1, 1, 'Mỳ Cay Hải Sản Cấp Độ 1', 65000, ''),
(1, 1, 'Mỳ Cay Hải Sản Cấp Độ 3', 68000, ''),
(1, 1, 'Mỳ Cay Hải Sản Cấp Độ 5', 70000, ''),
(1, 1, 'Mỳ Cay Gà Cấp Độ 1',      52000, ''),
(1, 1, 'Mỳ Cay Gà Cấp Độ 3',      55000, ''),
(1, 1, 'Mỳ Cay Nấm Đặc Biệt',     50000, ''),
(1, 1, 'Mỳ Kim Chi Cay Truyền Thống', 48000, ''),
(1, 1, 'Mỳ Cay Tôm Đặc Biệt',     62000, ''),
(1, 8, 'Set Đôi Mỳ Cay + Lẩu Mini', 120000, '');

-- Danh mục LẨU (cat_id=2)
INSERT INTO menu_items (cat_id, unit_id, item_name, price, image_url) VALUES
(2, 8, 'Lẩu Hải Sản Cay',       250000, ''),
(2, 8, 'Lẩu Bò Mỹ Cay',        220000, ''),
(2, 8, 'Lẩu Gà Lá Chanh',      200000, ''),
(2, 8, 'Lẩu Thái Chua Cay',    230000, ''),
(2, 8, 'Lẩu Nấm Chay',         180000, ''),
(2, 2, 'Thêm Mì Tươi',          20000, ''),
(2, 2, 'Thêm Rau Lẩu',          25000, ''),
(2, 2, 'Thêm Bò Thái Lát',      55000, ''),
(2, 2, 'Thêm Hải Sản Hỗn Hợp',  60000, '');

-- Danh mục ĐỒ NƯỚNG (cat_id=3)
INSERT INTO menu_items (cat_id, unit_id, item_name, price, image_url) VALUES
(3, 2, 'Thịt Bò Nướng Bulgogi',   75000, ''),
(3, 2, 'Gà Nướng Sốt Cay Hàn',   65000, ''),
(3, 2, 'Tôm Nướng Muối Ớt',      80000, ''),
(3, 2, 'Mực Nướng Sa Tế',        70000, ''),
(3, 7, 'Đĩa Rau Nướng Hàn Quốc', 35000, ''),
(3, 6, 'Phomai Que Chiên',        30000, '');

-- Danh mục ĐỒ UỐNG (cat_id=4)
INSERT INTO menu_items (cat_id, unit_id, item_name, price, image_url) VALUES
(4, 3, 'Trà Chanh Sả',         20000, ''),
(4, 3, 'Trà Đào Cam Sả',       25000, ''),
(4, 3, 'Trà Sữa Trân Châu',    35000, ''),
(4, 3, 'Nước Ép Dâu Tây',      30000, ''),
(4, 3, 'Nước Ép Dứa Bạc Hà',   28000, ''),
(4, 3, 'Sinh Tố Bơ',           35000, ''),
(4, 3, 'Nước Cam Tươi',        25000, ''),
(4, 4, 'Bia Tiger Chai 330ml', 25000, ''),
(4, 5, 'Bia Heineken Lon',     22000, ''),
(4, 5, 'Coca Cola Lon',        15000, ''),
(4, 5, 'Nước Suối Lavie',      10000, ''),
(4, 3, 'Soju Hàn Quốc Chá',    55000, '');

-- Danh mục ĂN VẶT (cat_id=5)
INSERT INTO menu_items (cat_id, unit_id, item_name, price, image_url) VALUES
(5, 2, 'Khăn Lạnh',                  2000, ''),
(5, 2, 'Salad Ức Gà Hàn Quốc',      45000, ''),
(5, 7, 'Kim Chi Cải Thảo Truyền Thống', 25000, ''),
(5, 7, 'Khoai Tây Chiên Phô Mai',   35000, ''),
(5, 2, 'Bánh Mì Bơ Tỏi Nướng',     20000, ''),
(5, 6, 'Mandu Chiên (5 cái)',        40000, ''),
(5, 6, 'Tokbokki Sốt Gochujang',    45000, ''),
(5, 7, 'Đĩa Rau Sống',              15000, '');

-- Danh mục TRÁNG MIỆNG (cat_id=6)
INSERT INTO menu_items (cat_id, unit_id, item_name, price, image_url) VALUES
(6, 2, 'Kem Dâu Tây Hàn Quốc',    45000, ''),
(6, 2, 'Bánh Hotteok Nhân Quế',   30000, ''),
(6, 3, 'Chè Thái Đặc Biệt',      35000, ''),
(6, 2, 'Bingsu Đào Sữa',         55000, '');

-- Phở Hà Nội 1975 (cat_id=14=Phở, 15=Bún, 16=Cơm, 17=Uống, 18=ĂnVặt)
-- units: 18=Tô(pho), 19=Phần, 20=Ly, 21=Đĩa, 22=Cái
INSERT INTO menu_items (cat_id, unit_id, item_name, price, image_url) VALUES
(14, 18, 'Phở Bò Tái Chín',       55000, ''),
(14, 18, 'Phở Bò Gầu Gân',       60000, ''),
(14, 18, 'Phở Bò Đặc Biệt',      70000, ''),
(14, 18, 'Phở Gà Truyền Thống',  50000, ''),
(15, 18, 'Bún Bò Huế',           52000, ''),
(15, 18, 'Bún Chả Hà Nội',       55000, ''),
(15, 18, 'Bún Riêu Cua Đồng',    50000, ''),
(16, 19, 'Cơm Tấm Sườn Bì Chả', 45000, ''),
(16, 19, 'Cơm Chiên Dương Châu', 40000, ''),
(17, 20, 'Trà Đá',                5000, ''),
(17, 20, 'Nước Ép Cam',          25000, ''),
(18, 21, 'Đĩa Quẩy Rán',        10000, ''),
(18, 22, 'Chả Lụa',             20000, '');

-- ============================================================
-- 8. ORDERS - Hóa đơn (một số đang phục vụ, một số đã xong)
-- Bàn đang có khách: 2,4,6 (Tầng Trệt), 9,11,14 (Lầu 1)...
-- table_id tương ứng: Bàn02=2, Bàn04=4, Bàn06=6, BànA01=9
-- ============================================================
INSERT INTO orders (table_id, status, total_amount, user_id) VALUES
(2,  0, 0, 1),       -- Bàn 02 Tầng Trệt - ĐANG PHỤC VỤ
(4,  0, 0, 1),       -- Bàn 04 Tầng Trệt - ĐANG PHỤC VỤ
(6,  0, 0, 1),       -- Bàn 06 Tầng Trệt - ĐANG PHỤC VỤ
(9,  0, 0, 1),       -- Bàn A01 Lầu 1    - ĐANG PHỤC VỤ
(11, 0, 0, 1),       -- Bàn A03 Lầu 1    - ĐANG PHỤC VỤ
(14, 0, 0, 1),       -- Bàn A06 Lầu 1    - ĐANG PHỤC VỤ
(15, 0, 0, 1),       -- VIP 02           - ĐANG PHỤC VỤ
-- Hóa đơn đã thanh toán (để có lịch sử)
(1,  1, 178000, 1),  -- ĐÃ THANH TOÁN
(3,  1, 125000, 1),
(5,  1, 310000, 1),
(7,  1, 250000, 1);

-- ============================================================
-- 9. ORDER_DETAILS - Chi tiết gọi món
-- item_id: 1=MỳCayBòCấpĐộ1(55k), 2=CấpĐộ3(58k),...
--          29=TràChanhSả(20k), 30=TràĐào(25k)...
--          43=KhănLạnh(2k), 44=SaladỨcGà(45k)...
-- order_id: 1=Bàn02, 2=Bàn04, 3=Bàn06, 4=BànA01...
-- ============================================================

-- Bàn 02 (order_id=1) - Đã gửi bếp (status=1)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status) VALUES
(1, 1,  1, 55000, 'Cấp độ 1, không hành ngò', 1),
(1, 29, 2, 20000, '', 1),
(1, 43, 2, 2000,  '', 1),
(1, 44, 1, 45000, 'Ít rau mầm', 1);

-- Bàn 04 (order_id=2) - Đang gọi thêm (status=0 và 1 lẫn lộn)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status) VALUES
(2, 4,  1, 65000, 'Hải sản cấp độ 3, thêm mực', 1),
(2, 5,  1, 68000, 'Cấp độ 5, cực cay', 1),
(2, 31, 1, 35000, '', 1),
(2, 33, 2, 25000, '', 1),  -- Coca đang nháp
(2, 44, 1, 45000, '', 0);   -- Salad đang nháp (chưa gửi)

-- Bàn 06 (order_id=3)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status) VALUES
(3, 13, 1, 250000, '4 người, lấy thêm rau', 1),
(3, 20, 1, 55000, '', 1),
(3, 21, 1, 60000, '', 1),
(3, 29, 4, 20000, '', 1),
(3, 43, 4, 2000,  '', 1);

-- Bàn A01 (order_id=4)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status) VALUES
(4, 3,  2, 58000, 'Cấp độ 3', 1),
(4, 22, 1, 75000, 'Bò nướng chín kỹ', 1),
(4, 35, 1, 55000, '', 1),
(4, 46, 2, 40000, '', 1);

-- Bàn A03 (order_id=5)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status) VALUES
(5, 14, 1, 220000, 'Lẩu bò, ít cay', 1),
(5, 18, 2, 55000, '', 1),
(5, 19, 1, 60000, '', 1),
(5, 32, 1, 25000, '', 0);

-- Hóa đơn đã thanh toán (order_id=8,9)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status) VALUES
(8, 2,  1, 58000, '', 1),
(8, 10, 1, 48000, '', 1),
(8, 29, 2, 20000, '', 1),
(8, 43, 2, 2000,  '', 1),
(9, 1,  1, 55000, 'Không hành', 1),
(9, 29, 1, 20000, '', 1),
(9, 44, 1, 45000, '', 1);

-- Cập nhật total_amount cho các hóa đơn đã thanh toán
UPDATE orders SET total_amount = 178000 WHERE id = 8;
UPDATE orders SET total_amount = 125000 WHERE id = 9;
UPDATE orders SET total_amount = 310000 WHERE id = 10;
UPDATE orders SET total_amount = 250000 WHERE id = 11;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- KIỂM TRA KẾT QUẢ
-- ============================================================
SELECT 'restaurants' AS bang, COUNT(*) AS so_ban_ghi FROM restaurants
UNION ALL SELECT 'users',        COUNT(*) FROM users
UNION ALL SELECT 'areas',        COUNT(*) FROM areas
UNION ALL SELECT 'tables',       COUNT(*) FROM `tables`
UNION ALL SELECT 'units',        COUNT(*) FROM units
UNION ALL SELECT 'categories',   COUNT(*) FROM categories
UNION ALL SELECT 'menu_items',   COUNT(*) FROM menu_items
UNION ALL SELECT 'orders',       COUNT(*) FROM orders
UNION ALL SELECT 'order_details',COUNT(*) FROM order_details;

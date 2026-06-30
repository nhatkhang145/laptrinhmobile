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
-- Cột: id (auto), res_name, res_domain, address
-- ============================================================
INSERT INTO restaurants (res_name, res_domain, address) VALUES
('Mỳ Cay Seoul', 'mycayseoulthuduc',         '126 Đường Võ Văn Ngân, Bình Thọ, Thủ Đức, TP.HCM'),
('Mỳ Cay Seoul', 'mycayseoulbinhthanh','45 Đinh Tiên Hoàng, Phường 3, Bình Thạnh, TP.HCM'),
('Phở Hà Nội 1975', 'phohanoi1975',    '88 Lê Văn Việt, Tăng Nhơn Phú A, Thủ Đức, TP.HCM');

-- ============================================================
-- 2. USERS - Nhân viên của từng nhà hàng
-- Cột: id (auto), res_id, username, password, role, email, fullname, is_online, is_active
-- role: 1 = Quản lý, 0 = Nhân viên, 2 = Thu ngân
-- ============================================================

-- Mỳ Cay Seoul Thủ Đức (res_id = 1)
INSERT INTO users (res_id, username, password, role, email, fullname, is_online, is_active) VALUES
(1, 'admin',         '123456', 1, 'admin1@gmail.com',       'Admin',           true,  true),
(1, 'nhanvien01',    '123456', 0, 'nv1@gmail.com',          'Nhân viên 1',     false, true),
(1, 'nhanvien02',    '123456', 0, 'nv2@gmail.com',          'Nhân viên 2',     false, true),
(1, 'thungan01',     '123456', 2, 'thungan1@gmail.com',     'Thu ngân 1',false,true);

-- Mỳ Cay Seoul Bình Thạnh (res_id = 2)
INSERT INTO users (res_id, username, password, role, email, fullname, is_online, is_active) VALUES
(2, 'admin2',        '123456', 1, 'admin2@gmail.com',       'Admin Bình Thạnh',false, true),
(2, 'nv_bt01',       '123456', 0, 'nv3@gmail.com',          'Nhân viên BT 1',  false, true),
(2, 'nv_bt02',       '123456', 0, 'nv4@gmail.com',          'Nhân viên BT 2',  false, true),
(2, 'tn_bt01',       '123456', 2, 'tn1_bt@gmail.com',       'Thu ngân BT 1',   false, true);

-- Phở Hà Nội 1975 (res_id = 3)
INSERT INTO users (res_id, username, password, role, email, fullname, is_online, is_active) VALUES
(3, 'admin3',        '123456', 1, 'admin3@gmail.com',       'Admin Phở HN',    false, true),
(3, 'nv_pho01',      '123456', 0, 'nv5@gmail.com',          'Nhân viên Phở 1', false, true),
(3, 'tn_pho01',      '123456', 2, 'tn1_pho@gmail.com',      'Thu ngân Phở 1',  false, true);

-- ============================================================
-- 3. AREAS - Khu vực phục vụ
-- Cột: id (auto), res_id, area_name, is_active
-- ============================================================

-- Mỳ Cay Seoul Thủ Đức (res_id=1): area_id 1..4
INSERT INTO areas (res_id, area_name, is_active) VALUES
(1, 'Tầng Trệt',    true),
(1, 'Lầu 1',        true),
(1, 'Khu VIP',      true),
(1, 'Sân Vườn',     true);

-- Mỳ Cay Seoul Bình Thạnh (res_id=2): area_id 5..7
INSERT INTO areas (res_id, area_name, is_active) VALUES
(2, 'Khu A',         true),
(2, 'Khu B',         true),
(2, 'Khu Ngoài Trời',true);

-- Phở Hà Nội 1975 (res_id=3): area_id 8..9
INSERT INTO areas (res_id, area_name, is_active) VALUES
(3, 'Khu Trong',    true),
(3, 'Khu Ngoài',    true);

-- ============================================================
-- 4. TABLES - Bàn ăn
-- Cột: id (auto), area_id, table_name, is_occupied, status, seats
-- status: HOẠT ĐỘNG | ĐANG KHÓA | BẢO TRÌ
-- ============================================================

-- Tầng Trệt (area_id=1) - 8 bàn: table_id 1..8
INSERT INTO `tables` (area_id, table_name, is_occupied, status, seats) VALUES
(1, 'Bàn 01', false, 'HOẠT ĐỘNG',  4),
(1, 'Bàn 02', true,  'HOẠT ĐỘNG',  4),
(1, 'Bàn 03', false, 'HOẠT ĐỘNG',  4),
(1, 'Bàn 04', true,  'HOẠT ĐỘNG',  4),
(1, 'Bàn 05', false, 'HOẠT ĐỘNG',  6),
(1, 'Bàn 06', true,  'HOẠT ĐỘNG',  6),
(1, 'Bàn 07', false, 'ĐANG KHÓA',  4),
(1, 'Bàn 08', false, 'HOẠT ĐỘNG',  4);

-- Lầu 1 (area_id=2) - 6 bàn: table_id 9..14
INSERT INTO `tables` (area_id, table_name, is_occupied, status, seats) VALUES
(2, 'Bàn A01', true,  'HOẠT ĐỘNG', 4),
(2, 'Bàn A02', false, 'HOẠT ĐỘNG', 4),
(2, 'Bàn A03', true,  'HOẠT ĐỘNG', 4),
(2, 'Bàn A04', false, 'HOẠT ĐỘNG', 4),
(2, 'Bàn A05', false, 'HOẠT ĐỘNG', 6),
(2, 'Bàn A06', true,  'HOẠT ĐỘNG', 6);

-- Khu VIP (area_id=3) - 4 bàn: table_id 15..18
INSERT INTO `tables` (area_id, table_name, is_occupied, status, seats) VALUES
(3, 'VIP 01', false, 'HOẠT ĐỘNG', 8),
(3, 'VIP 02', true,  'HOẠT ĐỘNG', 8),
(3, 'VIP 03', false, 'HOẠT ĐỘNG', 8),
(3, 'VIP 04', false, 'BẢO TRÌ',   8);

-- Sân Vườn (area_id=4) - 5 bàn: table_id 19..23
INSERT INTO `tables` (area_id, table_name, is_occupied, status, seats) VALUES
(4, 'SV 01', false, 'HOẠT ĐỘNG', 4),
(4, 'SV 02', false, 'HOẠT ĐỘNG', 4),
(4, 'SV 03', true,  'HOẠT ĐỘNG', 4),
(4, 'SV 04', false, 'ĐANG KHÓA', 6),
(4, 'SV 05', false, 'HOẠT ĐỘNG', 6);

-- Khu A - BT (area_id=5) - 6 bàn: table_id 24..29
INSERT INTO `tables` (area_id, table_name, is_occupied, status, seats) VALUES
(5, 'A01', true,  'HOẠT ĐỘNG', 4),
(5, 'A02', false, 'HOẠT ĐỘNG', 4),
(5, 'A03', true,  'HOẠT ĐỘNG', 4),
(5, 'A04', false, 'HOẠT ĐỘNG', 4),
(5, 'A05', false, 'HOẠT ĐỘNG', 4),
(5, 'A06', true,  'HOẠT ĐỘNG', 4);

-- Khu B - BT (area_id=6): table_id 30..33
INSERT INTO `tables` (area_id, table_name, is_occupied, status, seats) VALUES
(6, 'B01', false, 'HOẠT ĐỘNG', 4),
(6, 'B02', true,  'HOẠT ĐỘNG', 4),
(6, 'B03', false, 'HOẠT ĐỘNG', 4),
(6, 'B04', false, 'HOẠT ĐỘNG', 4);

-- Khu Ngoài Trời - BT (area_id=7): table_id 34..37
INSERT INTO `tables` (area_id, table_name, is_occupied, status, seats) VALUES
(7, 'NT 01', false, 'HOẠT ĐỘNG', 6),
(7, 'NT 02', false, 'HOẠT ĐỘNG', 6),
(7, 'NT 03', true,  'HOẠT ĐỘNG', 6),
(7, 'NT 04', false, 'ĐANG KHÓA', 6);

-- Phở HN: Khu Trong (area_id=8), Khu Ngoài (area_id=9): table_id 38..45
INSERT INTO `tables` (area_id, table_name, is_occupied, status, seats) VALUES
(8, 'Bàn 01', true,  'HOẠT ĐỘNG', 4),
(8, 'Bàn 02', false, 'HOẠT ĐỘNG', 4),
(8, 'Bàn 03', true,  'HOẠT ĐỘNG', 4),
(8, 'Bàn 04', false, 'HOẠT ĐỘNG', 4),
(9, 'Bàn 05', false, 'HOẠT ĐỘNG', 4),
(9, 'Bàn 06', true,  'HOẠT ĐỘNG', 4),
(9, 'Bàn 07', false, 'HOẠT ĐỘNG', 4),
(9, 'Bàn 08', false, 'HOẠT ĐỘNG', 4);

-- ============================================================
-- 5. UNITS - Đơn vị tính
-- Cột: id (auto), res_id, unit_name
-- ============================================================

-- Mỳ Cay Seoul Thủ Đức (res_id=1): unit_id 1..8
INSERT INTO units (res_id, unit_name) VALUES
(1, 'Tô'),
(1, 'Phần'),
(1, 'Ly'),
(1, 'Chai'),
(1, 'Lon'),
(1, 'Cái'),
(1, 'Đĩa'),
(1, 'Set');

-- Mỳ Cay Seoul Bình Thạnh (res_id=2): unit_id 9..14
INSERT INTO units (res_id, unit_name) VALUES
(2, 'Tô'),
(2, 'Phần'),
(2, 'Ly'),
(2, 'Chai'),
(2, 'Lon'),
(2, 'Cái');

-- Phở Hà Nội 1975 (res_id=3): unit_id 15..19
INSERT INTO units (res_id, unit_name) VALUES
(3, 'Tô'),
(3, 'Phần'),
(3, 'Ly'),
(3, 'Đĩa'),
(3, 'Cái');

-- ============================================================
-- 6. CATEGORIES - Danh mục menu
-- Cột: id (auto), res_id, cat_name
-- ============================================================

-- Mỳ Cay Seoul Thủ Đức (res_id=1): cat_id 1..6
INSERT INTO categories (res_id, cat_name) VALUES
(1, 'Mỳ Cay'),
(1, 'Lẩu'),
(1, 'Đồ Nướng'),
(1, 'Đồ Uống'),
(1, 'Ăn Vặt'),
(1, 'Tráng Miệng');

-- Mỳ Cay Seoul Bình Thạnh (res_id=2): cat_id 7..10
INSERT INTO categories (res_id, cat_name) VALUES
(2, 'Mỳ Cay'),
(2, 'Lẩu'),
(2, 'Đồ Uống'),
(2, 'Ăn Kèm');

-- Phở Hà Nội 1975 (res_id=3): cat_id 11..15
INSERT INTO categories (res_id, cat_name) VALUES
(3, 'Phở'),
(3, 'Bún'),
(3, 'Cơm'),
(3, 'Đồ Uống'),
(3, 'Ăn Vặt');

-- ============================================================
-- 7. MENU_ITEMS - Món ăn
-- Cột: id (auto), cat_id, unit_id, item_name, price, is_available, image_url
-- Nhà hàng 1: cat_id 1..6, unit_id 1..8
-- Nhà hàng 3: cat_id 11..15, unit_id 15..19
-- ============================================================

-- ---- Mỳ Cay Seoul Thủ Đức ----

-- Danh mục MỲ CAY (cat_id=1): item_id 1..12
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(1, 1, 'Mỳ Cay Bò Mỹ Cấp Độ 1',         55000, true,  ''),
(1, 1, 'Mỳ Cay Bò Mỹ Cấp Độ 3',         58000, true,  ''),
(1, 1, 'Mỳ Cay Bò Mỹ Cấp Độ 5',         60000, true,  ''),
(1, 1, 'Mỳ Cay Hải Sản Cấp Độ 1',       65000, true,  ''),
(1, 1, 'Mỳ Cay Hải Sản Cấp Độ 3',       68000, true,  ''),
(1, 1, 'Mỳ Cay Hải Sản Cấp Độ 5',       70000, true,  ''),
(1, 1, 'Mỳ Cay Gà Cấp Độ 1',            52000, true,  ''),
(1, 1, 'Mỳ Cay Gà Cấp Độ 3',            55000, true,  ''),
(1, 1, 'Mỳ Cay Nấm Đặc Biệt',           50000, true,  ''),
(1, 1, 'Mỳ Kim Chi Cay Truyền Thống',    48000, true,  ''),
(1, 1, 'Mỳ Cay Tôm Đặc Biệt',           62000, true,  ''),
(1, 8, 'Set Đôi Mỳ Cay + Lẩu Mini',    120000, true,  '');

-- Danh mục LẨU (cat_id=2): item_id 13..21
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(2, 8, 'Lẩu Hải Sản Cay',              250000, true,  ''),
(2, 8, 'Lẩu Bò Mỹ Cay',               220000, true,  ''),
(2, 8, 'Lẩu Gà Lá Chanh',             200000, true,  ''),
(2, 8, 'Lẩu Thái Chua Cay',           230000, true,  ''),
(2, 8, 'Lẩu Nấm Chay',                180000, true,  ''),
(2, 2, 'Thêm Mì Tươi',                 20000, true,  ''),
(2, 2, 'Thêm Rau Lẩu',                 25000, true,  ''),
(2, 2, 'Thêm Bò Thái Lát',             55000, true,  ''),
(2, 2, 'Thêm Hải Sản Hỗn Hợp',         60000, true,  '');

-- Danh mục ĐỒ NƯỚNG (cat_id=3): item_id 22..27
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(3, 2, 'Thịt Bò Nướng Bulgogi',         75000, true,  ''),
(3, 2, 'Gà Nướng Sốt Cay Hàn',         65000, true,  ''),
(3, 2, 'Tôm Nướng Muối Ớt',            80000, true,  ''),
(3, 2, 'Mực Nướng Sa Tế',              70000, true,  ''),
(3, 7, 'Đĩa Rau Nướng Hàn Quốc',       35000, true,  ''),
(3, 6, 'Phomai Que Chiên',              30000, true,  '');

-- Danh mục ĐỒ UỐNG (cat_id=4): item_id 28..39
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(4, 3, 'Trà Chanh Sả',                  20000, true,  ''),
(4, 3, 'Trà Đào Cam Sả',               25000, true,  ''),
(4, 3, 'Trà Sữa Trân Châu',            35000, true,  ''),
(4, 3, 'Nước Ép Dâu Tây',              30000, true,  ''),
(4, 3, 'Nước Ép Dứa Bạc Hà',           28000, true,  ''),
(4, 3, 'Sinh Tố Bơ',                   35000, true,  ''),
(4, 3, 'Nước Cam Tươi',                25000, true,  ''),
(4, 4, 'Bia Tiger Chai 330ml',          25000, true,  ''),
(4, 5, 'Bia Heineken Lon',              22000, true,  ''),
(4, 5, 'Coca Cola Lon',                 15000, true,  ''),
(4, 5, 'Nước Suối Lavie',              10000, true,  ''),
(4, 3, 'Soju Hàn Quốc Chá',            55000, false, '');  -- Tạm ẩn

-- Danh mục ĂN VẶT (cat_id=5): item_id 40..47
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(5, 6, 'Khăn Lạnh',                       2000, true,  ''),
(5, 2, 'Salad Ức Gà Hàn Quốc',           45000, true,  ''),
(5, 7, 'Kim Chi Cải Thảo Truyền Thống',   25000, true,  ''),
(5, 7, 'Khoai Tây Chiên Phô Mai',         35000, true,  ''),
(5, 2, 'Bánh Mì Bơ Tỏi Nướng',           20000, true,  ''),
(5, 6, 'Mandu Chiên (5 cái)',             40000, true,  ''),
(5, 6, 'Tokbokki Sốt Gochujang',          45000, true,  ''),
(5, 7, 'Đĩa Rau Sống',                   15000, true,  '');

-- Danh mục TRÁNG MIỆNG (cat_id=6): item_id 48..51
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(6, 2, 'Kem Dâu Tây Hàn Quốc',           45000, true,  ''),
(6, 2, 'Bánh Hotteok Nhân Quế',           30000, true,  ''),
(6, 3, 'Chè Thái Đặc Biệt',              35000, true,  ''),
(6, 2, 'Bingsu Đào Sữa',                 55000, true,  '');

-- ---- Phở Hà Nội 1975 ----
-- cat_id: 11=Phở, 12=Bún, 13=Cơm, 14=ĐồUống, 15=ĂnVặt
-- unit_id: 15=Tô, 16=Phần, 17=Ly, 18=Đĩa, 19=Cái

-- Danh mục PHỞ (cat_id=11): item_id 52..55
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(11, 15, 'Phở Bò Tái Chín',              55000, true,  ''),
(11, 15, 'Phở Bò Gầu Gân',              60000, true,  ''),
(11, 15, 'Phở Bò Đặc Biệt',             70000, true,  ''),
(11, 15, 'Phở Gà Truyền Thống',          50000, true,  '');

-- Danh mục BÚN (cat_id=12): item_id 56..58
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(12, 15, 'Bún Bò Huế',                   52000, true,  ''),
(12, 15, 'Bún Chả Hà Nội',              55000, true,  ''),
(12, 15, 'Bún Riêu Cua Đồng',            50000, true,  '');

-- Danh mục CƠM (cat_id=13): item_id 59..60
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(13, 16, 'Cơm Tấm Sườn Bì Chả',         45000, true,  ''),
(13, 16, 'Cơm Chiên Dương Châu',         40000, true,  '');

-- Danh mục ĐỒ UỐNG - Phở (cat_id=14): item_id 61..62
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(14, 17, 'Trà Đá',                        5000, true,  ''),
(14, 17, 'Nước Ép Cam',                  25000, true,  '');

-- Danh mục ĂN VẶT - Phở (cat_id=15): item_id 63..64
INSERT INTO menu_items (cat_id, unit_id, item_name, price, is_available, image_url) VALUES
(15, 18, 'Đĩa Quẩy Rán',                10000, true,  ''),
(15, 19, 'Chả Lụa',                      20000, true,  '');

-- ============================================================
-- 8. ORDERS - Hóa đơn
-- Cột: id (auto), table_id, status, total_amount, user_id, created_at, checkout_at
-- status: 0=Đang phục vụ, 1=Đã thanh toán, 2=Đã hủy
-- Bàn đang có khách (is_occupied=true): 2,4,6,9,11,14,16,21...
-- ============================================================

-- Đang phục vụ (status=0): order_id 1..7
INSERT INTO orders (table_id, status, total_amount, user_id, created_at, checkout_at) VALUES
(2,  0, 0,      1, NOW() - INTERVAL 1 HOUR,   NULL),
(4,  0, 0,      1, NOW() - INTERVAL 2 HOUR,   NULL),
(6,  0, 0,      2, NOW() - INTERVAL 45 MINUTE,NULL),
(9,  0, 0,      1, NOW() - INTERVAL 30 MINUTE,NULL),
(11, 0, 0,      2, NOW() - INTERVAL 1 HOUR,   NULL),
(14, 0, 0,      1, NOW() - INTERVAL 20 MINUTE,NULL),
(16, 0, 0,      1, NOW() - INTERVAL 2 HOUR,   NULL);

-- Đã thanh toán (status=1): order_id 8..13
INSERT INTO orders (table_id, status, total_amount, user_id, created_at, checkout_at) VALUES
(1,  1, 178000, 1, NOW() - INTERVAL 3 HOUR,  NOW() - INTERVAL 2 HOUR),
(3,  1, 125000, 2, NOW() - INTERVAL 4 HOUR,  NOW() - INTERVAL 3 HOUR),
(5,  1, 310000, 1, NOW() - INTERVAL 5 HOUR,  NOW() - INTERVAL 4 HOUR),
(7,  1, 250000, 2, NOW() - INTERVAL 6 HOUR,  NOW() - INTERVAL 5 HOUR),
(1,  1, 203000, 1, NOW() - INTERVAL 8 HOUR,  NOW() - INTERVAL 7 HOUR),
(3,  1,  95000, 2, NOW() - INTERVAL 9 HOUR,  NOW() - INTERVAL 8 HOUR);

-- ============================================================
-- 9. ORDER_DETAILS - Chi tiết gọi món
-- Cột: id (auto), order_id, item_id, quantity, price_at_sale,
--      note, status, cancel_reason, updated_at
-- status: 0=Nháp, 1=Đã gửi bếp, 2=Đã hủy
-- item_id: 1=MỳCayBòCấpĐộ1(55k), 28=TràChanhSả(20k), 40=KhănLạnh(2k)...
-- ============================================================

-- Bàn 02 (order_id=1) - Đã gửi bếp
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(1, 1,  1, 55000, 'Cấp độ 1, không hành ngò', 1, NULL, NOW()),
(1, 28, 2, 20000, '',                           1, NULL, NOW()),
(1, 40, 2,  2000, '',                           1, NULL, NOW()),
(1, 41, 1, 45000, 'Ít rau mầm',                1, NULL, NOW());

-- Bàn 04 (order_id=2) - Đang gọi thêm (status 0 và 1 lẫn lộn)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(2, 4,  1, 65000, 'Hải sản cấp độ 3, thêm mực',1, NULL, NOW()),
(2, 5,  1, 68000, 'Cấp độ 5, cực cay',          1, NULL, NOW()),
(2, 29, 1, 25000, '',                            1, NULL, NOW()),
(2, 38, 2, 25000, '',                            1, NULL, NOW()),
(2, 41, 1, 45000, '',                            0, NULL, NOW());  -- Salad nháp chưa gửi

-- Bàn 06 (order_id=3)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(3, 13, 1, 250000,'4 người, lấy thêm rau',      1, NULL, NOW()),
(3, 20, 1,  55000,'',                            1, NULL, NOW()),
(3, 21, 1,  60000,'',                            1, NULL, NOW()),
(3, 28, 4,  20000,'',                            1, NULL, NOW()),
(3, 40, 4,   2000,'',                            1, NULL, NOW());

-- Bàn A01 (order_id=4)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(4, 3,  2,  58000,'Cấp độ 3',                   1, NULL, NOW()),
(4, 22, 1,  75000,'Bò nướng chín kỹ',           1, NULL, NOW()),
(4, 34, 1,  55000,'',                            1, NULL, NOW()),
(4, 45, 2,  40000,'',                            1, NULL, NOW());

-- Bàn A03 (order_id=5)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(5, 14, 1, 220000,'Lẩu bò, ít cay',             1, NULL, NOW()),
(5, 18, 2,  55000,'',                            1, NULL, NOW()),
(5, 19, 1,  60000,'',                            1, NULL, NOW()),
(5, 29, 1,  25000,'',                            0, NULL, NOW());  -- Nháp

-- Bàn A06 (order_id=6) - Có một món bị hủy
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(6, 7,  1,  52000,'Gà cấp 1',                   1, NULL, NOW()),
(6, 31, 2,  35000,'',                            1, NULL, NOW()),
(6, 11, 1,  62000,'',                            2, 'Khách đổi ý', NOW()),  -- Đã hủy
(6, 40, 3,   2000,'',                            1, NULL, NOW());

-- Đã thanh toán (order_id=8)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(8, 2,  1,  58000,'',                            1, NULL, NOW()),
(8, 10, 1,  48000,'',                            1, NULL, NOW()),
(8, 28, 2,  20000,'',                            1, NULL, NOW()),
(8, 40, 2,   2000,'',                            1, NULL, NOW());

-- Đã thanh toán (order_id=9)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(9, 1,  1,  55000,'Không hành',                  1, NULL, NOW()),
(9, 28, 1,  20000,'',                             1, NULL, NOW()),
(9, 41, 1,  45000,'',                             1, NULL, NOW());

-- Đã thanh toán (order_id=10)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(10, 13, 1, 250000,'Lẩu hải sản đặc biệt',      1, NULL, NOW()),
(10, 28, 4,  20000,'',                           1, NULL, NOW()),
(10, 40, 4,   2000,'',                           1, NULL, NOW());

-- Đã thanh toán (order_id=11)
INSERT INTO order_details (order_id, item_id, quantity, price_at_sale, note, status, cancel_reason, updated_at) VALUES
(11, 14, 1, 220000,'',                           1, NULL, NOW()),
(11, 18, 1,  55000,'',                           1, NULL, NOW()),
(11, 40, 2,   2000,'',                           1, NULL, NOW()),
(11, 33, 2,  25000,'',                           1, NULL, NOW()),  -- 2 trà đào
(11,  3, 1,  58000,'Cấp độ 3',                  2, 'Nhầm order', NOW());  -- Đã hủy

-- ============================================================
-- 10. SHIFTS - Ca làm việc (nhà hàng 1)
-- Cột: id (auto), restaurant_id, start_time, end_time,
--      starting_fund, total_revenue, status, employee_names, employee_ids
-- ============================================================
INSERT INTO shifts (restaurant_id, start_time, end_time, starting_fund, total_revenue, status, employee_names, employee_ids) VALUES
(1, NOW() - INTERVAL 10 HOUR, NULL,              500000, NULL,   'OPEN',   'Admin,Nhân viên 1',   '1,2'),
(1, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 18 HOUR, 500000, 3200000, 'CLOSED', 'Admin,Nhân viên 2', '1,3'),
(1, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 42 HOUR, 500000, 4100000, 'CLOSED', 'Nhân viên 1,Thu ngân 1', '2,4'),
(1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 66 HOUR, 500000, 2800000, 'CLOSED', 'Admin,Nhân viên 1,Thu ngân 2', '1,2,5');

-- ============================================================
-- CẬP NHẬT TỔNG TIỀN order_details VÀO orders
-- ============================================================
UPDATE orders SET total_amount = 178000 WHERE id = 8;
UPDATE orders SET total_amount = 125000 WHERE id = 9;
UPDATE orders SET total_amount = 310000 WHERE id = 10;
UPDATE orders SET total_amount = 250000 WHERE id = 11;
UPDATE orders SET total_amount = 203000 WHERE id = 12;
UPDATE orders SET total_amount =  95000 WHERE id = 13;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- KIỂM TRA KẾT QUẢ
-- ============================================================
SELECT 'restaurants'   AS bang, COUNT(*) AS so_ban_ghi FROM restaurants
UNION ALL SELECT 'users',         COUNT(*) FROM users
UNION ALL SELECT 'areas',         COUNT(*) FROM areas
UNION ALL SELECT 'tables',        COUNT(*) FROM `tables`
UNION ALL SELECT 'units',         COUNT(*) FROM units
UNION ALL SELECT 'categories',    COUNT(*) FROM categories
UNION ALL SELECT 'menu_items',    COUNT(*) FROM menu_items
UNION ALL SELECT 'orders',        COUNT(*) FROM orders
UNION ALL SELECT 'order_details', COUNT(*) FROM order_details
UNION ALL SELECT 'shifts',        COUNT(*) FROM shifts;

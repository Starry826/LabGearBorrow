CREATE TABLE equipment (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL,
                            category TINYINT NOT NULL COMMENT '1-物理, 2-化学, 3-生物',
                            sub_category TINYINT NOT NULL COMMENT '物理:1-电学,2-光学,3-力学,4-声学; 化学:1-玻璃仪器,2-化学试剂,3-加热设备,4-计量设备; 生物:1-显微镜,2-培养设备,3-解剖工具,4-无菌设备,5-标本制作工具',
                            total_quantity INT NOT NULL DEFAULT 1 COMMENT '总设备数量',
                            available_quantity INT NOT NULL DEFAULT 1 COMMENT '可用设备数量',
                            deposit DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '押金金额'
);
CREATE TABLE user (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
                        password VARCHAR(255) NOT NULL COMMENT '密码',
                        email VARCHAR(20) NULL COMMENT '邮箱',
                        avatar VARCHAR(255) NULL COMMENT '头像URL',
                        role TINYINT NOT NULL DEFAULT 1 COMMENT '身份认证：0-管理员，1-普通用户'
);
CREATE TABLE borrow (
                                id INT PRIMARY KEY AUTO_INCREMENT,
                                user_id INT NOT NULL,
                                equipment_id INT NOT NULL,
                                borrow_time DATETIME COMMENT '借用时间',
                                expected_return_time DATETIME COMMENT '预计归还时间',
                                actual_return_time DATETIME COMMENT '实际归还时间',
                                status TINYINT NOT NULL DEFAULT 1 COMMENT '借用状态 (1:申请中 2:已借用 3:已归还 4:逾期 5:拒绝)',
                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- 插入物理实验设备
INSERT INTO equipment (name, category, sub_category, total_quantity, available_quantity, deposit) VALUES
-- 电学实验器材
('导线', 1, 1, 50, 50, 5.00),
('电阻箱', 1, 1, 15, 15, 50.00),
('滑动变阻器', 1, 1, 25, 25, 30.00),
('直流电源', 1, 1, 12, 12, 100.00),
('电流表', 1, 1, 20, 20, 80.00),
('电压表', 1, 1, 20, 20, 80.00),

-- 光学实验器材
('凸透镜', 1, 2, 18, 18, 25.00),
('凹透镜', 1, 2, 18, 18, 25.00),
('平面镜', 1, 2, 15, 15, 20.00),
('光具座', 1, 2, 10, 10, 60.00),

-- 力学实验器材
('弹簧测力计', 1, 3, 22, 22, 15.00),
('刻度尺', 1, 3, 30, 30, 5.00),
('滑轮', 1, 3, 20, 20, 10.00),
('滑轮组', 1, 3, 15, 15, 25.00),

-- 声学实验器材
('音叉', 1, 4, 8, 8, 15.00),
('共鸣箱', 1, 4, 6, 6, 40.00),
('示波器', 1, 4, 5, 5, 200.00);

-- 插入化学实验设备
INSERT INTO equipment (name, category, sub_category, total_quantity, available_quantity, deposit) VALUES
-- 玻璃仪器
('试管', 2, 1, 120, 120, 3.00),
('烧杯', 2, 1, 60, 60, 8.00),
('量筒', 2, 1, 25, 25, 15.00),
('滴管', 2, 1, 40, 40, 2.00),
('漏斗', 2, 1, 20, 20, 10.00),

-- 化学试剂
('盐酸', 2, 2, 12, 12, 20.00),
('硫酸', 2, 2, 10, 10, 25.00),
('氢氧化钠', 2, 2, 12, 12, 15.00),
('氯化钠', 2, 2, 15, 15, 5.00),
('硫酸铜', 2, 2, 10, 10, 12.00),

-- 加热设备
('酒精灯', 2, 3, 35, 35, 10.00),
('石棉网', 2, 3, 30, 30, 5.00),
('三角架', 2, 3, 25, 25, 8.00),

-- 计量设备
('天平', 2, 4, 8, 8, 150.00),
('温度计', 2, 4, 20, 20, 12.00);

-- 插入生物实验设备
INSERT INTO equipment (name, category, sub_category, total_quantity, available_quantity, deposit) VALUES
-- 显微镜
('光学显微镜', 3, 1, 15, 15, 300.00),
('体视显微镜', 3, 1, 8, 8, 500.00),
('放大镜', 3, 1, 25, 25, 15.00),

-- 培养设备
('恒温培养箱', 3, 2, 4, 4, 800.00),
('光照培养箱', 3, 2, 3, 3, 1000.00),
('培养皿', 3, 2, 100, 100, 2.00),

-- 解剖工具
('解剖刀', 3, 3, 25, 25, 20.00),
('解剖剪', 3, 3, 25, 25, 25.00),
('镊子', 3, 3, 30, 30, 8.00),
('解剖盘', 3, 3, 12, 12, 15.00),

-- 无菌设备
('酒精灯', 3, 4, 25, 25, 10.00),
('高压蒸汽灭菌锅', 3, 4, 2, 2, 1200.00),
('超净工作台', 3, 4, 3, 3, 1500.00),

-- 标本制作工具
('载玻片', 3, 5, 150, 150, 1.00),
('盖玻片', 3, 5, 150, 150, 0.50),
('染色剂', 3, 5, 30, 30, 25.00);

-- 插入用户数据
INSERT INTO user(username, password, email, avatar, role) values
('admin','123','cxlcxl722@163.com','/images/default.jpg',0),
('123','123','cxlcxl722@163.com','/images/default.jpg',1);

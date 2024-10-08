-- V2024.09.29__add_columns_to_sub_and_items.sql

-- 在 sub 表中新增 keep_last 字段
ALTER TABLE sub ADD COLUMN `keep_last` INTEGER;

-- 在 items 表中新增 public_time 字段
ALTER TABLE items ADD COLUMN `public_time` INTEGER;

-- 在 sub 表中新增 survival_way 字段，用于存放节目存活方式
ALTER TABLE sub ADD COLUMN `survival_way` TEXT DEFAULT 'keepTime';

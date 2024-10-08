-- V2024.09.30__add_columns_to_sub.sql

-- 在 sub 表中新增 sub_type 字段，用于订阅类型：empty 表示空订阅、plugin 表示根据使用插件订阅。默认是plugin
ALTER TABLE sub ADD COLUMN `sub_type` TEXT DEFAULT 'plugin';

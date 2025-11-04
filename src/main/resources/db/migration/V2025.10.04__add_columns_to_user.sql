-- V2025.10.04__add_columns_to_user.sql

-- 在 user 表中新增 cookie_cloud_info 字段
ALTER TABLE user ADD COLUMN `cookie_cloud_info` TEXT DEFAULT NULL;

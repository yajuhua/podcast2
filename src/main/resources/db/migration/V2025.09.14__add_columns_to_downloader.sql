-- V2025.09.14__add_columns_to_downloader.sql

-- 在 downloader 表中新增 update_args 字段
ALTER TABLE downloader ADD COLUMN `update_args` TEXT default NULL;
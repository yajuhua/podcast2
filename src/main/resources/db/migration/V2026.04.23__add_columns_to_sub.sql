-- V2026.04.23__add_columns_to_sub.sql

-- 在 sub 表中新增 unix_cron_expression 字段
ALTER TABLE sub ADD COLUMN `unix_cron_expression` TEXT DEFAULT NULL;

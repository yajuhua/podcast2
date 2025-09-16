-- V2025.09.08__add_columns_to_sub.sql

-- 新增schedule_type 默认=cron 表示按时间间隔。新增cron_expression 默认=空 按cron表达式运行
ALTER TABLE sub ADD COLUMN `schedule_type` TEXT DEFAULT 'cron';
ALTER TABLE sub ADD COLUMN `cron_expression` TEXT DEFAULT  NULL;


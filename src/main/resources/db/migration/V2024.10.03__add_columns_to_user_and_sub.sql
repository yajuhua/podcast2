-- V2024.10.03__add_columns_to_user_and_sub.sql

-- 在 user 表中新增 api_doc 字段，开放和关闭api文档 0表示关闭
ALTER TABLE user ADD COLUMN `api_doc` integer default 0;

-- V2024.10.03__add_columns_to_user_and_sub.sql

-- 在 user 表中新增 api_doc 字段，开放和关闭api文档 0表示关闭
ALTER TABLE user ADD COLUMN `api_doc` integer default 0;

-- 在 sub 表中新增 sync_way 字段，同步节目方式， latest仅同步最新节目，recent同步最近30集
ALTER TABLE sub ADD COLUMN `sync_way` TEXT default 'latest';
